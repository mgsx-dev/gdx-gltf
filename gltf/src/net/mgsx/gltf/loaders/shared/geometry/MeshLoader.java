package net.mgsx.gltf.loaders.shared.geometry;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gltf.data.data.GLTFAccessor;
import net.mgsx.gltf.data.data.GLTFBufferView;
import net.mgsx.gltf.data.geometry.GLTFMesh;
import net.mgsx.gltf.data.geometry.GLTFPrimitive;
import net.mgsx.gltf.loaders.blender.BlenderShapeKeys;
import net.mgsx.gltf.loaders.exceptions.GLTFIllegalException;
import net.mgsx.gltf.loaders.exceptions.GLTFUnsupportedException;
import net.mgsx.gltf.loaders.shared.GLTFTypes;
import net.mgsx.gltf.loaders.shared.data.DataResolver;
import net.mgsx.gltf.loaders.shared.material.MaterialLoader;
import net.mgsx.gltf.scene3d.attributes.PBRVertexAttributes;
import net.mgsx.gltf.scene3d.model.NodePartPlus;
import net.mgsx.gltf.scene3d.model.NodePlus;
import net.mgsx.gltf.scene3d.model.WeightVector;

public class MeshLoader {
	
	private ObjectMap<GLTFMesh, Array<NodePart>> meshMap = new ObjectMap<GLTFMesh, Array<NodePart>>();
	private final Array<Mesh> meshes = new Array<Mesh>();
	private int maxBones;
	
