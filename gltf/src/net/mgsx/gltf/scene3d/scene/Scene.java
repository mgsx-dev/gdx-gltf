package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Pool;

import net.mgsx.gltf.scene3d.animation.AnimationControllerHack;
import net.mgsx.gltf.scene3d.animation.AnimationsPlayer;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;
import net.mgsx.gltf.scene3d.utils.CameraUtils;
import net.mgsx.gltf.scene3d.utils.LightUtils;

public class Scene implements RenderableProvider, Updatable {
	public ModelInstance modelInstance;
	public AnimationController animationController;
	
	public final ObjectMap<Node, BaseLight> lights = new ObjectMap<Node, BaseLight>();
	public final ObjectMap<Node, Camera> cameras = new ObjectMap<Node, Camera>();
	public final AnimationsPlayer animations;
	
	private static final Matrix4 transform = new Matrix4();
	
	public Scene(SceneModel sceneModel) {
		this(new ModelInstanceHack(sceneModel.model), sceneModel);
	}
	
	public Scene(SceneModel sceneModel, String...rootNodeIds) {
		this(new ModelInstanceHack(sceneModel.model, rootNodeIds), sceneModel);
	}
	
	private Scene(ModelInstance modelInstance, SceneModel sceneModel){
		this(modelInstance);
		for(Entry<Node, Camera> entry : sceneModel.cameras){
			Node node = modelInstance.getNode(entry.key.id, true);
			if(node != null){
				cameras.put(node, CameraUtils.createCamera(entry.value));
			}
		}
		for(Entry<Node, BaseLight> entry : sceneModel.lights){
			Node node = modelInstance.getNode(entry.key.id, true);
			if(node != null){
				lights.put(node, LightUtils.createLight(entry.value));
			}
		}
		syncCameras();
		syncLights();
	}
	
	/**
	 * Default constructor create animated scene if model contains animations.
	 * use {@link #Scene(Model, boolean)} constructor to force animation management.
	 * @param model
	 */
	public Scene(Model model) {
		this(new ModelInstanceHack(model));
	}

	/**
	 * Default constructor create animated scene if model instance contains animations.
	 * use {@link #Scene(Model, boolean)} constructor to force animation management.
	 * @param modelInstance
	 */
	public Scene(ModelInstance modelInstance) {
		this(modelInstance, modelInstance.animations.size > 0);
	}

	/**
	 * Create a scene
	 * @param modelInstance
	 * @param animated
	 */
	public Scene(ModelInstance modelInstance, boolean animated) {
		super();
		this.modelInstance = modelInstance;
		if(animated){
			this.animationController = new AnimationControllerHack(modelInstance);
		}
		animations = new AnimationsPlayer(this);
	}
	public Scene(Model model, boolean animated) {
		this(new ModelInstanceHack(model), animated);
	}

	@Override
	public void update(Camera camera, float delta){
		animations.update(delta);
		syncCameras();
		syncLights();
	}

	private void syncCameras(){
		for(Entry<Node, Camera> e : cameras){
			Node node = e.key;
			Camera camera = e.value;
			transform.set(modelInstance.transform).mul(node.globalTransform);
			camera.position.setZero().mul(transform);
			camera.direction.set(0,0,-1).rot(transform);
			camera.up.set(Vector3.Y).rot(transform);
			camera.update();
		}
	}
	
	private void syncLights(){
		for(Entry<Node, BaseLight> e : lights){
			Node node = e.key;
			BaseLight light = e.value;
			transform.set(modelInstance.transform).mul(node.globalTransform);
			if(light instanceof DirectionalLight){
				((DirectionalLight)light).direction.set(0,0,-1).rot(transform);
			}else if(light instanceof PointLight){
				((PointLight)light).position.setZero().mul(transform);
			}else if(light instanceof SpotLight){
				((SpotLight)light).position.setZero().mul(transform);
				((SpotLight)light).direction.set(0,0,-1).rot(transform);
			}
		}
	}
	
	public Camera getCamera(String name) {
		for(Entry<Node, Camera> e : cameras){
			if(name.equals(e.key.id)){
				return e.value;
			}
		}
		return null;
	}
	
	public BaseLight getLight(String name) {
		for(Entry<Node, BaseLight> e : lights){
			if(name.equals(e.key.id)){
				return e.value;
			}
		}
		return null;
	}

	public int getDirectionalLightCount() {
		int count = 0;
		for(Entry<Node, BaseLight> entry : lights){
			if(entry.value instanceof DirectionalLight){
				count++;
			}
		}
		return count;
	}

	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		modelInstance.getRenderables(renderables, pool);
	}

	public ModelInstance getModelInstance() {
		return modelInstance;
	}

	public void setModelInstance(ModelInstance modelInstance) {
		this.modelInstance = modelInstance;
	}

	public AnimationController getAnimationController() {
		return animationController;
	}

	public void setAnimationController(AnimationController animationController) {
		this.animationController = animationController;
	}

	public ObjectMap<Node, BaseLight> getLights() {
		return lights;
	}

	public ObjectMap<Node, Camera> getCameras() {
		return cameras;
	}

	public AnimationsPlayer getAnimations() {
		return animations;
	}
}
