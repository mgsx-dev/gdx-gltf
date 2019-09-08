package net.mgsx.gltf.loaders.shared.geometry;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
				
				int [][] bonesIndices = {null, null};
				float [][] bonesWeights = {null, null};
				
				boolean hasNormals = false;
				
				for(Entry<String, Integer> attribute : primitive.attributes){
					String attributeName = attribute.key;
					int accessorId = attribute.value;
					GLTFAccessor accessor = dataResolver.getAccessor(accessorId);
					boolean rawAttribute = true;
					
					if(attributeName.equals("POSITION")){
						vertexAttributes.add(VertexAttribute.Position());
					}else if(attributeName.equals("NORMAL")){
						vertexAttributes.add(VertexAttribute.Normal());
						hasNormals = true;
					}else if(attributeName.equals("TANGENT")){
						vertexAttributes.add(new VertexAttribute(Usage.Tangent, 4, ShaderProgram.TANGENT_ATTRIBUTE));
					}else if(attributeName.startsWith("TEXCOORD_")){
						int unit = Integer.parseInt(attributeName.substring("TEXCOORD_".length()));
						vertexAttributes.add(VertexAttribute.TexCoords(unit));
					}else if(attributeName.startsWith("COLOR_")){
						int unit = Integer.parseInt(attributeName.substring("COLOR_".length()));
						if(unit == 0){
							vertexAttributes.add(VertexAttribute.ColorUnpacked());
						}else{
							vertexAttributes.add(new VertexAttribute(VertexAttributes.Usage.Generic, 4, ShaderProgram.COLOR_ATTRIBUTE + unit));
						}
					}else if(attributeName.startsWith("WEIGHTS_")){
						rawAttribute = false;
						// TODO could be var sizes ...
						int numComponentsBytes = 0;
						if("VEC4".equals(accessor.type)){
							numComponentsBytes = 4;
						}else{
							throw new GdxRuntimeException("type not known yet : " + accessor.type);
						}
						if(accessor.componentType == 5126){ // float
							numComponentsBytes *= 4;
						}else{
							throw new GdxRuntimeException("type not known yet : " + accessor.componentType);
						}
						int unit = Integer.parseInt(attributeName.substring("WEIGHTS_".length()));
						if(numComponentsBytes == 16){
							bonesWeights[unit] = dataResolver.readBufferFloat(accessorId);
							// vertexAttributes.add(VertexAttribute.BoneWeight(unit));
						}else if(numComponentsBytes % 4 == 0){
							throw new GdxRuntimeException("NYI !!");
							// vertexAttributes.add(new VertexAttribute(VertexAttributes.Usage.Generic, numComponentsBytes/4, "a_weight_gltf" + unit));
						}else{
							throw new GdxRuntimeException("bad alignement " + numComponentsBytes + " bytes");
						}
					}else if(attributeName.startsWith("JOINTS_")){
						rawAttribute = false;
						
						if(!"VEC4".equals(accessor.type)){
							throw new GdxRuntimeException("joint must be VEC4 found " + accessor.type);
						}
						int unit = Integer.parseInt(attributeName.substring("JOINTS_".length()));
						if(accessor.componentType == 5121){ // unsigned byte
							bonesIndices[unit] = dataResolver.readBufferUByte(accessorId);
						}else if(accessor.componentType == 5123){ // unsigned short
							bonesIndices[unit] = dataResolver.readBufferUShort(accessorId);
						}else{
							throw new GdxRuntimeException("type not supported : " + accessor.componentType);
						}
						if(accessor.max != null){
							for(float boneIndex : accessor.max){
								maxBones = Math.max(maxBones, (int)boneIndex + 1);
							}
						}else{
							// compute from data
							for(int [] ids : bonesIndices){
								if(ids != null){
									for(int boneIndex : ids){
										maxBones = Math.max(maxBones, boneIndex + 1);
									}
								}
							}
						}
					}else{
						throw new GdxRuntimeException("unsupported attribute type " + attributeName);
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
						for(Entry<String, ?> attribute : primitive.targets.get(t)){
							String attributeName = attribute.key;
							int accessorId = ((Float)attribute.value).intValue(); // XXX Json issue !?
							GLTFAccessor accessor = dataResolver.getAccessor(accessorId);
							glAccessors.add(accessor);
							
							if(attributeName.equals("POSITION")){
								vertexAttributes.add(new VertexAttribute(PBRVertexAttributes.Usage.PositionTarget, 3, ShaderProgram.POSITION_ATTRIBUTE+unit, unit));
							}else if(attributeName.equals("NORMAL")){
								vertexAttributes.add(new VertexAttribute(PBRVertexAttributes.Usage.NormalTarget, 3, ShaderProgram.NORMAL_ATTRIBUTE + unit, unit));
							}else if(attributeName.equals("TANGENT")){
								vertexAttributes.add(new VertexAttribute(PBRVertexAttributes.Usage.TangentTarget, 3, ShaderProgram.TANGENT_ATTRIBUTE + unit, unit));
							}else{
								throw new GdxRuntimeException("unsupported target attribute type " + attributeName);
							}
						}
					}
					
				}
				
				int bSize = 0;
				if(bonesIndices[0] != null) bSize = 4;
				if(bonesIndices[1] != null) bSize = 8;

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
				// TODO no need to go futher if maxVertices > 32767 ...

				float [] vertices = new float [maxVertices * vertexFloats];
				
				for(int b=0 ; b<bSize ; b++){
					VertexAttribute boneAttribute = bonesAttributes.get(b);
					for(int i=0 ; i<maxVertices ; i++){
						vertices[i * vertexFloats + boneAttribute.offset/4] = bonesIndices[b/4][i * 4 + b%4];
						vertices[i * vertexFloats + boneAttribute.offset/4+1] = bonesWeights[b/4][i * 4 + b%4];
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
					
					// buffer can be interleaved, so we 
					// in some cases we have to compute vertex stride
					int floatStride = (glBufferView.byteStride == null ? GLTFTypes.accessorStrideSize(glAccessor) : glBufferView.byteStride) / 4;
					
					for(int j=0 ; j<glAccessor.count ; j++){
						
						floatBuffer.position(j * floatStride);
						
						int vIndex = j * vertexFloats + attribute.offset/4;
						
						floatBuffer.get(vertices, vIndex, attribute.numComponents);
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

	private short[] loadIndices(GLTFPrimitive primitive, DataResolver dataResolver) {
		short [] indices = null;
		
		if(primitive.indices != null){
			
			GLTFAccessor indicesAccessor = dataResolver.getAccessor(primitive.indices);
			
			// Accessor Element Size
			// https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#accessor-element-size
			// 5120 : byte
			// 5121 : ubyte
			// 5122 : short
			// 5123 : ushort
			// 5125 : uint
			// 5126 : float (not in this case)
			
			if(indicesAccessor.type.equals("SCALAR")){
				
				int maxIndices = indicesAccessor.count; // glIndicesBuffer.byteLength / 2;
				indices = new short[maxIndices];
				
				switch(indicesAccessor.componentType){
				case 5125: // unsigned int
					IntBuffer intBuffer = dataResolver.getBufferInt(indicesAccessor);
					for(int i=0 ; i<maxIndices ; i++){
						long index = intBuffer.get() & 0xFFFFFFFF;
						if(index > 1<<15){
							throw new GdxRuntimeException("index too big : " + index);
						}
						indices[i] = (short)(index);
					}
					break;
				case 5123: // unsigned short
				case 5122: // short
					dataResolver.getBufferShort(indicesAccessor).get(indices);
					
					int maxIndex;
					if(indicesAccessor.max != null){
						maxIndex = (int)indicesAccessor.max[0];
					}else{
						maxIndex = 0;
						for(short i : indices){
							maxIndex = Math.max(maxIndex, i & 0xFFFF);
						}
					}
					
					if(maxIndex >= 1<<15){
						throw new GdxRuntimeException("index too big : " + maxIndex);
					}
					
					break;
				case 5121: // unsigned bytes
					ByteBuffer byteBuffer = dataResolver.getBufferByte(indicesAccessor);
					for(int i=0 ; i<maxIndices ; i++){
						indices[i] = (short)(byteBuffer.get() & 0xFF);
					}
					break;
				default:
					throw new GdxRuntimeException("unsupported componentType " + indicesAccessor.componentType);
				}
				
				
			}else{
				throw new GdxRuntimeException("indices accessor must be SCALAR but was " + indicesAccessor.type);
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
