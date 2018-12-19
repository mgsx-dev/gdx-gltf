package net.mgsx.gltf;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
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

import net.mgsx.gltf.demo.ModelEntry;
import net.mgsx.gltf.loaders.GLBLoader;
import net.mgsx.gltf.loaders.GLTFLoader;
import net.mgsx.gltf.scene3d.NodeAnimationPlus;
import net.mgsx.gltf.scene3d.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.PBRShader;
import net.mgsx.gltf.scene3d.PBRShaderProvider;
import net.mgsx.gltf.scene3d.Scene;
import net.mgsx.gltf.scene3d.SceneAsset;
import net.mgsx.gltf.scene3d.SceneManager;
import net.mgsx.gltf.scene3d.SceneSkybox;
import net.mgsx.gltf.ui.GLTFDemoUI;
import net.mgsx.gltf.util.EnvironmentUtil;
import net.mgsx.gltf.util.NodeUtil;

/**
 * some demo models : https://github.com/KhronosGroup/glTF-Sample-Models
 * addon Blender : 
 * - 2.79b- : https://github.com/KhronosGroup/glTF-Blender-Exporter
 * - 2.8+   : https://github.com/KhronosGroup/glTF-Blender-IO
 * 
 * spec V2 : https://github.com/KhronosGroup/glTF/tree/master/specification/2.0
 * 
 * @author mgsx
 *
 */

// TODO extract SceneManager : 1 active camera, 

public class GLTFDemo extends ApplicationAdapter
{
	
	// TODO use config file or something else ...
	
	private static String AUTOLOAD_ENTRY = "BoomBox"; // "BarramundiFish";
	private static String AUTOLOAD_VARIANT = "glTF";
	
	private static final boolean USE_DEFAULT_ENV_MAP = true;
	
	private static final String TAG = "GLTFDemo";
	
	public static enum ShaderMode{
		FLAT, GOURAUD, PHONG, PBR_MR, PBR_SG, 
	}
	
	private ShaderMode shaderMode = ShaderMode.PBR_MR;
	
	private String samplesPath;
	
	private Stage stage;
	private Skin skin;
	private Array entries;
	
	private FileHandle rootFolder;
	private CameraInputController cameraControl;
	
	private Scene scene;
	
	private SceneAsset rootModel;

	private SceneManager sceneManager;
	private GLTFDemoUI ui;
	
	public GLTFDemo() {
		this("models");
	}
	
	public GLTFDemo(String samplesPath) {
		this.samplesPath = samplesPath;
	}
	
	@Override
	public void create() {
		
		createSceneManager();
		
		createUI();
		
		loadModelIndex();
		
		if(AUTOLOAD_ENTRY != null && AUTOLOAD_VARIANT != null){
			load(AUTOLOAD_ENTRY, AUTOLOAD_VARIANT);
		}
	}
	
	private void createSceneManager()
	{
		sceneManager = new SceneManager(createShaderProvider(shaderMode, 12));
		
		// set environment maps
		
		Cubemap diffuseCubemap = EnvironmentUtil.createCubemap(new ClasspathFileHandleResolver(), 
				"net/mgsx/gltf/assets/diffuse/diffuse_", "_0.jpg");

		Cubemap defaultEnvironmentCubemap = EnvironmentUtil.createCubemap(new ClasspathFileHandleResolver(), 
				"net/mgsx/gltf/assets/environment/environment_", "_0.png");

		Cubemap altEnvironmentCubemap = EnvironmentUtil.createCubemap(new ClasspathFileHandleResolver(), 
				"net/mgsx/gltf/assets/demo_skybox_", ".png");
		
		Cubemap environmentCubemap = USE_DEFAULT_ENV_MAP ? defaultEnvironmentCubemap : altEnvironmentCubemap;
		
		Cubemap mipmapCubemap = EnvironmentUtil.createCubemap(new ClasspathFileHandleResolver(), 
				"net/mgsx/gltf/assets/specular/specular_", "_", ".jpg", 10);
		
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
		
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(mipmapCubemap));

		sceneManager.setSkyBox(new SceneSkybox(environmentCubemap));
		
