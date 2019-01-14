package net.mgsx.gltf.demo;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import net.mgsx.gltf.demo.data.ModelEntry;
import net.mgsx.gltf.demo.ui.GLTFDemoUI;
import net.mgsx.gltf.demo.util.EnvironmentUtil;
import net.mgsx.gltf.demo.util.NodeUtil;
import net.mgsx.gltf.demo.util.SafeHttpResponseListener;
import net.mgsx.gltf.loaders.GLBLoader;
import net.mgsx.gltf.loaders.GLTFLoader;
import net.mgsx.gltf.loaders.PixmapBinaryLoaderHack;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.shaders.PBRShader;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

public class GLTFDemo extends ApplicationAdapter
{
	public static String AUTOLOAD_ENTRY = null;
	public static String AUTOLOAD_VARIANT = null;
	
	private static final String TAG = "GLTFDemo";
	
	public static enum ShaderMode{
		GOURAUD,	// https://en.wikipedia.org/wiki/Gouraud_shading#Comparison_with_other_shading_techniques
//		PHONG,   	// https://en.wikipedia.org/wiki/Phong_shading
		PBR_MR, 
//		PBR_MRSG
	}
	
	private ShaderMode shaderMode = ShaderMode.PBR_MR;
	
	private String samplesPath;
	
	private Stage stage;
	private Skin skin;
	private Array<ModelEntry> entries;
	
	private FileHandle rootFolder;
	private CameraInputController cameraControl;
	
	private Scene scene;
	
	private SceneAsset rootModel;

	private SceneManager sceneManager;
	private GLTFDemoUI ui;
	private Cubemap diffuseCubemap;
	private Cubemap environmentCubemap;
	private Cubemap specularCubemap;
	private Texture brdfLUT;
	
	public GLTFDemo() {
		this("models");
	}
	
	public GLTFDemo(String samplesPath) {
		this.samplesPath = samplesPath;
	}
	
	@Override
	public void create() {
		
		createUI();
		
		createSceneManager();
		
		loadModelIndex();
	}
	
	private void createSceneManager()
	{
		// set environment maps
		
		diffuseCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				"textures/diffuse/diffuse_", "_0.jpg");

		environmentCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				"textures/environment/environment_", "_0.png");

		
		specularCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				"textures/specular/specular_", "_", ".jpg", 10);
		
		brdfLUT = new Texture(Gdx.files.internal("textures/brdfLUT.png"));
		
		sceneManager = new SceneManager(createShaderProvider(shaderMode, 12));
		
		sceneManager.setSkyBox(new SceneSkybox(environmentCubemap));
		
		// light direction based on environnement map SUN
		if(sceneManager.directionalLights.size > 0){
			sceneManager.directionalLights.first().direction.set(-.5f,-.5f,-.7f).nor();
			ui.lightDirectionControl.set(sceneManager.directionalLights.first().direction);
		}

		setEnvironment();
	}
	
	private void setEnvironment()
	{
		// TODO config UI based
		
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
		
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));

		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
	}
	
	private void loadModelIndex() 
	{
		rootFolder = Gdx.files.internal(samplesPath);	
		
		String indexFilename = Gdx.app.getType() == ApplicationType.WebGL ? "model-index-web.json" : "model-index.json";
		
		FileHandle file = rootFolder.child(indexFilename);
		
		entries = new Json().fromJson(Array.class, ModelEntry.class, file);
		
		ui.entrySelector.setItems(entries);
		
		if(AUTOLOAD_ENTRY != null && AUTOLOAD_VARIANT != null){
			for(int i=0 ; i<entries.size ; i++){
				ModelEntry entry = entries.get(i);
				if(entry.name.equals(AUTOLOAD_ENTRY)){
					ui.entrySelector.setSelected(entry);
					// will be auto select if there is only one variant.
					if(entry.variants.size != 1){
						ui.variantSelector.setSelected(AUTOLOAD_VARIANT);
					}
					break;
				}
			}
		}
	}

	private void createUI()
	{
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);
		skin = new Skin(Gdx.files.internal("skins/uiskin.json"));
		
		ui = new GLTFDemoUI(skin);
		ui.setFillParent(true);
		
		stage.addActor(ui);
		
		ui.shaderSelector.setSelected(shaderMode);
		
		ui.entrySelector.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				ui.setEntry(ui.entrySelector.getSelected(), rootFolder);
				setImage(ui.entrySelector.getSelected());
			}
		});
		
		ui.variantSelector.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				load(ui.entrySelector.getSelected(), ui.variantSelector.getSelected());
			}
		});
		
		ui.animationSelector.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setAnimation(ui.animationSelector.getSelected());
			}
		});
		
		ui.cameraSelector.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setCamera(ui.cameraSelector.getSelected());
			}
		});
		
		ui.shaderSelector.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setShader(ui.shaderSelector.getSelected());
			}
		});
		
		ChangeListener shaderOptionListener = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setShader(shaderMode);
			}
		};
		
		ui.shaderSRGB.addListener(shaderOptionListener);
		ui.shaderDebug.toggle.addListener(shaderOptionListener);
		
		ui.sceneSelector.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				load(ui.sceneSelector.getSelected());
			}
		});
	}
	
	protected void setImage(ModelEntry entry) {
		if(entry.screenshot != null){
			if(entry.url != null){
				HttpRequest httpRequest = new HttpRequest(HttpMethods.GET);
				httpRequest.setUrl(entry.url + entry.screenshot);

				Gdx.net.sendHttpRequest(httpRequest, new SafeHttpResponseListener(){
					@Override
					protected void handleData(byte[] bytes) {
						Pixmap pixmap = PixmapBinaryLoaderHack.load(bytes, 0, bytes.length);
						ui.setImage(new Texture(pixmap));
						pixmap.dispose();
					}
					@Override
					protected void handleError(Throwable t) {
						Gdx.app.error(TAG, "request error", t);
					}
					@Override
					protected void handleEnd() {
					}
				});
			}else{
				FileHandle file = rootFolder.child(entry.name).child(entry.screenshot);
				if(file.exists()){
					ui.setImage(new Texture(file));
				}else{
					Gdx.app.error("DEMO UI", "file not found " + file.path());
				}
			}
		}
	}

	private void setShader(ShaderMode shaderMode) {
		sceneManager.setShaderProvider(createShaderProvider(shaderMode, rootModel.maxBones));
	}
	
	private ShaderProvider createShaderProvider(ShaderMode shaderMode, int maxBones){
		
		switch(shaderMode){
		default:
		case GOURAUD:
			{
				Config config = new DefaultShader.Config();
				config.numBones = maxBones;
				return new DefaultShaderProvider(config);
			}
//		case PHONG:
//			// TODO phong variant (pixel based lighting)
//		case PBR_MRSG:
//			// TODO SG shader variant
		case PBR_MR:
			{
				PBRShaderConfig config = new PBRShaderConfig();
				config.manualSRGB = ui.shaderSRGB.getSelected();
				config.numBones = maxBones;
				config.debug = ui.shaderDebug.toggle.isChecked();
				return PBRShaderProvider.createDefault(config);
			}
		}
	}

	private void setAnimation(String name) {
		if(scene != null && scene.animationController != null){
			if(name == null || name.isEmpty()){
				scene.animationController.setAnimation(null);
			}else{
				scene.animationController.animate(name, -1, 1f, null, 0f);
			}
		}
	}

	private void clearScene(){
		if(scene != null){
			sceneManager.removeScene(scene);
			scene = null;
		}
		
	}
	
	private void load(ModelEntry entry, String variant) {
		
		clearScene();
		
		if(rootModel != null){
			rootModel.dispose();
			rootModel = null;
		}
		
		
		if(variant.isEmpty()) return;
		
		final String fileName = entry.variants.get(variant);
		if(fileName == null) return;
		
		if(entry.url != null){
			
			final Table waitUI = new Table(skin);
			waitUI.add("LOADING...").expand().center();
			waitUI.setFillParent(true);
			stage.addActor(waitUI);
			
			HttpRequest httpRequest = new HttpRequest(HttpMethods.GET);
			httpRequest.setUrl(entry.url + variant + "/" + fileName);

			Gdx.net.sendHttpRequest(httpRequest, new SafeHttpResponseListener(){
				@Override
				protected void handleData(byte[] bytes) {
					Gdx.app.log(TAG, "loading " + fileName);
					
					if(fileName.endsWith(".gltf")){
						throw new GdxRuntimeException("remote gltf format not supported.");
					}else if(fileName.endsWith(".glb")){
						rootModel = new GLBLoader().load(bytes);
					}else{
						throw new GdxRuntimeException("unknown file extension for " + fileName);
					}
					
					load();
					
					Gdx.app.log(TAG, "loaded " + fileName);
				}
				@Override
				protected void handleError(Throwable t) {
					Gdx.app.error(TAG, "request error", t);
				}
				@Override
				protected void handleEnd() {
					waitUI.remove();
				}
			});
		}else{
			FileHandle baseFolder = rootFolder.child(entry.name).child(variant);
			FileHandle glFile = baseFolder.child(fileName);
			
			Gdx.app.log(TAG, "loading " + fileName);
			
			if(fileName.endsWith(".gltf")){
				rootModel = new GLTFLoader().load(glFile, baseFolder);
			}else if(fileName.endsWith(".glb")){
				rootModel = new GLBLoader().load(glFile);
			}else{
				throw new GdxRuntimeException("unknown file extension " + glFile.extension());
			}
			
			load();
			
			Gdx.app.log(TAG, "loaded " + glFile.path());
		}
	}
	
	private void load()
	{
		if(rootModel.scenes.size > 1){
			ui.setScenes(rootModel.scenes);
			ui.sceneSelector.setSelectedIndex(rootModel.scenes.indexOf(rootModel.scene, true));
		}else{
			ui.setScenes(null);
			load(new Scene(rootModel.scene));
		}
	}
	
	protected void load(String name) {
		int index = ui.sceneSelector.getItems().indexOf(name, false) - 1;
		if(index < 0){
			return;
		}
		load(new Scene(rootModel.scenes.get(index)));
	}
	
	private void load(Scene scene)
	{
		clearScene();
		
		this.scene = scene;
		
		if(scene.lights.size == 0){
			sceneManager.setDefaultLight();
		}else{
			sceneManager.removeDefaultLight();
		}
		
		ui.setMaterials(scene.modelInstance.materials);
		ui.setAnimations(scene.modelInstance.animations);
		ui.setNodes(NodeUtil.getAllNodes(new Array<Node>(), scene.modelInstance));
		ui.setCameras(scene.cameras);
		ui.setLights(scene.lights);
		
		// XXX force shader provider to compile new shaders based on model
		setShader(shaderMode);
		
		sceneManager.addScene(scene);
	}
	
	protected void setCamera(String name) 
	{
		if(name == null) return;
		if(name.isEmpty()){
			PerspectiveCamera camera = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			camera.up.set(Vector3.Y);
			
			BoundingBox bb = scene.modelInstance.calculateBoundingBox(new BoundingBox());
			
			Vector3 center = bb.getCenter(new Vector3());
			camera.position.set(bb.max).sub(center).scl(3).add(center);
			camera.lookAt(center);
			
			float size = Math.max(bb.getWidth(), Math.max(bb.getHeight(), bb.getDepth()));
			camera.near = size / 1000f;
			camera.far = size * 30f;
			
			camera.update(true);
			
			cameraControl = new CameraInputController(camera);
			cameraControl.translateUnits = bb.max.dst(bb.min);
			cameraControl.target.set(center);
			
			
			sceneManager.setCamera(camera);
		}else{
			Camera camera = scene.getCamera(name);
			cameraControl = new CameraInputController(camera);
			sceneManager.setCamera(camera);
		}
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, cameraControl));
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
		sceneManager.updateViewport(width, height);
	}
	
	@Override
	public void render() {
		float delta = Gdx.graphics.getDeltaTime();
		stage.act();

		sceneManager.update(delta);
		
		if(cameraControl != null){
			cameraControl.update();
		}
		
		float l = 0f;
		
		Gdx.gl.glClearColor(l,l,l, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		sceneManager.setAmbiantLight(ui.ambiantSlider.getValue());
		
		float IBLScale = ui.lightFactorSlider.getValue();
		PBRShader.ScaleIBLAmbient.r = ui.debugAmbiantSlider.getValue() * IBLScale;
		PBRShader.ScaleIBLAmbient.g = ui.debugSpecularSlider.getValue() * IBLScale;
		
		if(sceneManager.directionalLights.size > 0){
			float lum = ui.lightSlider.getValue();
			sceneManager.directionalLights.first().color.set(lum, lum, lum, 1);
			sceneManager.directionalLights.first().direction.set(ui.lightDirectionControl.value).nor();
			sceneManager.directionalLights.first().color.r *= IBLScale;
			sceneManager.directionalLights.first().color.g *= IBLScale;
			sceneManager.directionalLights.first().color.b *= IBLScale;
		}

		sceneManager.render();
		
		stage.draw();
	}
	
}
