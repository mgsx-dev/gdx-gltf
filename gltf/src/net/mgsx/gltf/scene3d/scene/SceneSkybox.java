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
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

import net.mgsx.gltf.scene3d.attributes.PBRMatrixAttribute;
import net.mgsx.gltf.scene3d.shaders.PBRShader;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class SceneSkybox implements RenderableProvider, Updatable, Disposable {

	private ShaderProvider shaderProvider;
	private boolean ownShaderProvider;
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
		createBox(cubemap);
	}

	/**
	 * Create a sky box with color space conversion settings.
	 * @param cubemap
	 * @param manualSRGB see {@link net.mgsx.gltf.scene3d.shaders.PBRShaderConfig#manualSRGB}
	 * @param gammaCorrection when true, {@link net.mgsx.gltf.scene3d.shaders.PBRShaderConfig#DEFAULT_GAMMA} is used.
	 */
	public SceneSkybox(Cubemap cubemap, SRGB manualSRGB, boolean gammaCorrection){
		createShaderProvider(manualSRGB, gammaCorrection ? PBRShaderConfig.DEFAULT_GAMMA : null);
		createBox(cubemap);
	}

	/**
	 * Create a sky box with an optional custom shader.
	 * @param cubemap
	 * @param shaderProvider when null, a default shader provider is used (without manual SRGB nor gamma correction). 
	 * when not null, caller is responsible to dispose it.
	 */
	public SceneSkybox(Cubemap cubemap, ShaderProvider shaderProvider){
		if(shaderProvider == null){
			createShaderProvider(SRGB.NONE, null);
		}else{
			this.shaderProvider = shaderProvider;
		}
		createBox(cubemap);
	}
	
	private void createShaderProvider(SRGB manualSRGB, Float gammaCorrection){
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
		ownShaderProvider = true;
		this.shaderProvider = new SkyboxShaderProvider(shaderConfig);
	}
	
	private void createBox(Cubemap cubemap){
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
	}
	
	private static class SkyboxShader extends DefaultShader {

		public SkyboxShader(Renderable renderable, Config config) {
			super(renderable, config, createPrefix(renderable, config) + createSkyBoxPrefix(renderable));
			register(PBRShader.envRotationUniform, PBRShader.envRotationSetter);
		}

		private static String createSkyBoxPrefix(Renderable renderable) {
			String prefix = "";
			if(renderable.environment.has(PBRMatrixAttribute.EnvRotation)){
				prefix += "#define ENV_ROTATION\n";
			}
			return prefix;
		}
	}
	
	private static class SkyboxShaderProvider extends DefaultShaderProvider {
		public SkyboxShaderProvider(Config config) {
			super(config);
		}
		@Override
		protected Shader createShader (final Renderable renderable) {
			return new SkyboxShader(renderable, config);
		}
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
		// assign shader
		box.shader = shaderProvider.getShader(box);
		renderables.add(box);
	}

	@Override
	public void dispose() {
		if(shaderProvider != null && ownShaderProvider) shaderProvider.dispose();
		boxModel.dispose();
	}

	public void setRotation(float azymuthAngleDegree) {
		PBRMatrixAttribute attribute = box.environment.get(PBRMatrixAttribute.class, PBRMatrixAttribute.EnvRotation);
		if(attribute != null){
			attribute.set(azymuthAngleDegree);
		}else{
			box.environment.set(PBRMatrixAttribute.createEnvRotation(azymuthAngleDegree));
		}
	}
	
	public void setRotation(Matrix4 envRotation) {
		PBRMatrixAttribute attribute = box.environment.get(PBRMatrixAttribute.class, PBRMatrixAttribute.EnvRotation);
		if(envRotation != null){
			if(attribute != null){
				attribute.matrix.set(envRotation);
			}else{
				box.environment.set(PBRMatrixAttribute.createEnvRotation(envRotation));
			}
		}else if(attribute != null){
			box.environment.remove(PBRMatrixAttribute.EnvRotation);
		}
	}
}
