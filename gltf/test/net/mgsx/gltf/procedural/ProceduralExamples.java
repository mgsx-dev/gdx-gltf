package net.mgsx.gltf.procedural;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import net.mgsx.gltf.loaders.shared.geometry.MeshTangentSpaceGenerator;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBREmissiveShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

/**
 * Examples of manual PBR material setup.
 * 
 * Press SPACE to change model.
 * Press C to switch from PBRShaderProvider to PBREmissiveShaderProvider.
 *  
 * @author mgsx
 */
public class ProceduralExamples extends ApplicationAdapter {
	public static void main(String[] args) {
		new LwjglApplication(new ProceduralExamples());
	}
	
	private boolean defaultShaderProvider = true;
	
	private SceneManager manager;
	private PerspectiveCamera camera;
	private CameraInputController cameraController;
	private ModelInstance modelInstance;
	
	private Array<Disposable> disposables = new Array<Disposable>();
	private Array<ModelInstance> models = new Array<ModelInstance>();
	
	@Override
	public void create() {
		
		Texture diffuseTexture = new Texture(Gdx.files.classpath("textures/red_bricks_04_diff_1k.jpg"), true);
		Texture normalTexture = new Texture(Gdx.files.classpath("textures/red_bricks_04_nor_gl_1k.jpg"), true);
		Texture mrTexture = new Texture(Gdx.files.classpath("textures/red_bricks_04_rough_1k.jpg"), true);
		
		disposables.add(diffuseTexture, normalTexture, mrTexture);
		
		// minimal box with empty material
		{
			ModelBuilder mb = new ModelBuilder();
			mb.begin();
			Material material = new Material();
			MeshPartBuilder mpb = mb.part("cube", GL20.GL_TRIANGLES, Usage.Position, material);
			BoxShapeBuilder.build(mpb, 1f, 1f, 1f);
			Model model = mb.end();
			disposables.add(model);
			models.add(new ModelInstance(model));
		}
		
		// unlit box with solid color
		{
			ModelBuilder mb = new ModelBuilder();
			mb.begin();
			Material material = new Material();
			material.set(PBRColorAttribute.createBaseColorFactor(new Color(Color.WHITE).fromHsv(15, .9f, .8f)));
			MeshPartBuilder mpb = mb.part("cube", GL20.GL_TRIANGLES, Usage.Position, material);
			BoxShapeBuilder.build(mpb, 1f, 1f, 1f);
			Model model = mb.end();
			disposables.add(model);
			models.add(new ModelInstance(model));
		}
		
		// lit box with solid color
		{
			ModelBuilder mb = new ModelBuilder();
			mb.begin();
			Material material = new Material();
			material.set(PBRColorAttribute.createBaseColorFactor(new Color(Color.WHITE).fromHsv(15, .9f, .8f)));
			MeshPartBuilder mpb = mb.part("cube", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, material);
			BoxShapeBuilder.build(mpb, 1f, 1f, 1f);
			Model model = mb.end();
			disposables.add(model);
			models.add(new ModelInstance(model));
		}
		
		// lit box with solid color and emissive
		{
			ModelBuilder mb = new ModelBuilder();
			mb.begin();
			Material material = new Material();
			material.set(PBRColorAttribute.createBaseColorFactor(new Color(Color.WHITE).fromHsv(15, .9f, .8f)));
			material.set(PBRColorAttribute.createEmissive(new Color(Color.RED)));
			MeshPartBuilder mpb = mb.part("cube", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, material);
			BoxShapeBuilder.build(mpb, 1f, 1f, 1f);
			Model model = mb.end();
			disposables.add(model);
			models.add(new ModelInstance(model));
		}
		
		// lit box with emissive only
		{
			ModelBuilder mb = new ModelBuilder();
			mb.begin();
			Material material = new Material();
			material.set(PBRColorAttribute.createEmissive(new Color(Color.RED)));
			MeshPartBuilder mpb = mb.part("cube", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, material);
			BoxShapeBuilder.build(mpb, 1f, 1f, 1f);
			Model model = mb.end();
			disposables.add(model);
			models.add(new ModelInstance(model));
		}
		
		// lit sphere with a diffuse texture
		{
			ModelBuilder mb = new ModelBuilder();
			mb.begin();
			Material material = new Material();
			material.set(PBRTextureAttribute.createBaseColorTexture(diffuseTexture));
			MeshPartBuilder mpb = mb.part("cube", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, material);
			SphereShapeBuilder.build(mpb, 1, 1, 1, 32, 16);
			Model model = mb.end();
			disposables.add(model);
			models.add(new ModelInstance(model));
		}
		
		// lit sphere with normal maps
		{
			ModelBuilder mb = new ModelBuilder();
			mb.begin();
			Material material = new Material();
			material.set(PBRColorAttribute.createBaseColorFactor(new Color(Color.WHITE).fromHsv(15, .9f, .8f)));
			material.set(PBRTextureAttribute.createNormalTexture(normalTexture));
			VertexAttributes attributes = new VertexAttributes(
				VertexAttribute.Position(),
				VertexAttribute.Normal(),
				new VertexAttribute(Usage.Tangent, 4, ShaderProgram.TANGENT_ATTRIBUTE),
				VertexAttribute.TexCoords(0)
				);
			MeshPartBuilder mpb = mb.part("sphere", GL20.GL_TRIANGLES, attributes, material);
			SphereShapeBuilder.build(mpb, 1, 1, 1, 32, 16);
			Model model = mb.end();
			// generate tangents
			for(Mesh mesh : model.meshes){
				MeshTangentSpaceGenerator.computeTangentSpace(mesh, material, false, true);
			}
			disposables.add(model);
			models.add(new ModelInstance(model));
		}
		
		// lit sphere with all textures
		{
			ModelBuilder mb = new ModelBuilder();
			mb.begin();
			Material material = new Material();
			material.set(PBRTextureAttribute.createBaseColorTexture(diffuseTexture));
			material.set(PBRTextureAttribute.createNormalTexture(normalTexture));
			material.set(PBRTextureAttribute.createMetallicRoughnessTexture(mrTexture));
			VertexAttributes attributes = new VertexAttributes(
				VertexAttribute.Position(),
				VertexAttribute.Normal(),
				new VertexAttribute(Usage.Tangent, 4, ShaderProgram.TANGENT_ATTRIBUTE),
				VertexAttribute.TexCoords(0)
				);
			MeshPartBuilder mpb = mb.part("sphere", GL20.GL_TRIANGLES, attributes, material);
			SphereShapeBuilder.build(mpb, 1, 1, 1, 32, 16);
			Model model = mb.end();
			// generate tangents
			for(Mesh mesh : model.meshes){
				MeshTangentSpaceGenerator.computeTangentSpace(mesh, material, false, true);
			}
			disposables.add(model);
			models.add(new ModelInstance(model));
		}

		
		modelInstance = models.first();
		
		manager = new SceneManager(0);
		manager.setAmbientLight(0.01f);
		manager.environment.add(new DirectionalLightEx().set(Color.WHITE, new Vector3(-1,-4,-2), 5));
		
		manager.camera = camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(1,1,1).scl(3);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);
		camera.near = .01f;
		camera.far = 100f;
		camera.update();
		
		cameraController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(cameraController);
	}
	
	@Override
	public void dispose() {
		for(Disposable d : disposables){
			d.dispose();
		}
		manager.dispose();
	}
	
	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width;
		camera.viewportHeight = height;
	}
	
	@Override
	public void render() {
		
		if(Gdx.input.isKeyJustPressed(Keys.SPACE)){
			int index = models.indexOf(modelInstance, true);
			index = (index + 1) % models.size;
			modelInstance = models.get(index);
		}
		if(Gdx.input.isKeyJustPressed(Keys.C)){
			defaultShaderProvider = !defaultShaderProvider;
			if(defaultShaderProvider){
				manager.setShaderProvider(PBRShaderProvider.createDefault(0));
			}else{
				manager.setShaderProvider(new PBREmissiveShaderProvider(PBREmissiveShaderProvider.createConfig(0)));
			}
		}
		
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
