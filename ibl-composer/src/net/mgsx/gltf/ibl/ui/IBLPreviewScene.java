package net.mgsx.gltf.ibl.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import net.mgsx.gltf.ibl.model.IBLSettings;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;

public class IBLPreviewScene {
	public final CameraInputController cameraController;
	private final PerspectiveCamera camera;
	private SceneManager sceneManager;
	private Viewport viewport;
	private SceneSkybox skybox;
	private IBLSettings settings;
	private DirectionalLightEx light;
	private Material material;
	private PBRColorAttribute baseColor;
	private PBRFloatAttribute metallic;
	private PBRFloatAttribute roughness;
	private Scene sphereScene;
	
	public IBLPreviewScene(IBLSettings settings) {
		this.settings = settings;
		camera = new PerspectiveCamera(settings.previewFov, 1, 1);
		camera.near = .001f;
		camera.far = 10f;
		camera.position.set(1,0,0).scl(2f);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);
		camera.update();
		cameraController = new CameraInputController(camera);
		
		sceneManager = new SceneManager();
		sceneManager.camera = camera;
		
		sceneManager.environment.add(light = new DirectionalLightEx());
		
		viewport = new ExtendViewport(1, 1, camera);
		
		// preview sphere
		float radius = 1f;
		int divU = 32;
		int divV = 16;
		ModelBuilder mb = new ModelBuilder();
		mb.begin();
		SphereShapeBuilder.build(mb.part("", GL20.GL_TRIANGLES, Usage.Position|Usage.Normal|Usage.TextureCoordinates, new Material()), radius, radius, radius, divU, divV);
		sphereScene = new Scene(mb.end(), false);
		
		// XXX references to attribute are lost during copy, better get them on the fly
		material = sphereScene.modelInstance.materials.first();
		material.set(baseColor = new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, Color.WHITE));
		material.set(metallic = new PBRFloatAttribute(PBRFloatAttribute.Metallic, 1f));
		material.set(roughness = new PBRFloatAttribute(PBRFloatAttribute.Roughness, 0f));

		sceneManager.addScene(sphereScene);
	}
	
	public void setBRDF(Texture brdf){
		if(brdf != null){
			sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdf));
		}else{
			sceneManager.environment.remove(PBRTextureAttribute.BRDFLUTTexture);
		}
	}
	
	public void setDiffuse(Cubemap irradianceMap){
		if(irradianceMap != null){
			sceneManager.environment.set(new PBRCubemapAttribute(PBRCubemapAttribute.DiffuseEnv, irradianceMap));
		}else{
			sceneManager.environment.remove(PBRCubemapAttribute.DiffuseEnv);
		}
	}
	
	public void setSpecular(Cubemap radianceMap){
		if(radianceMap != null){
			sceneManager.environment.set(new PBRCubemapAttribute(PBRCubemapAttribute.SpecularEnv, radianceMap));
		}else{
			sceneManager.environment.remove(PBRCubemapAttribute.SpecularEnv);
		}
	}
	
	public void resize(int width, int height) {
		viewport.update(width, height, false);
	}
	
	public void update(float delta){
		// camera
		cameraController.update();
		camera.fieldOfView = settings.previewFov;
		camera.update();

		// light
		settings.getLightDirection(light.direction);
		settings.getLightColor(light.baseColor);
		light.intensity = settings.previewLightIntensity;
		light.updateColor();
		
		sceneManager.setAmbientLight(settings.previewAmbient);
		
		// material
		metallic.value = settings.previewMetallic;
		roughness.value = settings.previewRoughness;
		baseColor.color.set(Color.WHITE).mul(settings.previewAlbedo);
		baseColor.color.a = 1;
		
		// scene
		sceneManager.update(delta);
	}
	
	public void render(){
		viewport.apply(false);
		sceneManager.renderColors();
	}

	public void setEnvMap(Cubemap envMap) {
		if(skybox == null){
			skybox = new SceneSkybox(envMap);
			sceneManager.setSkyBox(skybox);
		}else{
			skybox.set(envMap);
		}
	}
}
