package net.mgsx.gltf.loaders;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.animation.GLTFAnimation;
import net.mgsx.gltf.data.animation.GLTFAnimationChannel;
import net.mgsx.gltf.data.animation.GLTFAnimationSampler;
import net.mgsx.gltf.data.camera.GLTFCamera;
import net.mgsx.gltf.data.data.GLTFAccessor;
import net.mgsx.gltf.data.data.GLTFBufferView;
import net.mgsx.gltf.data.extensions.KHRMaterialsPBRSpecularGlossiness;
import net.mgsx.gltf.data.extensions.KHRTextureTransform;
import net.mgsx.gltf.data.geometry.GLTFMesh;
import net.mgsx.gltf.data.geometry.GLTFPrimitive;
import net.mgsx.gltf.data.material.GLTFMaterial;
import net.mgsx.gltf.data.material.GLTFpbrMetallicRoughness;
import net.mgsx.gltf.data.scene.GLTFNode;
import net.mgsx.gltf.data.scene.GLTFScene;
import net.mgsx.gltf.data.scene.GLTFSkin;
import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.data.texture.GLTFSampler;
import net.mgsx.gltf.data.texture.GLTFTexture;
import net.mgsx.gltf.data.texture.GLTFTextureInfo;
import net.mgsx.gltf.scene3d.animation.NodeAnimationHack;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.model.NodePartPlus;
import net.mgsx.gltf.scene3d.model.NodePlus;
import net.mgsx.gltf.scene3d.model.WeightVector;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

// TODO simplify this class with some external herlpers ...

abstract public class GLTFLoaderBase implements Disposable {

	private ObjectMap<Integer, Node> nodeMap = new ObjectMap<Integer, Node>();
	private ObjectMap<Integer, Array<NodePart>> meshMap = new ObjectMap<Integer, Array<NodePart>>();
	
	protected ObjectMap<Integer, ByteBuffer> bufferMap = new ObjectMap<Integer, ByteBuffer>();
	
	private ObjectMap<Integer, Material> materialMap = new ObjectMap<Integer, Material>();
	
	private ObjectMap<Integer, Texture> textures = new ObjectMap<Integer, Texture>();
	private Array<Pixmap> pixmaps = new Array<Pixmap>();
	
	protected GLTF glModel;
	
	private SceneAsset model;
	
	private Material defaultMaterial;
	
	/**
	 * Expose glModel to user code in order to retreive some user define properties {@link net.mgsx.gltf.model.GLTFExtras}
	 * @return parsed GLTF root object
	 */
	public GLTF getGlModel() {
		return glModel;
	}
	
	protected SceneAsset loadInternal(){
		try{
			
			model = new SceneAsset();
			
			// prerequists
			if(glModel.extensionsRequired != null){
				for(String extension : glModel.extensionsRequired){
					if(KHRMaterialsPBRSpecularGlossiness.EXT.equals(extension)){
					}else if(KHRTextureTransform.EXT.equals(extension)){
					}else{
						throw new GdxRuntimeException("Extension " + extension + " required but not supported");
					}
				}
			}
			
			// load deps from lower to higher
			
			loadPixmaps();
			loadTextures();
			loadMaterials();
			
			loadCameras();
			loadScenes();
			loadAnimations();
			
			model.scene = model.scenes.get(glModel.scene);
			
			return model;
		}catch(RuntimeException e){
			dispose();
			throw e;
		}
	}
	
	@Override
	public void dispose() {
		for(Pixmap pixmap : pixmaps){
			pixmap.dispose();
		}
		for(Texture texture : model.textures){
			texture.dispose();
		}
		for(Model model : model.scenes){
			model.dispose();
		}
	}

	private void loadScenes() {
		for(int i=0 ; i<glModel.scenes.size ; i++)
		{
			model.scenes.add(loadScene(glModel.scenes.get(i)));
		}
	}

	private void loadCameras() {
		if(glModel.cameras != null){
			for(GLTFCamera glCamera : glModel.cameras){
				model.cameras.add(GLTFTypes.map(glCamera));
			}
		}
	}
	
