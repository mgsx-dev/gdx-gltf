package net.mgsx.gltf.loaders.shared.geometry;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
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
				
				final int glPrimitiveType = GLTFTypes.mapPrimitiveMode(primitive.mode);
				
				// material
				Material material;
				if(primitive.material != null){
					material = materialLoader.get(primitive.material);
				}else{
					material = materialLoader.getDefaultMaterial();
				}
				
				// vertices
				Array<VertexAttribute> vertexAttributes = new Array<VertexAttribute>();
				Array<GLTFAccessor> glAccessors = new Array<GLTFAccessor>();
				
				Array<int[]> bonesIndices = new Array<int[]>();
				Array<float[]> bonesWeights = new Array<float[]>();
				
				boolean hasNormals = false;
				boolean hasTangent = false;
				
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
						hasTangent = true;
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
				
				// add missing vertex attributes (normals and tangent)
				boolean computeNormals = false;
				boolean computeTangents = false;
				VertexAttribute normalMapUVs = null;
				if(glPrimitiveType == GL20.GL_TRIANGLES){
					if(!hasNormals){
						vertexAttributes.add(VertexAttribute.Normal());
						glAccessors.add(null);
						computeNormals = true;
					}
					if(!hasTangent){
						// tangent is only needed when normal map is used
						PBRTextureAttribute normalMap = material.get(PBRTextureAttribute.class, PBRTextureAttribute.NormalTexture);
						if(normalMap != null){
							vertexAttributes.add(new VertexAttribute(Usage.Tangent, 4, ShaderProgram.TANGENT_ATTRIBUTE));
							glAccessors.add(null);
							computeTangents = true;
							for(VertexAttribute attribute : vertexAttributes){
								if(attribute.usage == Usage.TextureCoordinates && attribute.unit == normalMap.uvIndex){
									normalMapUVs = attribute;
								}
							}
							if(normalMapUVs == null) throw new GLTFIllegalException("UVs not found for normal map");
						}
					}
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
				
				// indices
				if(primitive.indices != null){
					
					GLTFAccessor indicesAccessor = dataResolver.getAccessor(primitive.indices);
					
					if(!indicesAccessor.type.equals(GLTFTypes.TYPE_SCALAR)){
						throw new GLTFIllegalException("indices accessor must be SCALAR but was " + indicesAccessor.type);
					}
						
					int maxIndices = indicesAccessor.count;
					
					switch(indicesAccessor.componentType){
					case GLTFTypes.C_UINT:
						{
							Gdx.app.error("GLTF", "integer indices partially supported, mesh will be split");
							Gdx.app.error("GLTF", "splitting mesh: " + maxVertices + " vertices, " + maxIndices + " indices.");

							int verticesPerPrimitive;
							if(glPrimitiveType == GL20.GL_TRIANGLES){
								verticesPerPrimitive = 3;
							}else if(glPrimitiveType == GL20.GL_LINES){
								verticesPerPrimitive = 2;
							}else{
								throw new GLTFUnsupportedException("integer indices only supported for triangles or lines");
							}
							
							int [] indices = new int[maxIndices];
							dataResolver.getBufferInt(indicesAccessor).get(indices);
							
							Array<float[]> splitVertices = new Array<float[]>();
							Array<short[]> splitIndices = new Array<short[]>();
							
							MeshSpliter.split(splitVertices, splitIndices, vertices, attributesGroup, indices, verticesPerPrimitive);
							
							int stride = attributesGroup.vertexSize / 4;
							int groups = splitIndices.size;
							int totalVertices = 0;
							int totalIndices = 0;
							for(int i=0 ; i<groups ; i++){
								float[] groupVertices = splitVertices.get(i);
								short[] groupIndices = splitIndices.get(i);
								int groupVertexCount = groupVertices.length / stride;
								
								totalVertices += groupVertexCount;
								totalIndices += groupIndices.length;
								
								Gdx.app.error("GLTF", "generate mesh: " + groupVertexCount + " vertices, " + groupIndices.length + " indices.");
								
								generateParts(node, parts, material, glMesh.name, groupVertices, groupVertexCount, groupIndices, attributesGroup, glPrimitiveType, computeNormals, computeTangents, normalMapUVs);
							}
							Gdx.app.error("GLTF", "mesh split: " + parts.size + " meshes generated: " + totalVertices + " vertices, " + totalIndices + " indices.");
						}
						break;
					case GLTFTypes.C_USHORT:
					case GLTFTypes.C_SHORT:
					{
						short [] indices = new short[maxIndices];
						dataResolver.getBufferShort(indicesAccessor).get(indices);
						generateParts(node, parts, material, glMesh.name, vertices, maxVertices, indices, attributesGroup, glPrimitiveType, computeNormals, computeTangents, normalMapUVs);
						break;
					}
					case GLTFTypes.C_UBYTE:
					{
						short [] indices = new short[maxIndices];
						ByteBuffer byteBuffer = dataResolver.getBufferByte(indicesAccessor);
						for(int i=0 ; i<maxIndices ; i++){
							indices[i] = (short)(byteBuffer.get() & 0xFF);
						}
						generateParts(node, parts, material, glMesh.name, vertices, maxVertices, indices, attributesGroup, glPrimitiveType, computeNormals, computeTangents, normalMapUVs);
						break;
					}
					default:
						throw new GLTFIllegalException("illegal componentType " + indicesAccessor.componentType);
					}
				}else{
					// non indexed mesh
					generateParts(node, parts, material, glMesh.name, vertices, maxVertices, null, attributesGroup, glPrimitiveType, computeNormals, computeTangents, normalMapUVs);
				}
			}
			meshMap.put(glMesh, parts);
		}
		node.parts.addAll(parts);
	}

	private void generateParts(Node node, Array<NodePart> parts, Material material, String id, float[] vertices, int vertexCount, short[] indices, VertexAttributes attributesGroup, int glPrimitiveType, boolean computeNormals, boolean computeTangents, VertexAttribute normalMapUVs) {

		if(computeNormals || computeTangents){
			if(computeNormals && computeTangents) Gdx.app.log("GLTF", "compute normals and tangents for primitive " + id);
			else if(computeTangents) Gdx.app.log("GLTF", "compute tangents for primitive " + id);
			else Gdx.app.log("GLTF", "compute normals for primitive " + id);
			MeshTangentSpaceGenerator.computeTangentSpace(vertices, indices, attributesGroup, computeNormals, computeTangents, normalMapUVs);
		}
		
		Mesh mesh = new Mesh(true, vertexCount, indices == null ? 0 : indices.length, attributesGroup);
		meshes.add(mesh);
		mesh.setVertices(vertices);
		
		if(indices != null){
			mesh.setIndices(indices);
		}
		
		int len = indices == null ? vertexCount : indices.length;
		
		MeshPart meshPart = new MeshPart(id, mesh, 0, len, glPrimitiveType);
		
		
		NodePartPlus nodePart = new NodePartPlus();
		nodePart.morphTargets = ((NodePlus)node).weights;
		nodePart.meshPart = meshPart;
		nodePart.material = material;
		parts.add(nodePart);
		
	}

	private int parseAttributeUnit(String attributeName) {
		int lastUnderscoreIndex = attributeName.lastIndexOf('_');
		try{
			return Integer.parseInt(attributeName.substring(lastUnderscoreIndex+1));
		}catch(NumberFormatException e){
			throw new GLTFIllegalException("illegal attribute name " + attributeName);
		}
	}

	public int getMaxBones() {
		return maxBones;
	}

	public Array<? extends Mesh> getMeshes() {
		return meshes;
	}

}
