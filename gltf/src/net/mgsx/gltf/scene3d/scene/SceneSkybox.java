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

import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class SceneSkybox implements RenderableProvider, Updatable, Disposable {

	private ShaderProvider shaderProvider;
	private Model boxModel;
	private Renderable box;
	
	/**
	 * Create a sky box with a default shader.
	 * 
	 * @param cubemap
	 */
	public SceneSkybox(Cubemap cubemap){
		this(cubemap, null);
	}

	/**
	 * Create a sky box with color space conversion settings.
	 * @param cubemap
	 * @param manualSRGB see {@link net.mgsx.gltf.scene3d.shaders.PBRShaderConfig#manualSRGB}
	 * @param gammaCorrection when null, gamma correction is disabled.
	 * 		see {@link net.mgsx.gltf.scene3d.shaders.PBRShaderConfig#manualGammaCorrection}
	 */
	public SceneSkybox(Cubemap cubemap, SRGB manualSRGB, Float gammaCorrection){
		createShaderProvider(manualSRGB, gammaCorrection);
		createBox(cubemap, shaderProvider);
	}

	/**
	 * Create a sky box with color space conversion settings.
	 * @param cubemap
	 * @param manualSRGB see {@link net.mgsx.gltf.scene3d.shaders.PBRShaderConfig#manualSRGB}
	 * @param gammaCorrection when true, {@link net.mgsx.gltf.scene3d.shaders.PBRShaderConfig#DEFAULT_GAMMA} is used.
	 */
	public SceneSkybox(Cubemap cubemap, SRGB manualSRGB, boolean gammaCorrection){
		createShaderProvider(manualSRGB, gammaCorrection ? PBRShaderConfig.DEFAULT_GAMMA : null);
		createBox(cubemap, shaderProvider);
	}

	/**
	 * Create a sky box with an optional custom shader.
	 * @param cubemap
	 * @param shaderProvider when null, a default shader provider is used (without manual SRGB nor gamma correction). 
	 * when not null, caller is responsible to dispose it.
	 */
	public SceneSkybox(Cubemap cubemap, ShaderProvider shaderProvider){
		if(shaderProvider == null){
			shaderProvider = createShaderProvider(SRGB.NONE, null);
		}
		createBox(cubemap, shaderProvider);
	}
	
	private ShaderProvider createShaderProvider(SRGB manualSRGB, Float gammaCorrection){
		String prefix = "";
		if(manualSRGB != SRGB.NONE){
			prefix += "#define MANUAL_SRGB\n";
			if(manualSRGB == SRGB.FAST){
				prefix += "#define SRGB_FAST_APPROXIMATION\n";
			}
		}
		if(gammaCorrection != null){
			prefix += "#define GAMMA_CORRECTION " + gammaCorrection + "\n";
		}
		Config shaderConfig = new Config();
		String basePathName = "net/mgsx/gltf/shaders/skybox";
		shaderConfig.vertexShader = Gdx.files.classpath(basePathName + ".vs.glsl").readString();
		shaderConfig.fragmentShader = prefix + Gdx.files.classpath(basePathName + ".fs.glsl").readString();
		return this.shaderProvider = new DefaultShaderProvider(shaderConfig);
	}
	
	private void createBox(Cubemap cubemap, ShaderProvider shaderProvider){
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
