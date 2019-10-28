package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

/**
 * Convient manager class for: model instances, animators, camera, environment, lights, batch/shaderProvider
 * 
 * @author mgsx
 *
 */
public class SceneManager implements Disposable {
	private Array<Scene> scenes = new Array<Scene>();
	
	private ModelBatch batch;
	private ModelBatch shadowBatch;
	
	public Environment environment;
	
	public Camera camera;

	private SceneSkybox skyBox;
	
	private RenderableSorter renderableSorter;
	
	public SceneManager() {
		this(24);
	}
	
	public SceneManager(int maxBones) {
		this(PBRShaderProvider.createDefault(maxBones), PBRShaderProvider.createDepthShaderProvider(maxBones));
	}
	
	public SceneManager(ShaderProvider shaderProvider, DepthShaderProvider depthShaderProvider)
	{
		this(shaderProvider, depthShaderProvider, new SceneRenderableSorter());
	}
	
	public SceneManager(ShaderProvider shaderProvider, DepthShaderProvider depthShaderProvider, RenderableSorter renderableSorter)
	{
		this.renderableSorter = renderableSorter;
		
		batch = new ModelBatch(shaderProvider, renderableSorter);
		
		shadowBatch = new ModelBatch(depthShaderProvider);
		
		environment = new Environment();
		
		float lum = 1f;
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, lum, lum, lum, 1));
	}
	
	public ModelBatch getBatch() {
		return batch;
	}
	
	public void setShaderProvider(ShaderProvider shaderProvider) {
		batch.dispose();
		batch = new ModelBatch(shaderProvider, renderableSorter);
	}
	
	public void setDepthShaderProvider(DepthShaderProvider depthShaderProvider) {
		shadowBatch.dispose();
		shadowBatch = new ModelBatch(depthShaderProvider);
	}
	
	public void addScene(Scene scene){
		addScene(scene, true);
	}
	
	public void addScene(Scene scene, boolean appendLights){
		scenes.add(scene);
		if(appendLights){
			for(Entry<Node, BaseLight> e : scene.lights){
				environment.add(e.value);
			}
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
	
	/**
	 * render all scenes.
	 * because shadows use frame buffers, if you need to render scenes to a frame buffer, you should instead
	 * first call {@link #renderShadows()}, bind your frame buffer and then call {@link #renderColors()}
	 */
	public void render(){
		if(camera == null) return;
		
		renderShadows();
		
		renderColors();
	}
	
	/**
	 * Render shadows only to interal frame buffers.
	 * (useful when you're using your own frame buffer to render scenes)
	 */
	@SuppressWarnings("deprecation")
	public void renderShadows(){
		DirectionalLight light = getFirstDirectionalLight();
		if(light instanceof DirectionalShadowLight){
			DirectionalShadowLight shadowLight = (DirectionalShadowLight)light;
			shadowLight.begin();
			shadowBatch.begin(shadowLight.getCamera());
			for(Scene scene : scenes){
				shadowBatch.render(scene.modelInstance);
			}
			shadowBatch.end();
			shadowLight.end();
			
			environment.shadowMap = shadowLight;
		}else{
			environment.shadowMap = null;
		}
	}
	
	/**
	 * Render colors only. You should call {@link #renderShadows()} before.
	 * (useful when you're using your own frame buffer to render scenes)
	 */
	public void renderColors(){
		batch.begin(camera);
		for(Scene scene : scenes){
			batch.render(scene.modelInstance, environment);
		}
		if(skyBox != null){
			batch.render(skyBox);
		}
		batch.end();
	}
	
	public DirectionalLight getFirstDirectionalLight(){
		DirectionalLightsAttribute dla = environment.get(DirectionalLightsAttribute.class, DirectionalLightsAttribute.Type);
		if(dla != null){
			for(DirectionalLight dl : dla.lights){
				if(dl instanceof DirectionalLight){
					return (DirectionalLight)dl;
				}
			}
		}
		return null;
	}

	public void setSkyBox(SceneSkybox skyBox) {
		this.skyBox = skyBox;
	}
	
	public void setAmbientLight(float lum) {
		environment.get(ColorAttribute.class, ColorAttribute.AmbientLight).color.set(lum, lum, lum, 1);
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public void removeScene(Scene scene) {
		scenes.removeValue(scene, true);
		for(Entry<Node, BaseLight> e : scene.lights){
			environment.remove(e.value);
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
		shadowBatch.dispose();
	}
}
