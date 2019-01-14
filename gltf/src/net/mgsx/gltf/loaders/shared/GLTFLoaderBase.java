package net.mgsx.gltf.loaders.shared;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.camera.GLTFCamera;
import net.mgsx.gltf.data.extensions.KHRLightsPunctual;
import net.mgsx.gltf.data.extensions.KHRLightsPunctual.GLTFLight;
import net.mgsx.gltf.data.extensions.KHRMaterialsPBRSpecularGlossiness;
import net.mgsx.gltf.data.extensions.KHRTextureTransform;
import net.mgsx.gltf.data.scene.GLTFNode;
import net.mgsx.gltf.data.scene.GLTFScene;
import net.mgsx.gltf.loaders.shared.animation.AnimationLoader;
import net.mgsx.gltf.loaders.shared.data.DataResolver;
import net.mgsx.gltf.loaders.shared.geometry.MeshLoader;
import net.mgsx.gltf.loaders.shared.material.DefaultMaterialLoader;
import net.mgsx.gltf.loaders.shared.material.MaterialLoader;
import net.mgsx.gltf.loaders.shared.scene.NodeResolver;
import net.mgsx.gltf.loaders.shared.scene.SkinLoader;
import net.mgsx.gltf.loaders.shared.texture.ImageResolver;
import net.mgsx.gltf.loaders.shared.texture.TextureResolver;
import net.mgsx.gltf.scene3d.model.NodePlus;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneModel;

abstract public class GLTFLoaderBase implements Disposable {

	private final Array<Camera> cameras = new Array<Camera>();
	private final Array<BaseLight> lights = new Array<BaseLight>();
	
	/** node name to light index */
	private ObjectMap<String, Integer> lightMap = new ObjectMap<String, Integer>();
	
	
	/** node name to camera index */
	private ObjectMap<String, Integer> cameraMap = new ObjectMap<String, Integer>();

	private Array<SceneModel> scenes = new Array<SceneModel>();
	
	protected GLTF glModel;
	
	protected MaterialLoader materialLoader;
	protected TextureResolver textureResolver;
	protected AnimationLoader animationLoader;
	protected DataResolver dataResolver;
	protected SkinLoader skinLoader;
	protected NodeResolver nodeResolver;
	protected MeshLoader meshLoader;
	protected ImageResolver imageResolver;
	
	public GLTFLoaderBase() {
		materialLoader = new DefaultMaterialLoader();
		textureResolver = new TextureResolver();
		animationLoader = new AnimationLoader();
		nodeResolver = new NodeResolver();
		meshLoader = new MeshLoader();
		skinLoader = new SkinLoader();
	}
	
	/**
	 * Expose glModel to user code in order to retreive some user define properties {@link net.mgsx.gltf.model.GLTFExtras}
	 * @return parsed GLTF root object
	 */
	public GLTF getGlModel() {
		return glModel;
	}
	
	protected SceneAsset loadInternal(){
		try{
			
			// prerequists
			if(glModel.extensionsRequired != null){
				for(String extension : glModel.extensionsRequired){
					if(KHRMaterialsPBRSpecularGlossiness.EXT.equals(extension)){
					}else if(KHRTextureTransform.EXT.equals(extension)){
					}else if(KHRLightsPunctual.EXT.equals(extension)){
					}else{
						throw new GdxRuntimeException("Extension " + extension + " required but not supported");
					}
				}
			}
			
			// load deps from lower to higher
			
			// images (pixmaps)
			imageResolver.load(glModel.images);
			textureResolver.loadTextures(glModel.textures, glModel.samplers, imageResolver);
			imageResolver.dispose();
			
			materialLoader.loadMaterials(glModel.materials, textureResolver);
			
			loadCameras();
			loadLights();
			loadScenes();
			
			animationLoader.load(glModel.animations, nodeResolver, dataResolver);
			skinLoader.load(glModel.skins, glModel.nodes, nodeResolver, dataResolver);
			
			// create scene asset
			SceneAsset model = new SceneAsset();
			model.scenes = scenes;
			model.scene = scenes.get(glModel.scene);
			model.maxBones = meshLoader.getMaxBones();
			textureResolver.getTextures(model.textures);
			model.animations = animationLoader.animations;
			// XXX don't know where the animation are ...
			for(SceneModel scene : model.scenes){
				scene.model.animations.addAll(animationLoader.animations);
			}
			
			return model;
		}catch(RuntimeException e){
			dispose();
			throw e;
		}
	}
	
	private void loadLights() {
		if(glModel.extensions != null){
			KHRLightsPunctual.GLTFLights lightExt = glModel.extensions.get(KHRLightsPunctual.GLTFLights.class, KHRLightsPunctual.EXT);
			if(lightExt != null){
				for(GLTFLight light : lightExt.lights){
					lights.add(KHRLightsPunctual.map(light));
				}
			}
		}
	}

	@Override
	public void dispose() {
		imageResolver.dispose();
		textureResolver.dispose();
		for(SceneModel scene : scenes){
			scene.dispose();
		}
	}

	private void loadScenes() {
		for(int i=0 ; i<glModel.scenes.size ; i++)
		{
			scenes.add(loadScene(glModel.scenes.get(i)));
		}
	}

	private void loadCameras() {
		if(glModel.cameras != null){
			for(GLTFCamera glCamera : glModel.cameras){
				cameras.add(GLTFTypes.map(glCamera));
			}
		}
	}
	
	private SceneModel loadScene(GLTFScene gltfScene) 
	{
		SceneModel sceneModel = new SceneModel();
		sceneModel.name = gltfScene.name;
		sceneModel.model = new Model();
		
		// add root nodes
		for(int id : gltfScene.nodes){
			sceneModel.model.nodes.add(getNode(id));
		}
		// add scene cameras (filter from all scenes cameras)
		for(Entry<String, Integer> entry : cameraMap){
			Node node = sceneModel.model.getNode(entry.key, true);
			if(node != null) sceneModel.cameras.put(cameras.get(entry.value), node);
		}
		// add scene lights (filter from all scenes lights)
		for(Entry<String, Integer> entry : lightMap){
			Node node = sceneModel.model.getNode(entry.key, true);
			if(node != null) sceneModel.lights.put(lights.get(entry.value), node);
		}
		
		return sceneModel;
	}

	private Node getNode(int id) 
	{
		Node node = nodeResolver.get(id);
		if(node == null){
			node = new NodePlus();
			nodeResolver.put(id, node);
			
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
			
			node.id = glNode.name == null ? "glNode " + id : glNode.name;
			
			if(glNode.children != null){
				for(int childId : glNode.children){
					node.addChild(getNode(childId));
				}
			}
			
			if(glNode.mesh != null){
				meshLoader.load(node, glModel.meshes.get(glNode.mesh), dataResolver, materialLoader);
			}
			
			if(glNode.camera != null){
				cameraMap.put(node.id, glNode.camera);
			}
			
			// node extensions
			if(glNode.extensions != null){
				KHRLightsPunctual.GLTFLightNode nodeLight = glNode.extensions.get(KHRLightsPunctual.GLTFLightNode.class, KHRLightsPunctual.EXT);
				if(nodeLight != null){
					lightMap.put(node.id, nodeLight.light);
				}
			}
			
		}
		return node;
	}
}