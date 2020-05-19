package net.mgsx.gltf.ibl;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;

public class IBLComposer extends ApplicationAdapter
{
	private Stage stage;
	private Skin skin;
	private Image imgRaw;
	private IBLGenerator hdr;
	private Table imgCube;
	private SceneManager scene;
	private SceneSkybox skyBox;
	private CameraInputController camControl;
	private PerspectiveCamera camera;
	private FrameBuffer fbo;
	private Image imgScene;
	private FrameBuffer fboPreview;
	private FrameBuffer fboPreview2;
	private Image imgPreview;
	private Image imgPreview2;
	private Scene sphereScene;
	private Scene sphereScene2;
	int fboSize = 256;
	private Environment sceneEnv;
	private DirectionalLightEx light;
	private PBRFloatAttribute metallic;
	private PBRFloatAttribute roughness;
	private PBRColorAttribute baseColor;
	
	private String HDRPath;
	
	public IBLComposer(String HDRPath) {
		this.HDRPath = HDRPath;
	}
	
	@Override
	public void create() {
		
		fbo = new FrameBuffer(Format.RGBA8888, fboSize, fboSize, true);
		fboPreview = new FrameBuffer(Format.RGBA8888, fboSize, fboSize, true);
		fboPreview2 = new FrameBuffer(Format.RGBA8888, fboSize, fboSize, true);
		
		hdr = new IBLGenerator(Gdx.files.absolute(HDRPath));
		
		
		Texture brdf = hdr.createBRDF(256, true);
		Pixmap brdfPacked = hdr.createBRDFPacked(brdf);
		// XXX test
		// PixmapIO.writePNG(Gdx.files.absolute("/tmp/brdf.png"), brdfPacked);
		
		brdf = hdr.createBRDFUnacked(brdfPacked);
		
		stage = new Stage();
		skin = new Skin(Gdx.files.internal("skins/uiskin.json"));
		
		Table root = new Table(skin);
		root.setFillParent(true);
		stage.addActor(root);
		
		hdr.setExposure(1f);
		Texture raw = hdr.createRaw();
		Cubemap envSky = hdr.createEnv(2048, true);
		Cubemap env = hdr.createEnv(1024, true);
		Cubemap irr = hdr.createIrradiance(env, 128);
		// env = hdr.createEnv(1024, true);
		Cubemap rad = hdr.createRadiance(env);
		
		// XXX brdf = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
		
		sceneEnv = new Environment();
		sceneEnv.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdf));
		sceneEnv.set(new PBRCubemapAttribute(PBRCubemapAttribute.DiffuseEnv, irr));
		sceneEnv.set(new PBRCubemapAttribute(PBRCubemapAttribute.SpecularEnv, rad));
		sceneEnv.add(light = new DirectionalLightEx());
		sceneEnv.set(new PBRColorAttribute(PBRColorAttribute.AmbientLight, Color.WHITE));
		
		// XXX imgRaw = new Image(raw);
		// imgRaw = new Image(hdr.createRadiancePacked(env));
		imgRaw = new Image(brdf);
		imgRaw.setScaling(Scaling.fit);
		
		camera = new PerspectiveCamera(67, 1, 1);
		camera.near = .001f;
		camera.far = 10f;
		camera.position.set(1,1,1).scl(.7f);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);
		camera.update();
		camControl = new CameraInputController(camera);
		
		scene = new SceneManager();
		scene.setSkyBox(skyBox = new SceneSkybox(envSky));
		
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, camControl));

		// 6 sides view
		imgCube = new CubemapUI(hdr.createEnvSeparated());
		
		imgScene = new Image();
		imgScene.setScale(1, -1);
		imgScene.setOrigin(fboSize/2, fboSize/2);
		imgScene.setScaling(Scaling.fit);
		
		imgPreview =  new Image();
		imgPreview.setScale(1, -1);
		imgPreview.setOrigin(fboSize/2, fboSize/2);
		imgPreview.setScaling(Scaling.fit);
		
		imgPreview2 =  new Image();
		imgPreview2.setScale(1, -1);
		imgPreview2.setOrigin(fboSize/2, fboSize/2);
		imgPreview2.setScaling(Scaling.fit);
		
		root.add(imgScene).width(fboSize).height(fboSize).colspan(2);
		root.add(imgCube).expandX().fill();
		root.row();
		root.add(imgPreview).size(fboSize);
		root.add(imgPreview2).size(fboSize);
		root.add(imgRaw).expandX().fill().row();
		
		ModelBuilder mb = new ModelBuilder();
		mb.begin();
		float radius = 1f;
		int divU = 32;
		int divV = 16;
		Material material = new Material();
		
		// TODO createEmissive doesn't work!! because bad factory conflicts!!!
		material.set(new PBRTextureAttribute(PBRTextureAttribute.EmissiveTexture, hdr.createRaw()));
		SphereShapeBuilder.build(mb.part("", GL20.GL_TRIANGLES, Usage.Position|Usage.TextureCoordinates, material), radius, radius, radius, divU, divV);
		sphereScene = new Scene(mb.end(), false);
		
		Material material2 = new Material();
		ModelBuilder mb2 = new ModelBuilder();
		mb2.begin();
		material2.set(baseColor = new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, Color.WHITE));
		material2.set(metallic = new PBRFloatAttribute(PBRFloatAttribute.Metallic, 1f));
		material2.set(roughness = new PBRFloatAttribute(PBRFloatAttribute.Roughness, 0f));
		SphereShapeBuilder.build(mb2.part("", GL20.GL_TRIANGLES, Usage.Position|Usage.Normal|Usage.TextureCoordinates, material2), radius, radius, radius, divU, divV);
		sphereScene2 = new Scene(mb2.end(), false);
		
		scene.setAmbientLight(0);
		
		root.debugAll();
		
	}
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height);
	}
	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camControl.update();
		scene.camera = camera;
		
		/*
		metallic.value = 4 / 10f;
		roughness.value = 4 / 10f;
		baseColor.color.set(1,.7f,.3f, 1);
		*/
		
		metallic.value = 10 / 10f;
		roughness.value = 3 / 10f;
		baseColor.color.set(1,1,1, 1);
		
		light.direction.set(-1,-.3f,0.6f).nor();
		light.baseColor.set(Color.WHITE);
		light.intensity = 1;
		light.updateColor();
		
		Material m = sphereScene2.modelInstance.nodes.first().parts.first().material;
		m.set(metallic);
		m.set(roughness);
		m.set(baseColor);
		
		camera.viewportWidth = camera.viewportHeight = fboSize;
		camera.update();
		
		scene.setSkyBox(skyBox);
		scene.update(Gdx.graphics.getDeltaTime());
		
		fbo.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		scene.renderColors();
		fbo.end();
		
		if(imgScene.getDrawable() == null)
			imgScene.setDrawable(new TextureRegionDrawable(fbo.getColorBufferTexture()));
		
		fboPreview.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		scene.setSkyBox(null);
		scene.addScene(sphereScene);
		scene.renderColors();
		scene.removeScene(sphereScene);
		fboPreview.end();

		fboPreview2.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Environment defaultEnv = scene.environment;
		scene.environment = sceneEnv;
		scene.update(0);
		scene.setSkyBox(skyBox);
		scene.addScene(sphereScene2);
		scene.renderColors();
		scene.removeScene(sphereScene2);
		scene.environment = defaultEnv;
		scene.update(0);
		fboPreview2.end();

		if(imgPreview.getDrawable() == null)
			imgPreview.setDrawable(new TextureRegionDrawable(fboPreview.getColorBufferTexture()));
		if(imgPreview2.getDrawable() == null)
			imgPreview2.setDrawable(new TextureRegionDrawable(fboPreview2.getColorBufferTexture()));

		stage.getViewport().apply();
		stage.act();
		stage.draw();
	}

}