		// light direction based on environnement map SUN
		sceneManager.directionalLights.first().direction.set(-.5f,-.5f,-.7f).nor();
	}
	
	private void loadModelIndex() 
	{
		rootFolder = Gdx.files.internal(samplesPath);	
		
		FileHandle file = rootFolder.child("model-index.json");
		
		entries = new Json().fromJson(Array.class, ModelEntry.class, file);
		
		ui.entrySelector.setItems(entries);
	}

	private void createUI()
	{
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);
		skin = new Skin(Gdx.files.internal("skins/uiskin.json"));
		Table base = new Table(skin);
		base.setFillParent(true);
		
		base.defaults().expand().top().left();
		
		stage.addActor(base);
		
		base.add(ui = new GLTFDemoUI(skin));
		
		ui.shaderSelector.setSelected(shaderMode);
		
		ui.entrySelector.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				ui.setEntry(ui.entrySelector.getSelected(), rootFolder);
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
	}
	
	private void setShader(ShaderMode shaderMode) {
		sceneManager.batch.dispose();
		sceneManager.batch = new ModelBatch(createShaderProvider(shaderMode, rootModel.maxBones));
	}
	
	private ShaderProvider createShaderProvider(ShaderMode shaderMode, int maxBones){
		switch(shaderMode){
		default:
		case FLAT:
		case GOURAUD:
		case PHONG:
			Config config = new DefaultShader.Config();
			config.numBones = maxBones;
			return new DefaultShaderProvider(config);
		case PBR_SG:
			// TODO SG shader variant
		case PBR_MR:
			return PBRShaderProvider.createDefault(maxBones);
		}
	}

	private void load(String entryName, String variant) {
		for(ModelEntry item : ui.entrySelector.getItems()){
			if(item.name.equals(entryName)){
				load(item, variant);
				return;
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

	private void load(ModelEntry entry, String variant) {
		
		if(scene != null){
			sceneManager.removeScene(scene);
		}
		
		if(rootModel != null){
			rootModel.dispose();
		}
		
		if(variant.isEmpty()) return;
		String fileName = entry.variants.get(variant);
		
		Gdx.app.log(TAG, "loading " + fileName);
		
		FileHandle baseFolder = rootFolder.child(entry.name).child(variant);
		
		FileHandle glFile = baseFolder.child(fileName);
		
		if(glFile.extension().equals("gltf")){
			rootModel = new GLTFLoader().load(glFile, baseFolder);
		}else if(glFile.extension().equals("glb")){
			rootModel = new GLBLoader().load(glFile);
		}else{
			throw new GdxRuntimeException("unknown file extension " + glFile.extension());
		}

		scene = new Scene(rootModel.scene, true);
		
		// XXX patch animation because of overload ....
		for(Animation anim : rootModel.scene.animations){
			Animation newAnim = new Animation();
			newAnim.id = anim.id;
			newAnim.duration = anim.duration;
			for(NodeAnimation nodeAnim : anim.nodeAnimations){
				NodeAnimationPlus newNodeAnim = new NodeAnimationPlus();
				newNodeAnim.set(nodeAnim);
				newAnim.nodeAnimations.add(newNodeAnim);
			}
			scene.modelInstance.animations.add(anim);
		}
		
		ui.setMaterials(scene.modelInstance.materials);
		ui.setAnimations(rootModel.animations);
		ui.setCameras(rootModel.cameraMap);
		ui.setNodes(NodeUtil.getAllNodes(new Array<Node>(), scene.modelInstance));
		
		ui.lightDirectionControl.set(sceneManager.directionalLights.first().direction);
		
		setShader(shaderMode);
		
		sceneManager.addScene(scene);
		
		Gdx.app.log(TAG, "loaded " + glFile.path());
	}
	
	protected void setCamera(String name) 
	{
		if(name == null) return;
		if(name.isEmpty()){
			PerspectiveCamera camera = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			camera.up.set(Vector3.Y);
			
			BoundingBox bb = scene.modelInstance.calculateBoundingBox(new BoundingBox());
			//BoundingBox bb = new BoundingBox(new Vector3(-10, -10, -10), new Vector3(10, 10, 10));
			
			Vector3 center = bb.getCenter(new Vector3());
			camera.position.set(bb.max).sub(center).scl(3).add(center);
			camera.lookAt(center);
			cameraControl = new CameraInputController(camera);
			cameraControl.translateUnits = bb.max.dst(bb.min);
			cameraControl.target.set(center);
			
			sceneManager.setCamera(camera);
		}else{
			Camera camera = rootModel.createCamera(name);
			Node cameraNode = scene.modelInstance.getNode(name, true);
			cameraControl = new CameraInputController(camera);
			sceneManager.setCamera(camera, cameraNode);
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
		
		float l = .5f;
		
		Gdx.gl.glClearColor(l,l,l, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		sceneManager.setAmbiantLight(ui.ambiantSlider.getValue());
		
		float IBLScale = ui.lightFactorSlider.getValue();
		PBRShader.ScaleIBLAmbient.r = ui.ambiantSlider.getValue() * IBLScale;
		PBRShader.ScaleIBLAmbient.g = ui.specularSlider.getValue() * IBLScale;
		
		// XXX
		sceneManager.camera.near = .01f;
		sceneManager.camera.far = 100000f;
		sceneManager.camera.update();
		
		// dirLight.direction.nor(); //.set(-4, -64, -4).nor();
		
		float lum = ui.lightSlider.getValue();
		sceneManager.directionalLights.first().color.set(lum, lum, lum, 1);
		sceneManager.directionalLights.first().direction.set(ui.lightDirectionControl.value).nor();
		
		sceneManager.directionalLights.first().color.r *= IBLScale;
		sceneManager.directionalLights.first().color.g *= IBLScale;
		sceneManager.directionalLights.first().color.b *= IBLScale;
		
		sceneManager.render();
		
		stage.draw();
	}
	
}
