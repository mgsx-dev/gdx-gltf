package net.mgsx.gltf.exporters;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gltf.data.data.GLTFAccessor;
import net.mgsx.gltf.data.geometry.GLTFMorphTarget;
import net.mgsx.gltf.data.geometry.GLTFPrimitive;
import net.mgsx.gltf.loaders.shared.GLTFTypes;
import net.mgsx.gltf.scene3d.attributes.PBRVertexAttributes;

class GLTFMeshExporter {
	
	private final GLTFExporter base;
	
	private ObjectMap<Mesh, GLTFPrimitive> layouts = new ObjectMap<Mesh, GLTFPrimitive>();
	
	public GLTFMeshExporter(GLTFExporter base) {
		super();
		this.base = base;
	}

	GLTFPrimitive exportMeshPart(MeshPart meshPart) {
		Mesh mesh = meshPart.mesh;
		GLTFPrimitive primitive = new GLTFPrimitive();
		primitive.attributes = new ObjectMap<String, Integer>();
		primitive.mode = mapPrimitiveMode(meshPart.primitiveType);
		
		GLTFPrimitive layout = layouts.get(mesh);
		if(layout != null){
			copyLayout(primitive, layout);
		}else{
			layouts.put(mesh, primitive);
			exportMesh(primitive, mesh);
		}
		
		// mesh may not have indices
		if(mesh.getNumIndices() > 0)
		{
			ShortBuffer outBuffer = base.binManager.beginShorts(meshPart.size);
			ShortBuffer inBuffer = mesh.getIndicesBuffer();
			if(meshPart.offset == 0 && meshPart.size == mesh.getNumIndices()){
				outBuffer.put(mesh.getIndicesBuffer());
			}else{
				short[] localIndices = new short[meshPart.size];
				inBuffer.position(meshPart.offset);
				inBuffer.get(localIndices);
				outBuffer.put(localIndices);
			}
			inBuffer.rewind();
			
			GLTFAccessor accessor = base.obtainAccessor();
			accessor.type = GLTFTypes.TYPE_SCALAR;
			accessor.componentType = GLTFTypes.C_SHORT;
			accessor.count = meshPart.size;
			accessor.bufferView = base.binManager.end();
			
			primitive.indices = base.root.accessors.size-1;
		}
		
		return primitive;
	}
	
	private void copyLayout(GLTFPrimitive primitive, GLTFPrimitive layout) {
		primitive.attributes.putAll(layout.attributes);
		if(layout.targets != null){
			primitive.targets = new Array<GLTFMorphTarget>();
			primitive.targets.addAll(layout.targets);
		}
	}