	private void loadAnimations() 
	{
		if(glModel.animations == null) return;
		for(int i=0 ; i<glModel.animations.size ; i++){
			GLTFAnimation glAnimation = glModel.animations.get(i);
			
			ObjectMap<Node, NodeAnimation> animMap = new ObjectMap<Node, NodeAnimation>();
			
			Animation animation = new Animation();
			animation.id = glAnimation.name == null ? "animation" + i : glAnimation.name;
			
			for(GLTFAnimationChannel glChannel : glAnimation.channels){
				GLTFAnimationSampler glSampler = glAnimation.samplers.get(glChannel.sampler);
				Node node = nodeMap.get(glChannel.target.node);
				
				NodeAnimation nodeAnimation = animMap.get(node);
				if(nodeAnimation == null){
					nodeAnimation = new NodeAnimationHack();
					nodeAnimation.node = node;
					animMap.put(node, nodeAnimation);
					animation.nodeAnimations.add(nodeAnimation);
				}
				
				float[] inputData = readBufferFloat(glSampler.input);
				float[] outputData = readBufferFloat(glSampler.output);
				
				Interpolation interpolation = GLTFTypes.mapInterpolation(glSampler.interpolation);
				// TODO store interpolation on AnimationNode (for individual channels) and use it in animations ...
				
				GLTFAccessor inputAccessor = glModel.accessors.get(glSampler.input);
				animation.duration = Math.max(animation.duration, inputAccessor.max[0]);
				
				String property = glChannel.target.path;
				if("translation".equals(property)){
					nodeAnimation.translation = new Array<NodeKeyframe<Vector3>>();
					// copy first frame if not at zero time
					if(inputData[0] > 0){
						nodeAnimation.translation.add(new NodeKeyframe<Vector3>(0, GLTFTypes.map(new Vector3(), outputData, 0)));
					}
					for(int k=0 ; k<inputData.length ; k++){
						nodeAnimation.translation.add(new NodeKeyframe<Vector3>(inputData[k], GLTFTypes.map(new Vector3(), outputData, k*3)));
					}
				}else if("rotation".equals(property)){
					nodeAnimation.rotation = new Array<NodeKeyframe<Quaternion>>();
					// copy first frame if not at zero time
					if(inputData[0] > 0){
						nodeAnimation.rotation.add(new NodeKeyframe<Quaternion>(0, GLTFTypes.map(new Quaternion(), outputData, 0)));
					}
					for(int k=0 ; k<inputData.length ; k++){
						nodeAnimation.rotation.add(new NodeKeyframe<Quaternion>(inputData[k], GLTFTypes.map(new Quaternion(), outputData, k*4)));
					}
				}else if("scale".equals(property)){
					nodeAnimation.scaling = new Array<NodeKeyframe<Vector3>>();
					// copy first frame if not at zero time
					if(inputData[0] > 0){
						nodeAnimation.scaling.add(new NodeKeyframe<Vector3>(0, GLTFTypes.map(new Vector3(), outputData, 0)));
					}
					for(int k=0 ; k<inputData.length ; k++){
						nodeAnimation.scaling.add(new NodeKeyframe<Vector3>(inputData[k], GLTFTypes.map(new Vector3(), outputData, k*3)));
					}
				}else if("weights".equals(property)){
					NodeAnimationHack np = (NodeAnimationHack)nodeAnimation;
					int nbWeights = ((NodePlus)node).weights.count;
					np.weights = new Array<NodeKeyframe<WeightVector>>();
					// copy first frame if not at zero time
					if(inputData[0] > 0){
						np.weights.add(new NodeKeyframe<WeightVector>(0, GLTFTypes.map(new WeightVector(nbWeights), outputData, 0)));
					}
					for(int k=0 ; k<inputData.length ; k++){
						np.weights.add(new NodeKeyframe<WeightVector>(inputData[k], GLTFTypes.map(new WeightVector(nbWeights), outputData, k*nbWeights)));
					}
				}else{
					throw new GdxRuntimeException("unsupported " + property);
				}
			}
			
			model.animations.add(animation);
			
			// XXX don't know where the animation are ...
			for(Model scene : model.scenes){
				scene.animations.add(animation);
			}
			
		}
		
	}