	public void load(Node node, GLTFMesh glMesh, DataResolver dataResolver, MaterialLoader materialLoader) 
	{
		((NodePlus)node).morphTargetNames = BlenderShapeKeys.parse(glMesh);
		
		Array<NodePart> parts = meshMap.get(glMesh);
		if(parts == null){
			parts = new Array<NodePart>();
			
			for(GLTFPrimitive primitive : glMesh.primitives){
				
				// indices
				short [] indices = loadIndices(primitive, dataResolver);
				int maxIndices = indices == null ? 0 : indices.length;
				
				// vertices
				Array<VertexAttribute> vertexAttributes = new Array<VertexAttribute>();
				Array<GLTFAccessor> glAccessors = new Array<GLTFAccessor>();
				
				Array<int[]> bonesIndices = new Array<int[]>();
				Array<float[]> bonesWeights = new Array<float[]>();
				
				boolean hasNormals = false;
				
				for(Entry<String, Integer> attribute : primitive.attributes){
					String attributeName = attribute.key;
					int accessorId = attribute.value;
					GLTFAccessor accessor = dataResolver.getAccessor(accessorId);
					boolean rawAttribute = true;
					
					if(attributeName.equals("POSITION")){
						if(!(GLTFTypes.TYPE_VEC3.equals(accessor.type) && accessor.componentType == GLTFTypes.C_FLOAT)) throw new GLTFIllegalException("illegal position attribute format");
						vertexAttributes.add(VertexAttribute.Position());
					}else if(attributeName.equals("NORMAL")){
						if(!(GLTFTypes.TYPE_VEC3.equals(accessor.type) && accessor.componentType == GLTFTypes.C_FLOAT)) throw new GLTFIllegalException("illegal normal attribute format");
						vertexAttributes.add(VertexAttribute.Normal());
						hasNormals = true;
					}else if(attributeName.equals("TANGENT")){
						if(!(GLTFTypes.TYPE_VEC4.equals(accessor.type) && accessor.componentType == GLTFTypes.C_FLOAT)) throw new GLTFIllegalException("illegal tangent attribute format");
						vertexAttributes.add(new VertexAttribute(Usage.Tangent, 4, ShaderProgram.TANGENT_ATTRIBUTE));
					}else if(attributeName.startsWith("TEXCOORD_")){
						if(!GLTFTypes.TYPE_VEC2.equals(accessor.type)) throw new GLTFIllegalException("illegal texture coordinate attribute type : " + accessor.type);
						if(accessor.componentType == GLTFTypes.C_UBYTE) throw new GLTFUnsupportedException("unsigned byte texture coordinate attribute not supported");
						if(accessor.componentType == GLTFTypes.C_USHORT) throw new GLTFUnsupportedException("unsigned short texture coordinate attribute not supported");
						if(accessor.componentType != GLTFTypes.C_FLOAT) throw new GLTFIllegalException("illegal texture coordinate component type : " + accessor.componentType);
						int unit = parseAttributeUnit(attributeName);
						vertexAttributes.add(VertexAttribute.TexCoords(unit));
					}else if(attributeName.startsWith("COLOR_")){
						int unit = parseAttributeUnit(attributeName);
						String alias = unit > 0 ? ShaderProgram.COLOR_ATTRIBUTE + unit : ShaderProgram.COLOR_ATTRIBUTE;
						if(GLTFTypes.TYPE_VEC4.equals(accessor.type)){
							if(GLTFTypes.C_FLOAT == accessor.componentType){
								vertexAttributes.add(new VertexAttribute(Usage.ColorUnpacked, 4, GL20.GL_FLOAT, false, alias));
							}
							else if(GLTFTypes.C_USHORT == accessor.componentType){
								vertexAttributes.add(new VertexAttribute(Usage.ColorUnpacked, 4, GL20.GL_UNSIGNED_SHORT, true, alias));
							}
							else if(GLTFTypes.C_UBYTE == accessor.componentType){
								vertexAttributes.add(new VertexAttribute(Usage.ColorUnpacked, 4, GL20.GL_UNSIGNED_BYTE, true, alias));
							}else{
								throw new GLTFIllegalException("illegal color attribute component type: " + accessor.type);
							}
						}
						else if(GLTFTypes.TYPE_VEC3.equals(accessor.type)){
							if(GLTFTypes.C_FLOAT == accessor.componentType){
								vertexAttributes.add(new VertexAttribute(Usage.ColorUnpacked, 3, GL20.GL_FLOAT, false, alias));
							}
							else if(GLTFTypes.C_USHORT == accessor.componentType){
								throw new GLTFUnsupportedException("RGB unsigned short color attribute not supported");
							}
							else if(GLTFTypes.C_UBYTE == accessor.componentType){
								throw new GLTFUnsupportedException("RGB unsigned byte color attribute not supported");
							}else{
								throw new GLTFIllegalException("illegal color attribute component type: " + accessor.type);
							}
						}
						else{
							throw new GLTFIllegalException("illegal color attribute type: " + accessor.type);
						}
							
					}else if(attributeName.startsWith("WEIGHTS_")){
						rawAttribute = false;
						
						if(!GLTFTypes.TYPE_VEC4.equals(accessor.type)){
							throw new GLTFIllegalException("illegal weight attribute type: " + accessor.type);
						}
						
						int unit = parseAttributeUnit(attributeName);
						if(unit >= bonesWeights.size) bonesWeights.setSize(unit+1);

						if(accessor.componentType == GLTFTypes.C_FLOAT){
							bonesWeights.set(unit, dataResolver.readBufferFloat(accessorId));
						}else if(accessor.componentType == GLTFTypes.C_USHORT){ 
							throw new GLTFUnsupportedException("unsigned short weight attribute not supported");
						}else if(accessor.componentType == GLTFTypes.C_UBYTE){ 
							throw new GLTFUnsupportedException("unsigned byte weight attribute not supported");
						}else{
							throw new GLTFIllegalException("illegal weight attribute type: " + accessor.componentType);
						}
					}else if(attributeName.startsWith("JOINTS_")){
						rawAttribute = false;
						
						if(!GLTFTypes.TYPE_VEC4.equals(accessor.type)){
							throw new GLTFIllegalException("illegal joints attribute type: " + accessor.type);
						}
						
						int unit = parseAttributeUnit(attributeName);
						if(unit >= bonesIndices.size) bonesIndices.setSize(unit+1);
						
						if(accessor.componentType == GLTFTypes.C_UBYTE){ // unsigned byte
							bonesIndices.set(unit, dataResolver.readBufferUByte(accessorId));
						}else if(accessor.componentType == GLTFTypes.C_USHORT){ // unsigned short
							bonesIndices.set(unit, dataResolver.readBufferUShort(accessorId));
						}else{
							throw new GLTFIllegalException("illegal type for joints: " + accessor.componentType);
						}
						if(accessor.max != null){
							for(float boneIndex : accessor.max){
								maxBones = Math.max(maxBones, (int)boneIndex + 1);
							}
						}else{
							// compute from data
							for(int boneIndex : bonesIndices.get(unit)){
								maxBones = Math.max(maxBones, boneIndex + 1);
							}
						}
					}
					else if(attributeName.startsWith("_")){
						Gdx.app.error("GLTF", "skip unsupported custom attribute: " + attributeName);
					}else{
						throw new GLTFIllegalException("illegal attribute type " + attributeName);
					}
					
					if(rawAttribute){
						glAccessors.add(accessor);
					}
				}
				
				// morph targets
				if(primitive.targets != null){
					int morphTargetCount = primitive.targets.size;
					((NodePlus)node).weights = new WeightVector(morphTargetCount);
					
					for(int t=0 ; t<primitive.targets.size ; t++){
						int unit = t;
						for(Entry<String, Integer> attribute : primitive.targets.get(t)){
							String attributeName = attribute.key;
							int accessorId = attribute.value.intValue();
							GLTFAccessor accessor = dataResolver.getAccessor(accessorId);
							glAccessors.add(accessor);
							
							if(attributeName.equals("POSITION")){
								if(!(GLTFTypes.TYPE_VEC3.equals(accessor.type) && accessor.componentType == GLTFTypes.C_FLOAT)) throw new GLTFIllegalException("illegal morph target position attribute format");
								vertexAttributes.add(new VertexAttribute(PBRVertexAttributes.Usage.PositionTarget, 3, ShaderProgram.POSITION_ATTRIBUTE+unit, unit));
							}else if(attributeName.equals("NORMAL")){
								if(!(GLTFTypes.TYPE_VEC3.equals(accessor.type) && accessor.componentType == GLTFTypes.C_FLOAT)) throw new GLTFIllegalException("illegal morph target normal attribute format");
								vertexAttributes.add(new VertexAttribute(PBRVertexAttributes.Usage.NormalTarget, 3, ShaderProgram.NORMAL_ATTRIBUTE + unit, unit));
							}else if(attributeName.equals("TANGENT")){
								if(!(GLTFTypes.TYPE_VEC3.equals(accessor.type) && accessor.componentType == GLTFTypes.C_FLOAT)) throw new GLTFIllegalException("illegal morph target tangent attribute format");
								vertexAttributes.add(new VertexAttribute(PBRVertexAttributes.Usage.TangentTarget, 3, ShaderProgram.TANGENT_ATTRIBUTE + unit, unit));
							}else{
								throw new GLTFIllegalException("illegal morph target attribute type " + attributeName);
							}
						}
					}
					
				}
				
				int bSize = bonesIndices.size * 4;

				Array<VertexAttribute> bonesAttributes = new Array<VertexAttribute>();
				for(int b=0 ; b<bSize ; b++){
					VertexAttribute boneAttribute = VertexAttribute.BoneWeight(b);
					vertexAttributes.add(boneAttribute);
					bonesAttributes.add(boneAttribute);
				}
				
				if(!hasNormals){
					vertexAttributes.add(VertexAttribute.Normal());
					glAccessors.add(null);
				}
				
				VertexAttributes attributesGroup = new VertexAttributes((VertexAttribute[])vertexAttributes.toArray(VertexAttribute.class));
				
				int vertexFloats = attributesGroup.vertexSize/4;
				
				int maxVertices = glAccessors.first().count;

				float [] vertices = new float [maxVertices * vertexFloats];
				
				for(int b=0 ; b<bSize ; b++){
					VertexAttribute boneAttribute = bonesAttributes.get(b);
					for(int i=0 ; i<maxVertices ; i++){
						vertices[i * vertexFloats + boneAttribute.offset/4] = bonesIndices.get(b/4)[i * 4 + b%4];
						vertices[i * vertexFloats + boneAttribute.offset/4+1] = bonesWeights.get(b/4)[i * 4 + b%4];
					}
				}
				
				for(int i=0 ; i<glAccessors.size ; i++){
					GLTFAccessor glAccessor = glAccessors.get(i);
					VertexAttribute attribute = vertexAttributes.get(i);
					
					
					if(glAccessor == null) continue;
					
					if(glAccessor.bufferView == null){
						throw new GdxRuntimeException("bufferView is null (mesh compression ?)");
					}
					
					GLTFBufferView glBufferView = dataResolver.getBufferView(glAccessor.bufferView);
					
					// not used for now : used for direct mesh ....
					if(glBufferView.target != null){
						if(glBufferView.target == 34963){ // ELEMENT_ARRAY_BUFFER
						}else if(glBufferView.target == 34962){ // ARRAY_BUFFER
						}else{
							throw new GdxRuntimeException("bufferView target unknown : " + glBufferView.target);
						}
					}
					
					FloatBuffer floatBuffer = dataResolver.getBufferFloat(glAccessor);
					
					int attributeFloats = GLTFTypes.accessorStrideSize(glAccessor) / 4;

					// buffer can be interleaved, so vertex stride may be different than vertex size 
					int floatStride = glBufferView.byteStride == null ? attributeFloats : glBufferView.byteStride / 4;
					
					for(int j=0 ; j<glAccessor.count ; j++){
						
						floatBuffer.position(j * floatStride);
						
						int vIndex = j * vertexFloats + attribute.offset/4;
						
						floatBuffer.get(vertices, vIndex, attributeFloats);
					}
				}
				
				if(!hasNormals){
					int posOffset = attributesGroup.getOffset(VertexAttributes.Usage.Position);
					int normalOffset = attributesGroup.getOffset(VertexAttributes.Usage.Normal);
					int stride = attributesGroup.vertexSize / 4;
					
					Vector3 vab = new Vector3();
					Vector3 vac = new Vector3();
					for(int index = 0 ; index<maxIndices ; ){
						
						int vIndexA = indices[index++];
						float ax = vertices[vIndexA * stride + posOffset];
						float ay = vertices[vIndexA * stride + posOffset+1];
						float az = vertices[vIndexA * stride + posOffset+2];
						
						int vIndexB = indices[index++];
						float bx = vertices[vIndexB * stride + posOffset];
						float by = vertices[vIndexB * stride + posOffset+1];
						float bz = vertices[vIndexB * stride + posOffset+2];
						
						int vIndexC = indices[index++];
						float cx = vertices[vIndexC * stride + posOffset];
						float cy = vertices[vIndexC * stride + posOffset+1];
						float cz = vertices[vIndexC * stride + posOffset+2];
						
						vab.set(bx,by,bz).sub(ax,ay,az);
						vac.set(cx,cy,cz).sub(ax,ay,az);
						Vector3 n = vab.crs(vac).nor();
						
						vertices[vIndexA * stride + normalOffset] = n.x;
						vertices[vIndexA * stride + normalOffset+1] = n.y;
						vertices[vIndexA * stride + normalOffset+2] = n.z;
						
						vertices[vIndexB * stride + normalOffset] = n.x;
						vertices[vIndexB * stride + normalOffset+1] = n.y;
						vertices[vIndexB * stride + normalOffset+2] = n.z;
						
						vertices[vIndexC * stride + normalOffset] = n.x;
						vertices[vIndexC * stride + normalOffset+1] = n.y;
						vertices[vIndexC * stride + normalOffset+2] = n.z;
						
					}
				}
				
				Mesh mesh = new Mesh(true, maxVertices, maxIndices, attributesGroup);
				meshes.add(mesh);
				mesh.setVertices(vertices);
				
				if(indices != null){
					mesh.setIndices(indices);
				}
				
				int len = indices == null ? maxVertices : indices.length;
				
				MeshPart meshPart = new MeshPart(glMesh.name, mesh, 0, len, GLTFTypes.mapPrimitiveMode(primitive.mode));
				
				
				NodePartPlus nodePart = new NodePartPlus();
				nodePart.morphTargets = ((NodePlus)node).weights;
				nodePart.meshPart = meshPart;
				if(primitive.material != null){
					nodePart.material = materialLoader.get(primitive.material);
				}else{
					nodePart.material = materialLoader.getDefaultMaterial();
				}
				
				parts.add(nodePart);
			}
			
			meshMap.put(glMesh, parts);
		}
		node.parts.addAll(parts);
	}