	private void exportMesh(GLTFPrimitive primitive, Mesh mesh) {
		FloatBuffer vertices = mesh.getVerticesBuffer();
		
		Array<FloatBuffer> boneWeightsBuffers = new Array<FloatBuffer>();
		Array<ShortBuffer> boneJointsBuffers = new Array<ShortBuffer>(); // TODO short or byte (for small amount of bones)
		
		// split vertices individual attributes
		int stride = mesh.getVertexAttributes().vertexSize / 4;
		int numVertices = mesh.getNumVertices();
		for(VertexAttribute a : mesh.getVertexAttributes()){
			String accessorType;
			int accessorComponentType = GLTFTypes.C_FLOAT;
			boolean useTargets = false;
			final String attributeKey;
			if(a.usage == Usage.Position){
				attributeKey = "POSITION";
				accessorType = GLTFTypes.TYPE_VEC3;
			}else if(a.usage == Usage.Normal){
				attributeKey = "NORMAL";
				accessorType = GLTFTypes.TYPE_VEC3;
			}else if(a.usage == Usage.Tangent){
				attributeKey = "TANGENT";
				accessorType = GLTFTypes.TYPE_VEC3;
			}else if(a.usage == Usage.ColorUnpacked){
				attributeKey = "COLOR_" + a.unit;
				accessorType = GLTFTypes.TYPE_VEC4;
				if(a.type == GL20.GL_FLOAT){
					accessorComponentType = GLTFTypes.C_FLOAT;			
				}else if(a.type == GL20.GL_UNSIGNED_SHORT){
					accessorComponentType = GLTFTypes.C_USHORT;
				}else if(a.type == GL20.GL_UNSIGNED_BYTE){
					accessorComponentType = GLTFTypes.C_UBYTE;
				}else{
					throw new GdxRuntimeException("color attribute format not supported");
				}
			}else if(a.usage == Usage.TextureCoordinates){
				attributeKey = "TEXCOORD_" + a.unit;
				accessorType = GLTFTypes.TYPE_VEC2;
			}else if(a.usage == PBRVertexAttributes.Usage.PositionTarget){
				attributeKey = "POSITION";
				accessorType = GLTFTypes.TYPE_VEC3;
				useTargets = true;
			}else if(a.usage == PBRVertexAttributes.Usage.NormalTarget){
				attributeKey = "NORMAL";
				accessorType = GLTFTypes.TYPE_VEC3;
				useTargets = true;
			}else if(a.usage == PBRVertexAttributes.Usage.TangentTarget){
				attributeKey = "TANGENT";
				accessorType = GLTFTypes.TYPE_VEC3;
				useTargets = true;
			}else if(a.usage == Usage.BoneWeight){
				
				while(a.unit >= boneWeightsBuffers.size) boneWeightsBuffers.add(null);
				while(a.unit >= boneJointsBuffers.size) boneJointsBuffers.add(null);
				
				FloatBuffer boneWeightsBuffer = FloatBuffer.allocate(numVertices);
				boneWeightsBuffers.set(a.unit, boneWeightsBuffer);
				
				ShortBuffer boneJointsBuffer = ShortBuffer.allocate(numVertices);
				boneJointsBuffers.set(a.unit, boneJointsBuffer);
				
				for(int i=0 ; i<numVertices ; i++){
					vertices.position(i * stride + a.offset/4);
					
					int boneID = (int)vertices.get();
					short shortID = (short)(boneID & 0xFFFF);
					
					float boneWeight = vertices.get();
					
					boneWeightsBuffer.put(boneWeight);
					boneJointsBuffer.put(shortID);
				}
				
				// skip this attribute because will be output later
				continue;
			}else{
				throw new GdxRuntimeException("unsupported vertex attribute " + a.alias);
			}
			
			GLTFAccessor accessor = base.obtainAccessor();
			accessor.type = accessorType;
			accessor.componentType = accessorComponentType;
			accessor.count = numVertices;
			
			if(useTargets){
				if(primitive.targets == null) primitive.targets = new Array<GLTFMorphTarget>();
				while(primitive.targets.size <= a.unit) primitive.targets.add(new GLTFMorphTarget());
				primitive.targets.get(a.unit).put(attributeKey, base.root.accessors.size-1);
			}else{
				primitive.attributes.put(attributeKey, base.root.accessors.size-1);
			}
			
			int attributeFloats = GLTFTypes.accessorStrideSize(accessor) / 4;
			
			FloatBuffer outBuffer = base.binManager.beginFloats(numVertices * attributeFloats);
			
			for(int i=0 ; i<numVertices ; i++){
				vertices.position(i * stride + a.offset/4);
				for(int j=0 ; j<attributeFloats ; j++){
					outBuffer.put(vertices.get());
				}
			}
			accessor.bufferView = base.binManager.end();
		}
		
		// export bones
		if(boneWeightsBuffers.size > 0){
			GLTFAccessor accessor;
			
			int numGroup = boneWeightsBuffers.size > 4 ? 2 : 1;
			
			// export weights
			for(int g=0 ; g<numGroup ; g++){
				accessor = base.obtainAccessor();
				FloatBuffer outBuffer = base.binManager.beginFloats(numVertices * 4);
				for(int i=0 ; i<numVertices ; i++){
					// first is bone, second is weight
					for(int j=0 ; j<4 ; j++){
						FloatBuffer boneWeightsBuffer = boneWeightsBuffers.get(g*4+j);
						if(boneWeightsBuffer != null){
							outBuffer.put(boneWeightsBuffer.get(i));
						}else{
							// fill zeros
							outBuffer.put(0f);
						}
					}
				}
				accessor.type = GLTFTypes.TYPE_VEC4;
				accessor.componentType = GLTFTypes.C_FLOAT;
				accessor.count = numVertices;
				accessor.bufferView = base.binManager.end();
				primitive.attributes.put("WEIGHTS_" + g, base.root.accessors.size-1);
			}
			
			// export joints
			for(int g=0 ; g<numGroup ; g++){
				accessor = base.obtainAccessor();
				ShortBuffer soutBuffer = base.binManager.beginShorts(numVertices * 4);
				for(int i=0 ; i<numVertices ; i++){
					// first is bone, second is weight
					for(int j=0 ; j<4 ; j++){
						ShortBuffer boneJointsBuffer = boneJointsBuffers.get(g*4+j);
						if(boneJointsBuffer != null){
							soutBuffer.put(boneJointsBuffer.get(i));
						}else{
							// fill zeros
							soutBuffer.put((short)0);
						}
					}
				}
				accessor.type = GLTFTypes.TYPE_VEC4;
				accessor.componentType = GLTFTypes.C_USHORT;
				accessor.count = numVertices;
				accessor.bufferView = base.binManager.end();
				primitive.attributes.put("JOINTS_" + g, base.root.accessors.size-1);
			}
		}
	}
	
	public static Integer mapPrimitiveMode(int type){
		switch(type){
		case GL20.GL_POINTS: return 0;
		case GL20.GL_LINES: return 1;
		case GL20.GL_LINE_LOOP: return 2;
		case GL20.GL_LINE_STRIP: return 3;
		case GL20.GL_TRIANGLES: return null; // default not need to be set
		case GL20.GL_TRIANGLE_STRIP: return 5;
		case GL20.GL_TRIANGLE_FAN: return 6;
		}
		throw new GdxRuntimeException("unsupported primitive type " + type);
	}
}
