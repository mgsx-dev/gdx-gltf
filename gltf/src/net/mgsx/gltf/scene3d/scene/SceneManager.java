package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.lights.PointLightEx;
import net.mgsx.gltf.scene3d.lights.SpotLightEx;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.EnvironmentCache;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;

/**
 * Convient manager class for: model instances, animators, camera, environment, lights, batch/shaderProvider
 * 
 * @author mgsx
 *
 */
public class SceneManager implements Disposable {
	private Array<Scene> scenes = new Array<Scene>();
	
	private ModelBatch batch;
	private ModelBatch depthBatch;
	
	/** Shouldn't be null. */
	public Environment environment = new Environment();
	protected final EnvironmentCache computedEnvironement = new EnvironmentCache();
	
	public Camera camera;

	private SceneSkybox skyBox;
	
	private RenderableSorter renderableSorter;
	
	private PointLightsAttribute pointLights = new PointLightsAttribute();
	private SpotLightsAttribute spotLights = new SpotLightsAttribute();
			

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
		
		depthBatch = new ModelBatch(depthShaderProvider);
		
		float lum = 1f;
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, lum, lum, lum, 1));
	}
	
	public ModelBatch getBatch() {
		return batch;
	}
	
	public void setBatch(ModelBatch batch) {
		this.batch = batch;
	}
	
	public void setShaderProvider(ShaderProvider shaderProvider) {
		batch.dispose();
		batch = new ModelBatch(shaderProvider, renderableSorter);
	}
	
	public void setDepthShaderProvider(DepthShaderProvider depthShaderProvider) {
		depthBatch.dispose();
		depthBatch = new ModelBatch(depthShaderProvider);
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
	
	/**
	 * should be called in order to perform light culling, skybox update and animations.
	 * @param delta
	 */
	public void update(float delta){
		if(camera != null){
			updateEnvironment();
			if(skyBox != null){
				skyBox.update(camera);
			}
		}
		for(Scene scene : scenes){
			scene.update(delta);
		}
	}
	
	protected void updateEnvironment(){
		computedEnvironement.setCache(environment);
		pointLights.lights.clear();
		spotLights.lights.clear();
		if(environment != null) {
			for(Attribute a : environment){
				if(a instanceof PointLightsAttribute){
					pointLights.lights.addAll(((PointLightsAttribute) a).lights);
					computedEnvironement.replaceCache(pointLights);
				}else if(a instanceof SpotLightsAttribute){
					spotLights.lights.addAll(((SpotLightsAttribute) a).lights);
					computedEnvironement.replaceCache(spotLights);
				}else{
					computedEnvironement.set(a);
				}
			}
		}
		cullLights();
	}
	protected void cullLights(){
		PointLightsAttribute pla = environment.get(PointLightsAttribute.class, PointLightsAttribute.Type);
		if(pla != null){
			for(PointLight light : pla.lights){
				if(light instanceof PointLightEx){
					PointLightEx l = (PointLightEx) light;
					if(l.range != null && !camera.frustum.sphereInFrustum(l.position, l.range)){
						pointLights.lights.removeValue(l, true);
					}
				}
			}
		}
		SpotLightsAttribute sla = environment.get(SpotLightsAttribute.class, SpotLightsAttribute.Type);
		if(sla != null){
			for(SpotLight light : sla.lights){
				if(light instanceof SpotLightEx){
					SpotLightEx l = (SpotLightEx) light;
					if(l.range != null && !camera.frustum.sphereInFrustum(l.position, l.range)){
						spotLights.lights.removeValue(l, true);
					}
				}
			}
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
			depthBatch.begin(shadowLight.getCamera());
			for(Scene scene : scenes){
				depthBatch.render(scene.modelInstance);
			}
			depthBatch.end();
			shadowLight.end();
			
			environment.shadowMap = shadowLight;
		}else{
			environment.shadowMap = null;
		}
	}
	
	/**
	 * Render only depth (packed 32 bits), usefull for post processing effects.
	 * You typically render it to a FBO with depth enabled.
	 */
	public void renderDepth(){
		depthBatch.begin(camera);
		for(Scene scene : scenes){
			depthBatch.render(scene.modelInstance);
		}
		depthBatch.end();
	}
	
	/**
	 * Render colors only. You should call {@link #renderShadows()} before.
	 * (useful when you're using your own frame buffer to render scenes)
	 */
	public void renderColors(){
		computedEnvironement.shadowMap = environment.shadowMap;
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
	
	public int getActiveLightsCount(){
		return EnvironmentUtil.getLightCount(computedEnvironement);
	}
	public int getTotalLightsCount(){
		return EnvironmentUtil.getLightCount(environment);
	}
	

	@Override
	public void dispose() {
		batch.dispose();
		depthBatch.dispose();
	}
}
