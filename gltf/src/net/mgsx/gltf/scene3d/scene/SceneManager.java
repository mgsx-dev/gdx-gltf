package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap.Entry;

/**
 * not related to GLTF : just a helper with : model instances, animators, camera, environement, lights, batch/shaderProvider
 * 
 * @author mgsx
 *
 */
public class SceneManager implements Disposable {
	private Array<Scene> scenes = new Array<Scene>();
	
	private ModelBatch batch;
	
	public final Array<DirectionalLight> directionalLights = new Array<DirectionalLight>();
	
	public Environment environment;
	
	public Camera camera;

	private SceneSkybox skyBox;
	
	public SceneManager() {
		this(new DefaultShaderProvider());
	}
	
	public SceneManager(ShaderProvider shaderProvider)
	{
		batch = new ModelBatch(shaderProvider, new SceneRenderableSorter());
		
		environment = new Environment();
		
		setDefaultLight();
		
		float lum = .5f;
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, lum, lum, lum, 1));
	}
	
	public ModelBatch getBatch() {
		return batch;
	}
	
	public void setShaderProvider(ShaderProvider shaderProvider) {
		batch.dispose();
		batch = new ModelBatch(shaderProvider, new SceneRenderableSorter());
	}
	
	
	public void addScene(Scene scene){
		scenes.add(scene);
		for(Entry<BaseLight, Node> e : scene.lights){
			environment.add(e.key);
		}
	}
	
	public void update(float delta){
		if(camera != null){
			skyBox.update(camera);
		}
		for(Scene scene : scenes){
			scene.update(delta);
		}
	}
	
	public void render(){
		if(camera == null) return;
		batch.begin(camera);
		for(Scene scene : scenes){
			batch.render(scene.modelInstance, environment);
		}
		batch.render(skyBox);
		batch.end();
		
	}

	public void setSkyBox(SceneSkybox skyBox) {
		this.skyBox = skyBox;
	}
	
	public void setAmbiantLight(float lum) {
		environment.get(ColorAttribute.class, ColorAttribute.AmbientLight).color.set(lum, lum, lum, 1);
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public void removeScene(Scene scene) {
		scenes.removeValue(scene, true);
		for(Entry<BaseLight, Node> e : scene.lights){
			environment.remove(e.key);
		}
	}
	
	public Array<Scene> getScenes() {
		return scenes;
	}

	public void updateViewport(int width, int height) {
		if(camera != null){
			camera.viewportWidth = width;
			camera.viewportHeight = height;
			camera.update(true);
		}
	}

	@Override
	public void dispose() {
		batch.dispose();
	}

	public void setDefaultLight() {
		if(directionalLights.size < 1){
			DirectionalLight dirLight = new DirectionalLight();
			dirLight.set(Color.WHITE, new Vector3(0,-1,0));
			environment.add(dirLight);
			directionalLights.add(dirLight);
		}
	}
	
	public void removeDefaultLight(){
		if(directionalLights.size > 0){
			environment.remove(directionalLights.first());
			directionalLights.clear();
		}
	}
}
