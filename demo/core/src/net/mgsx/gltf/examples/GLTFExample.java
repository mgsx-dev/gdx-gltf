package net.mgsx.gltf.examples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;

public class GLTFExample extends ApplicationAdapter
{
	private SceneManager sceneManager;
	private SceneAsset sceneAsset;
	private Scene scene;
	private PerspectiveCamera camera;
	private Cubemap diffuseCubemap;
	private Cubemap environmentCubemap;
	private Cubemap specularCubemap;
	private Texture brdfLUT;
	private float time;
	private SceneSkybox skybox;

	@Override
	public void create() {
		
		// create scene
		sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/BoomBox/glTF/BoomBox.gltf"));
		scene = new Scene(sceneAsset.scene);
		sceneManager = new SceneManager();
		sceneManager.addScene(scene);
		
		// setup camera
		camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		float d = .02f;
		camera.near = d / 1000f;
		camera.far = d * 4;
		sceneManager.setCamera(camera);
		
		// setup IBL (image based lighting)
		environmentCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				"textures/environment/environment_", "_0.png", EnvironmentUtil.FACE_NAMES_NEG_POS);
		diffuseCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				"textures/diffuse/diffuse_", "_0.jpg", EnvironmentUtil.FACE_NAMES_NEG_POS);
		specularCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				"textures/specular/specular_", "_", ".jpg", 10, EnvironmentUtil.FACE_NAMES_NEG_POS);
		brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
		
		sceneManager.setAmbientLight(1f);
		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
		
		// setup skybox
		skybox = new SceneSkybox(environmentCubemap);
		sceneManager.setSkyBox(skybox);
	}
	
	@Override
	public void resize(int width, int height) {
		sceneManager.updateViewport(width, height);
	}
	
	@Override
	public void render() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		time += deltaTime;
		
		// animate camera
		camera.position.setFromSpherical(MathUtils.PI/4, time * .3f).scl(.02f);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);
		camera.update();
		
		// render
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		sceneManager.update(deltaTime);
		sceneManager.render();
	}
	
	@Override
	public void dispose() {
		sceneManager.dispose();
		sceneAsset.dispose();
		environmentCubemap.dispose();
		diffuseCubemap.dispose();
		specularCubemap.dispose();
		brdfLUT.dispose();
		skybox.dispose();
	}
}