	private float[] readBufferFloat(int accessorID) {
		GLTFAccessor accessor = glModel.accessors.get(accessorID);
		GLTFBufferView bufferView = glModel.bufferViews.get(accessor.bufferView);
		ByteBuffer bytes = bufferMap.get(bufferView.buffer);
		bytes.position(bufferView.byteOffset + accessor.byteOffset);
		
		FloatBuffer floatBuffer = bytes.asFloatBuffer();
		float [] data = new float[GLTFTypes.accessorSize(accessor) / 4];
		floatBuffer.get(data);
		return data;
	}
	
	private int[] readBufferUByte(int accessorID) {
		GLTFAccessor accessor = glModel.accessors.get(accessorID);
		GLTFBufferView bufferView = glModel.bufferViews.get(accessor.bufferView);
		ByteBuffer bytes = bufferMap.get(bufferView.buffer);
		bytes.position(bufferView.byteOffset + accessor.byteOffset);
		int [] data = new int[GLTFTypes.accessorSize(accessor)];
		for(int i=0 ; i<data.length ; i++){
			data[i] = bytes.get() & 0xFF;
		}
		return data;
	}
	
	private int[] readBufferUShort(int accessorID) {
		GLTFAccessor accessor = glModel.accessors.get(accessorID);
		GLTFBufferView bufferView = glModel.bufferViews.get(accessor.bufferView);
		ByteBuffer bytes = bufferMap.get(bufferView.buffer);
		bytes.position(bufferView.byteOffset + accessor.byteOffset);
		ShortBuffer shorts = bytes.asShortBuffer();
		int [] data = new int[GLTFTypes.accessorSize(accessor)/2];
		for(int i=0 ; i<data.length ; i++){
			data[i] = shorts.get() & 0xFFFF;
		}
		return data;
	}
	
	private void loadPixmaps() {
		if(glModel.images != null){
			for(int i=0 ; i<glModel.images.size ; i++){
				GLTFImage glImage = glModel.images.get(i);
				Pixmap pixmap;
				try{
					pixmap = loadPixmap(glImage);
				}catch(GdxRuntimeException e){
					pixmap = null;
					System.err.println("cannot load pixmap " + glImage.uri);
				}
				pixmaps.add(pixmap);
			}
		}
		
	}
	
	abstract protected Pixmap loadPixmap(GLTFImage glImage);

	private void loadTextures() {
		if(glModel.textures != null){
			for(int i=0 ; i<glModel.textures.size ; i++){
				GLTFTexture glTexture = glModel.textures.get(i);
				Pixmap pixmap = pixmaps.get(glTexture.source);
				boolean useMipMaps = false;
				if(glTexture.sampler != null){
					GLTFSampler sampler = glModel.samplers.get(glTexture.sampler);
					if(GLTFTypes.isMipMapFilter(sampler)){
						useMipMaps = true;
					}
				}
				if(pixmap != null){
					Texture texture = new Texture(pixmap, useMipMaps);
					textures.put(i, texture);
					model.textures.add(texture);
				}
			}
		}
		
	}

