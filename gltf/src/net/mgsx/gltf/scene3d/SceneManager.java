package net.mgsx.gltf.scene3d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * not related to GLTF : just a helper with : model instances, animators, camera, environement, lights, batch/shaderProvider
 * 
 * @author mgsx
 *
 */
public class SceneManager {
	private Array<Scene> scenes = new Array<Scene>();
	
	public ModelBatch batch;
	
	// TODO lights
	public final Array<DirectionalLight> directionalLights = new Array<DirectionalLight>();
	
	public Environment environment;
	
	// TODO default batch with provider and shader
	
	public Camera camera;
	public Node cameraNode;

	private SceneSkybox skyBox;
	
	public SceneManager() {
		this(new DefaultShaderProvider());
	}
	
	public SceneManager(ShaderProvider shaderProvider)
	{
		batch = new ModelBatch(shaderProvider);
		
		environment = new Environment();
		
		
		DirectionalLight dirLight = new DirectionalLight();
		dirLight.set(Color.WHITE, new Vector3(0,-1,0));
		environment.add(dirLight);
		directionalLights.add(dirLight);
		
		float lum = .3f;
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, lum, lum, lum, 1));
	}
	
	
	
	public void addScene(Scene scene){
		scenes.add(scene);
	}
	
	public void update(float delta){
		for(Scene scene : scenes){
			scene.upadte(delta);
		}
	}
	
	public void render(){
		if(camera == null) return;
		if(cameraNode != null){
			camera.position.setZero().mul(cameraNode.globalTransform);
			camera.direction.set(0,0,-1).rot(cameraNode.globalTransform);
			camera.up.set(0,1,0).rot(cameraNode.globalTransform);
			camera.update();
		}
		batch.begin(camera);
		for(Scene scene : scenes){
			batch.render(scene.modelInstance, environment);
		}
		// TODO render directly skybox with strategy : all opaque near to far, skybox, all transparent far to near
		skyBox.render(batch);
		batch.end();
		
	}

	public void setSkyBox(SceneSkybox skyBox) {
		this.skyBox = skyBox;
	}
	
	public void setAmbiantLight(float lum) {
		environment.get(ColorAttribute.class, ColorAttribute.AmbientLight).color.set(lum, lum, lum, 1);
	}



	public void setCamera(Camera camera) {
		setCamera(camera, null);
	}

	public void setCamera(Camera camera, Node cameraNode) {
		this.camera = camera;
		this.cameraNode = cameraNode;
	}

	public void removeScene(Scene scene) {
		scenes.removeValue(scene, true);
	}

	public void updateViewport(int width, int height) {
		if(camera != null){
			camera.viewportWidth = width;
			camera.viewportHeight = height;
			camera.update(true);
		}
	}
	
}
