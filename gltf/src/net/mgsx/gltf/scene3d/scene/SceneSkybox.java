package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class SceneSkybox {

	private static final float BOX_SCALE = (float)(1.0 / Math.sqrt(2.0));

	private ModelInstance model;
	private Environment env;
	private ModelBatch ownBatch;
	
	public SceneSkybox(Cubemap cubemap){
		super();
		
		env = new Environment();
		env.set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
		env.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.WHITE));
		
		Material material = new Material();

		Model box = new ModelBuilder().createBox(BOX_SCALE, BOX_SCALE, BOX_SCALE, material, VertexAttributes.Usage.Position);
		model = new ModelInstance(box);
		
		ownBatch = createBatch("net/mgsx/gltf/shaders/skybox");
	}
	
	private static ModelBatch createBatch(String basePathName) {
		return new ModelBatch(Gdx.files.classpath(basePathName + ".vs"), Gdx.files.classpath(basePathName + ".fs"));
	}
	
	public void render(ModelBatch batch){
		batch.flush();
		
		// scale skybox to camera range.
		float s = batch.getCamera().far;
		model.transform.setToScaling(s, s, s);
		model.transform.setTranslation(batch.getCamera().position);
		model.calculateTransforms();
		
		// render skybox
		ownBatch.begin(batch.getCamera());
		ownBatch.render(model, env);
		ownBatch.end();
	}
	
}
