package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

public class SceneSkybox implements RenderableProvider, Updatable, Disposable {

	private DefaultShaderProvider shaderProvider;
	private Model boxModel;
	private Renderable box;
	
	/**
	 * Create a sky box with a default shader.
	 * @param cubemap
	 */
	public SceneSkybox(Cubemap cubemap){
		this(cubemap, null);
	}
	
	/**
	 * Create a sky box with an optional custom shader.
	 * @param cubemap
	 * @param shaderProvider when null, a default shader provider is used. when not null, caller is responsible to dispose it.
	 */
	public SceneSkybox(Cubemap cubemap, ShaderProvider shaderProvider){
		
		// create shader provider if needed
		if(shaderProvider == null){
			Config shaderConfig = new Config();
			String basePathName = "net/mgsx/gltf/shaders/skybox";
			shaderConfig.vertexShader = Gdx.files.classpath(basePathName + ".vs.glsl").readString();
			shaderConfig.fragmentShader = Gdx.files.classpath(basePathName + ".fs.glsl").readString();
			shaderProvider = this.shaderProvider = new DefaultShaderProvider(shaderConfig);
		}
		
		// create box
		float boxScale = (float)(1.0 / Math.sqrt(2.0));
		boxModel = new ModelBuilder().createBox(boxScale, boxScale, boxScale, new Material(), VertexAttributes.Usage.Position);
		box = boxModel.nodes.first().parts.first().setRenderable(new Renderable());
		
		// assign environment
		Environment env = new Environment();
		env.set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
		env.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.WHITE));
		box.environment = env;
		
		// set hint to render last but before transparent ones
		box.userData = SceneRenderableSorter.Hints.OPAQUE_LAST;
		
		// set material options : preserve background depth
		box.material = new Material(ColorAttribute.createDiffuse(Color.WHITE));
		box.material.set(new DepthTestAttribute(false));
		
		
		// assign shader
		box.shader = shaderProvider.getShader(box);
	}
	
	public SceneSkybox set(Cubemap cubemap){
		box.environment.set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
		return this;
	}
	
	/**
	 * @return skybox material color to be modified (default is white)
	 */
	public Color getColor(){
		return box.material.get(ColorAttribute.class, ColorAttribute.Diffuse).color;
	}
	
	@Override
	public void update(Camera camera, float delta){
		// scale skybox to camera range.
		float s = camera.far * (float)Math.sqrt(2.0);
		box.worldTransform.setToScaling(s, s, s);
		box.worldTransform.setTranslation(camera.position);
	}
	
	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		renderables.add(box);
	}

	@Override
	public void dispose() {
		if(shaderProvider != null) shaderProvider.dispose();
		boxModel.dispose();
	}
}
