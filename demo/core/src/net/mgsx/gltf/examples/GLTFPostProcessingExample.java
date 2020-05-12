package net.mgsx.gltf.examples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

/**
 * Use as template which contains :
 * - camera controller and a loaded scene 
 * - custom shader (optional) 
 * - post processing (optional)
 */
public class GLTFPostProcessingExample extends ApplicationAdapter {

	private Viewport viewport;
	private SceneManager sceneManager;
	private PerspectiveCamera camera;
	private CameraInputController cameraController;
	private final Color clearColor = new Color(Color.BLACK);
	
	// post processing
	private FrameBuffer fbo;
	private Batch batch;
	private ShaderProgram effectShader;
	
	private final boolean postProcessingEnabled = false;
	private final boolean customShaderEnabled = false;
	
	@Override
	public void create() {
		
		sceneManager = createSceneManager();
		sceneManager.camera = camera = new PerspectiveCamera();
		camera.fieldOfView = 50;
		camera.near = 0.01f;
		camera.far = 10f;
		camera.position.set(1, 1, 1).scl(.1f);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);
		camera.update();
		
		// load user scene
		SceneAsset asset = new GLTFLoader().load(Gdx.files.internal("models/BoomBox/glTF/BoomBox.gltf"));
		sceneManager.addScene(new Scene(asset.scene));
		
		cameraController = new CameraInputController(camera);
		cameraController.translateUnits = .1f;
		Gdx.input.setInputProcessor(cameraController);

		viewport = new FitViewport(1000, 500, camera);
		
		// post processing
		if(postProcessingEnabled){
			fbo = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
			effectShader = new ShaderProgram(Gdx.files.classpath("shaders/effect.vs.glsl"), Gdx.files.classpath("shaders/effect.fs.glsl"));
			batch = new SpriteBatch();
		}
		
	}
	
	private SceneManager createSceneManager(){
		PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
		config.numBones = 32;
		config.numDirectionalLights = 1;
		config.numPointLights = 0;
		config.numSpotLights = 0;
		
		if(customShaderEnabled){
			// shader files should be located in core project resources (shaders folder)
			config.vertexShader = Gdx.files.classpath("shaders/custom.vs.glsl").readString();
			config.fragmentShader = Gdx.files.classpath("shaders/custom.fs.glsl").readString();
		}
		
		
		Config depthConfig = new Config();
		depthConfig.numBones = config.numBones;
		if(customShaderEnabled){
			// shader files should be located in core project resources (shaders folder)
			depthConfig.vertexShader = Gdx.files.classpath("shaders/custom-depth.vs.glsl").readString();
			depthConfig.fragmentShader = Gdx.files.classpath("shaders/custom-depth.vs.glsl").readString();
		}
		
		return new SceneManager(PBRShaderProvider.createDefault(config), PBRShaderProvider.createDefaultDepth(depthConfig));
	}
	
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}
	
	@Override
	public void render() {
		float delta = Gdx.graphics.getDeltaTime();
		cameraController.update();
		sceneManager.update(delta);
		
		// TODO fbo template ?
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		if(postProcessingEnabled){
			renderWithPostProcessing();
		}else{
			renderDefault();
		}
	}
	
	private void renderDefault(){
		viewport.apply();
		sceneManager.render();
	}
	
	private void renderWithPostProcessing() {
		
		// render scene
		sceneManager.renderShadows();
		fbo.begin();
		sceneManager.renderColors();
		fbo.end();
		
		effectShader.begin();
		setEffectUniforms();
		effectShader.end();
		
		viewport.apply();
		batch.setShader(effectShader);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.begin();
		batch.draw(fbo.getColorBufferTexture(), 0, 0, 1, 1, 0f, 0f, 1f, 1f);
		batch.end();
		batch.setShader(null);
	}

	private void setEffectUniforms() {
		// template
		effectShader.setUniformf("u_effectLevel", 0.9f);
	}

}