	private int parseAttributeUnit(String attributeName) {
		int lastUnderscoreIndex = attributeName.lastIndexOf('_');
		try{
			return Integer.parseInt(attributeName.substring(lastUnderscoreIndex+1));
		}catch(NumberFormatException e){
			throw new GLTFIllegalException("illegal attribute name " + attributeName);
		}
	}

	private short[] loadIndices(GLTFPrimitive primitive, DataResolver dataResolver) {
		short [] indices = null;
		
		if(primitive.indices != null){
			
			GLTFAccessor indicesAccessor = dataResolver.getAccessor(primitive.indices);
			
			if(!indicesAccessor.type.equals(GLTFTypes.TYPE_SCALAR)){
				throw new GLTFIllegalException("indices accessor must be SCALAR but was " + indicesAccessor.type);
			}
				
			int maxIndices = indicesAccessor.count;
			indices = new short[maxIndices];
			
			switch(indicesAccessor.componentType){
			case GLTFTypes.C_UINT:
				{
					IntBuffer intBuffer = dataResolver.getBufferInt(indicesAccessor);
					long maxIndex = 0;
					for(int i=0 ; i<maxIndices ; i++){
						long index = intBuffer.get() & 0xFFFFFFFFL;
						maxIndex = Math.max(index, maxIndex);
						indices[i] = (short)(index);
					}
					// Values used by some graphics APIs as "primitive restart" values are disallowed.
			        // Specifically, the value 65535 (in UINT16) cannot be used as a vertex index. 
					if(maxIndex >= 65535){
						// TODO split
						throw new GLTFUnsupportedException("high index detected: " + maxIndex + ". Not supported");
					}
				}
				break;
			case GLTFTypes.C_USHORT:
			case GLTFTypes.C_SHORT:
				dataResolver.getBufferShort(indicesAccessor).get(indices);
				break;
			case GLTFTypes.C_UBYTE:
				ByteBuffer byteBuffer = dataResolver.getBufferByte(indicesAccessor);
				for(int i=0 ; i<maxIndices ; i++){
					indices[i] = (short)(byteBuffer.get() & 0xFF);
				}
				break;
			default:
				throw new GLTFIllegalException("illegal componentType " + indicesAccessor.componentType);
			}
					
		}
		
		return indices;
	}

	public int getMaxBones() {
		return maxBones;
	}

	public Array<? extends Mesh> getMeshes() {
		return meshes;
	}

}
