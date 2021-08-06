package net.mgsx.gltf.procedural;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.SceneManager;

public class ProceduralExample extends ApplicationAdapter {
	public static void main(String[] args) {
		new LwjglApplication(new ProceduralExample());
	}

	private Model model;
	private SceneManager manager;
	private PerspectiveCamera camera;
	private CameraInputController cameraController;
	private ModelInstance modelInstance;
	
	@Override
	public void create() {
		ModelBuilder mb = new ModelBuilder();
		
		mb.begin();
		{
			Material material = new Material();
			material.set(PBRColorAttribute.createBaseColorFactor(new Color(Color.WHITE).fromHsv(15, .9f, .8f)));
			MeshPartBuilder mpb = mb.part("cube", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, material);
			BoxShapeBuilder.build(mpb, 1f, 1f, 1f);
		}
		model = mb.end();
		modelInstance = new ModelInstance(model);
		
		manager = new SceneManager(0);
		manager.setAmbientLight(0.01f);
		manager.environment.add(new DirectionalLightEx().set(Color.WHITE, new Vector3(-1,-4,-2), 5));
		
		manager.camera = camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(1,1,1).scl(3);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);
		camera.update();
		
		cameraController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(cameraController);
	}
	
	@Override
	public void dispose() {
		model.dispose();
	}
	
	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width;
		camera.viewportHeight = height;
	}
	
	@Override
	public void render() {
		
		cameraController.update();
		
		float delta = Gdx.graphics.getDeltaTime();
		manager.update(delta);
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		manager.getRenderableProviders().add(modelInstance);
		manager.render();
		manager.getRenderableProviders().clear();
	}
}