	private void loadMaterials() 
	{
		// TODO default material ... use base color instead diffuse
		defaultMaterial = new Material();
		defaultMaterial.set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));
		
		if(glModel.materials != null){
			for(int i=0 ; i<glModel.materials.size ; i++){
				GLTFMaterial glMaterial = glModel.materials.get(i);
				Material material = new Material();
				if(glMaterial.name != null) material.id = glMaterial.name;
				
				if(glMaterial.emissiveFactor != null){
					material.set(new ColorAttribute(ColorAttribute.Emissive, GLTFTypes.mapColor(glMaterial.emissiveFactor, Color.BLACK)));
				}
				
				if(glMaterial.emissiveTexture != null){
					material.set(getTexureMap(PBRTextureAttribute.EmissiveTexture, glMaterial.emissiveTexture));
				}
				
				if(glMaterial.doubleSided == Boolean.TRUE){
					material.set(IntAttribute.createCullFace(0)); // 0 to disable culling
				}
				
				if(glMaterial.normalTexture != null){
					material.set(getTexureMap(PBRTextureAttribute.NormalTexture, glMaterial.normalTexture));
					material.set(PBRFloatAttribute.createNormalScale(glMaterial.normalTexture.scale));
				}
				
				if(glMaterial.occlusionTexture != null){
					material.set(getTexureMap(PBRTextureAttribute.OcclusionTexture, glMaterial.occlusionTexture));
					material.set(PBRFloatAttribute.createOcclusionStrength(glMaterial.occlusionTexture.strength));
				}
				
				if("OPAQUE".equals(glMaterial.alphaMode)){
					// nothing to do
				}else if("MASK".equals(glMaterial.alphaMode)){ 
					float value = glMaterial.alphaCutoff == null ? 0.5f : glMaterial.alphaCutoff;
					material.set(FloatAttribute.createAlphaTest(value));
					material.set(new BlendingAttribute()); // necessary
				}else if("BLEND".equals(glMaterial.alphaMode)){
					material.set(new BlendingAttribute());
				}else if(glMaterial.alphaMode != null){
					throw new GdxRuntimeException("unknow alpha mode : " + glMaterial.alphaMode);
				}
				
				if(glMaterial.pbrMetallicRoughness != null){
					GLTFpbrMetallicRoughness p = glMaterial.pbrMetallicRoughness;
					
					material.set(new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, GLTFTypes.mapColor(p.baseColorFactor, Color.WHITE)));

					material.set(PBRFloatAttribute.createMetallic(p.metallicFactor));
					material.set(PBRFloatAttribute.createRoughness(p.roughnessFactor));
					
					if(p.metallicRoughnessTexture != null){
						material.set(getTexureMap(PBRTextureAttribute.MetallicRoughnessTexture, p.metallicRoughnessTexture));
					}
					
					if(p.baseColorTexture != null){
						material.set(getTexureMap(PBRTextureAttribute.BaseColorTexture, p.baseColorTexture));
					}
				}
				
				// can have both PBR base and ext
				if(glMaterial.extensions != null){
					{
						KHRMaterialsPBRSpecularGlossiness ext = glMaterial.extensions.get(KHRMaterialsPBRSpecularGlossiness.class, KHRMaterialsPBRSpecularGlossiness.EXT);
						if(ext != null){
							material.set(new ColorAttribute(ColorAttribute.Diffuse, GLTFTypes.mapColor(ext.diffuseFactor, Color.WHITE)));
							material.set(new ColorAttribute(ColorAttribute.Specular, GLTFTypes.mapColor(ext.specularFactor, Color.WHITE)));
							
							// TODO not sure how to map normalized gloss to exponent ...
							material.set(new FloatAttribute(FloatAttribute.Shininess, MathUtils.lerp(1, 100, ext.glossinessFactor)));
							if(ext.diffuseTexture != null){
								// TODO use another attribe : DiffuseTexture
								material.set(getTexureMap(PBRTextureAttribute.Diffuse, ext.diffuseTexture));
							}
							if(ext.specularGlossinessTexture != null){
								// TODO use another attribute : SpecularTexture
								material.set(getTexureMap(PBRTextureAttribute.Specular, ext.specularGlossinessTexture));
							}
						}
					}
				}
				
				materialMap.put(i, material);
			}
		}
	}
	
	private PBRTextureAttribute getTexureMap(long type, GLTFTextureInfo glMap) {
		
		GLTFTexture glTexture = glModel.textures.get(glMap.index);
		Texture texture = textures.get(glTexture.source);
		
		TextureDescriptor<Texture> textureDescriptor = new TextureDescriptor<Texture>();

		if(glTexture.sampler != null){
			GLTFSampler glSampler = glModel.samplers.get(glTexture.sampler);
			GLTFTypes.mapTextureSampler(textureDescriptor, glSampler);
		}
		textureDescriptor.texture = texture;
		
		PBRTextureAttribute attribute = new PBRTextureAttribute(type, textureDescriptor);
		attribute.uvIndex = glMap.texCoord;
		
		if(glMap.extensions != null){
			{
				KHRTextureTransform ext = glMap.extensions.get(KHRTextureTransform.class, KHRTextureTransform.EXT);
				if(ext != null){
					attribute.offsetU = ext.offset[0];
					attribute.offsetV = ext.offset[1];
					attribute.scaleU = ext.scale[0];
					attribute.scaleV = ext.scale[1];
					attribute.rotationUV = ext.rotation;
					if(ext.texCoord != null){
						attribute.uvIndex = ext.texCoord;
					}
				}
			}
		}
		
		return attribute;
	}


	private Model loadScene(GLTFScene gltfScene) 
	{
		Model model = new Model();
		
		for(int id : gltfScene.nodes){
			model.nodes.add(getNode(id));
		}
		
		return model;
	}

	private Node getNode(int id) 
	{
		Node node = nodeMap.get(id);
		if(node == null){
			node = new NodePlus();
			nodeMap.put(id, node);
			
			GLTFNode glNode = glModel.nodes.get(id);
			
			if(glNode.matrix != null){
				Matrix4 matrix = new Matrix4(glNode.matrix);
				matrix.getTranslation(node.translation);
				matrix.getScale(node.scale);
				matrix.getRotation(node.rotation);
			}else{
				if(glNode.translation != null){
					GLTFTypes.map(node.translation, glNode.translation);
				}
				if(glNode.rotation != null){
					GLTFTypes.map(node.rotation, glNode.rotation);
				}
				if(glNode.scale != null){
					GLTFTypes.map(node.scale, glNode.scale);
				}
			}
			
			node.id = "glNode " + id;
			
			if(glNode.children != null){
				for(int childId : glNode.children){
					node.addChild(getNode(childId));
				}
			}
			
			Array<Matrix4> ibms = new Array<Matrix4>();
			Array<Integer> joints = new Array<Integer>();
			
			if(glNode.skin != null){
				GLTFSkin glSkin = glModel.skins.get(glNode.skin);
				int bonesCount = glSkin.joints.size;
				GLTFAccessor glAccessorIBM = glModel.accessors.get(glSkin.inverseBindMatrices);
				GLTFBufferView glBufferViewIBM = glModel.bufferViews.get(glAccessorIBM.bufferView);
				ByteBuffer buffer = bufferMap.get(glBufferViewIBM.buffer);
				buffer.position(glBufferViewIBM.byteOffset + glAccessorIBM.byteOffset);
				FloatBuffer floatBuffer = buffer.asFloatBuffer();
				
				for(int i=0 ; i<bonesCount ; i++){
					float [] matrixData = new float[16];
					floatBuffer.get(matrixData);
					ibms.add(new Matrix4(matrixData));
				}
				joints.addAll(glSkin.joints);
			}
			
			if(glNode.mesh != null){
				getMeshParts(node, glNode.mesh, joints, ibms);
			}
			
			if(glNode.camera != null){
				model.cameraMap.put(node.id, glNode.camera);
			}
			
		}
		return node;
	}

	private void getMeshParts(Node node, Integer meshId, Array<Integer> joints, Array<Matrix4> ibms) 
	{
		Array<NodePart> parts = meshMap.get(meshId);
		if(parts == null){
			parts = new Array<NodePart>();
			GLTFMesh glMesh = glModel.meshes.get(meshId);
			
			for(GLTFPrimitive primitive : glMesh.primitives){
				
				// indices
				short [] indices = loadIndices(primitive);
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
					GLTFAccessor accessor = glModel.accessors.get(accessorId);
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
							bonesWeights[unit] = readBufferFloat(accessorId);
							// vertexAttributes.add(VertexAttribute.BoneWeight(unit));
						}else if(numComponentsBytes % 4 == 0){
							throw new GdxRuntimeException("NYI !!");
							// vertexAttributes.add(new VertexAttribute(VertexAttributes.Usage.Generic, numComponentsBytes/4, "a_weight_gltf" + unit));
						}else{
							throw new GdxRuntimeException("bad alignement " + numComponentsBytes + " bytes");
						}
					}else if(attributeName.startsWith("JOINTS_")){
						rawAttribute = false;
						for(float boneIndex : accessor.max){
							model.maxBones = Math.max(model.maxBones, (int)boneIndex + 1);
						}
						
						if(!"VEC4".equals(accessor.type)){
							throw new GdxRuntimeException("joint must be VEC4 found " + accessor.type);
						}
						int unit = Integer.parseInt(attributeName.substring("JOINTS_".length()));
						if(accessor.componentType == 5121){ // unsigned byte
							bonesIndices[unit] = readBufferUByte(accessorId);
						}else if(accessor.componentType == 5123){ // unsigned short
							bonesIndices[unit] = readBufferUShort(accessorId);
						}else{
							throw new GdxRuntimeException("type not supported : " + accessor.componentType);
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
							GLTFAccessor accessor = glModel.accessors.get(accessorId);
							glAccessors.add(accessor);
							
							if(attributeName.equals("POSITION")){
								vertexAttributes.add(new VertexAttribute(VertexAttributes.Usage.Generic, 3, ShaderProgram.POSITION_ATTRIBUTE + unit));
							}else if(attributeName.equals("NORMAL")){
								vertexAttributes.add(new VertexAttribute(VertexAttributes.Usage.Generic, 3, ShaderProgram.NORMAL_ATTRIBUTE + unit));
							}else if(attributeName.equals("TANGENT")){
								vertexAttributes.add(new VertexAttribute(VertexAttributes.Usage.Generic, 3, ShaderProgram.TANGENT_ATTRIBUTE + unit));
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
					
					GLTFBufferView glBufferView = glModel.bufferViews.get(glAccessor.bufferView);
					
					// not used for now : used for direct mesh ....
					if(glBufferView.target != null){
						if(glBufferView.target == 34963){ // ELEMENT_ARRAY_BUFFER
						}else if(glBufferView.target == 34962){ // ARRAY_BUFFER
						}else{
							throw new GdxRuntimeException("bufferView target unknown : " + glBufferView.target);
						}
					}
					
					ByteBuffer buffer = bufferMap.get(glBufferView.buffer);
					buffer.position(glBufferView.byteOffset + glAccessor.byteOffset);
					
					
					FloatBuffer floatBuffer = buffer.asFloatBuffer();
					
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
					nodePart.material = materialMap.get(primitive.material);
				}else{
					nodePart.material = defaultMaterial;
				}
				
				if(ibms.size > 0){
					nodePart.bones = new Matrix4[ibms.size];
					nodePart.invBoneBindTransforms = new ArrayMap<Node, Matrix4>();
					for(int n=0 ; n<joints.size ; n++){
						nodePart.bones[n] = new Matrix4().idt();
						Node key = nodeMap.get(joints.get(n));
						nodePart.invBoneBindTransforms.put(key, ibms.get(n));
					}
				}
				
				parts.add(nodePart);
			}
			
			meshMap.put(meshId, parts);
		}
		node.parts.addAll(parts);
	}

	private short[] loadIndices(GLTFPrimitive primitive) {
		short [] indices = null;
		
		if(primitive.indices != null){
			
			GLTFAccessor indicesAccessor = glModel.accessors.get(primitive.indices);
			
			GLTFBufferView glIndicesBuffer = glModel.bufferViews.get(indicesAccessor.bufferView);
			ByteBuffer glIndicesData = bufferMap.get(glIndicesBuffer.buffer);
			
			
			// 5120 : byte
			// 5121 : ubyte
			// 5122 : short
			// 5123 : ushort
			
			if(indicesAccessor.type.equals("SCALAR")){
				
				int maxIndices = indicesAccessor.count; // glIndicesBuffer.byteLength / 2;
				indices = new short[maxIndices];
				
				glIndicesData.rewind();
				glIndicesData.position(glIndicesBuffer.byteOffset + indicesAccessor.byteOffset);
				
				switch(indicesAccessor.componentType){
				case 5125: // unsigned int
					System.err.println("warning : unsigned int could not work");
					IntBuffer intBuffer = glIndicesData.asIntBuffer();
					for(int i=0 ; i<maxIndices ; i++){
						indices[i] = (short)(intBuffer.get() & 0xFFFF);
					}
					break;
				case 5123: // unsigned short
					{
						int maxIndex;
						if(indicesAccessor.max != null){
							maxIndex = (int)indicesAccessor.max[0];
						}else{
							maxIndex = indicesAccessor.count-1;
						}
						// TODO corset example work well with 16 limit ...
						if(maxIndex >= 1<<15){ // 32767 is the limit for shorts ...
							throw new GdxRuntimeException("indices too big : " + maxIndex);
						}
					}
				case 5122: // short
					glIndicesData.asShortBuffer().get(indices);
					break;
				case 5121: // unsigned bytes
					for(int i=0 ; i<maxIndices ; i++){
						indices[i] = (short)(glIndicesData.get() & 0xFF);
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
	
}
