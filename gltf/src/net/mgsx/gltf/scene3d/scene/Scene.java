package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
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
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Pool;

import net.mgsx.gltf.scene3d.animation.AnimationControllerHack;
import net.mgsx.gltf.scene3d.animation.AnimationsPlayer;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.PointLightEx;
import net.mgsx.gltf.scene3d.lights.SpotLightEx;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;

public class Scene implements RenderableProvider, Updatable {
	public ModelInstance modelInstance;
	public AnimationController animationController;
	
	public final ObjectMap<Node, BaseLight> lights = new ObjectMap<Node, BaseLight>();
	public final ObjectMap<Node, Camera> cameras = new ObjectMap<Node, Camera>();
	public final AnimationsPlayer animations;
	
	private static final Matrix4 transform = new Matrix4();
	
	public Scene(SceneModel sceneModel) {
		this(new ModelInstanceHack(sceneModel.model));
		for(Entry<Node, Camera> entry : sceneModel.cameras){
			cameras.put(modelInstance.getNode(entry.key.id, true), createCamera(entry.value));
		}
		for(Entry<Node, BaseLight> entry : sceneModel.lights){
			lights.put(modelInstance.getNode(entry.key.id, true), createLight(entry.value));
		}
		syncCameras();
		syncLights();
	}
	
	public Camera createCamera(Camera from) 
	{
		Camera copy;
		if(from instanceof PerspectiveCamera){
			PerspectiveCamera camera = new PerspectiveCamera();
			camera.fieldOfView = ((PerspectiveCamera) from).fieldOfView;
			copy = camera;
		}else if(from instanceof OrthographicCamera){
			OrthographicCamera camera = new OrthographicCamera();
			camera.zoom = ((OrthographicCamera) from).zoom;
			copy = camera;
		}else{
			throw new GdxRuntimeException("unknown camera type " + from.getClass().getName());
		}
		copy.position.set(from.position);
		copy.direction.set(from.direction);
		copy.up.set(from.up);
		copy.near = from.near;
		copy.far = from.far;
		copy.viewportWidth = from.viewportWidth;
		copy.viewportHeight = from.viewportHeight;
		return copy;
	}
	
	protected BaseLight createLight(BaseLight from) {
		if(from instanceof DirectionalLight){
			return new DirectionalLightEx().set((DirectionalLight)from);
		}
		if(from instanceof PointLight){
			return new PointLightEx().set((PointLight)from);
		}
		if(from instanceof SpotLight){
			return new SpotLightEx().set((SpotLight)from);
		}
		throw new GdxRuntimeException("unknown light type " + from.getClass().getName());
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
}
