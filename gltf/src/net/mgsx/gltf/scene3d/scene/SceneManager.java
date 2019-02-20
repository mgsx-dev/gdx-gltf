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
	
	private DirectionalLight defaultLight;
	
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
		
		enableDefaultLight();
		
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
			if(skyBox != null){
				skyBox.update(camera);
			}
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
		if(skyBox != null){
			batch.render(skyBox);
		}
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

	public void enableDefaultLight() {
		if(defaultLight == null){
			defaultLight = new DirectionalLight();
			defaultLight.set(Color.WHITE, new Vector3(0,-1,0));
			environment.add(defaultLight);
		}
	}
	
	public void disableDefaultLight(){
		if(defaultLight != null){
			environment.remove(defaultLight);
		}
	}
	
	public DirectionalLight getDefaultLight() {
		return defaultLight;
	}
}
