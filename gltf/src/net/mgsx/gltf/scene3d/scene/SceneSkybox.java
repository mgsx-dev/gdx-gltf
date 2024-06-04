package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attributes;
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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;

import net.mgsx.gltf.scene3d.attributes.PBRMatrixAttribute;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class SceneSkybox implements RenderableProvider, Updatable, Disposable {
	
	public static void enableMipmaps(Cubemap cubemap) {
		cubemap.bind();
		Gdx.gl.glGenerateMipmap(GL20.GL_TEXTURE_CUBE_MAP);
		cubemap.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
	}
	
	/**
	 * Dynamically change cubemap mipmap bias.
	 * Cubemap must have mipmaps and according texture filter to take effect.
	 * You could use {@link #enableMipmaps(Cubemap)} convenient method to generate automatic mipmaps.
	 * SceneSkybox must also have to be created with lod option enabled.
	 * A positive lodBias value will render smaller mipmap layers causing a kind of blur effect.
	 * While a negative value will shift to bigger mipmaps layers.
	 */
	public float lodBias = 0;
	
	/**
	 * Environment used by skybox. Useful to enable some features like blending.
	 */
	public final Environment environment = new Environment();

	private final Matrix4 directionInverse = new Matrix4();
	private final Matrix4 envRotationInverse = new Matrix4();
	private boolean lodEnabled;
	private Model quadModel;
	private Renderable quad;

	private ShaderProvider shaderProvider;
	private boolean ownShaderProvider;
	
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
	 * @param lod enable level of details capability (requires GLES 3+)
	 */
	public SceneSkybox(Cubemap cubemap, SRGB manualSRGB, Float gammaCorrection, boolean lod){
		lodEnabled = lod;
		createShaderProvider(manualSRGB, gammaCorrection);
		createQuad(cubemap);
	}

	/**
	 * Create a sky box with color space conversion settings.
	 * @param cubemap
	 * @param manualSRGB see {@link net.mgsx.gltf.scene3d.shaders.PBRShaderConfig#manualSRGB}
	 * @param gammaCorrection when true, {@link net.mgsx.gltf.scene3d.shaders.PBRShaderConfig#DEFAULT_GAMMA} is used.
	 * @param lod enable level of details capability (requires GLES 3+)
	 */
	public SceneSkybox(Cubemap cubemap, SRGB manualSRGB, boolean gammaCorrection, boolean lod){
		lodEnabled = lod;
		createShaderProvider(manualSRGB, gammaCorrection ? PBRShaderConfig.DEFAULT_GAMMA : null);
		createQuad(cubemap);
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
		createQuad(cubemap);
	}
	
	private void createShaderProvider(SRGB manualSRGB, Float gammaCorrection){
		String prefix;
		if(lodEnabled){
			if(!Gdx.graphics.getGLVersion().isVersionEqualToOrHigher(3, 0)) {
				throw new IllegalArgumentException("GDX-GLTF Skybox LOD requires GLES 3+");
			}
			if(Gdx.app.getType() == ApplicationType.Desktop){
				prefix = "#version 130\n";
			}else {
				prefix = "#version 300 es\n";
			}
			prefix += "#define GLSL3\n";
		}else {
			prefix = "";
		}
		
		if(manualSRGB != SRGB.NONE){
			prefix += "#define MANUAL_SRGB\n";
			if(manualSRGB == SRGB.FAST){
				prefix += "#define SRGB_FAST_APPROXIMATION\n";
			}
		}
		if(gammaCorrection != null){
			prefix += "#define GAMMA_CORRECTION " + gammaCorrection + "\n";
		}
		
		if(lodEnabled) {
			prefix += "#define ENV_LOD\n";
		}
		
		Config shaderConfig = new Config();
		String basePathName = "net/mgsx/gltf/shaders/skybox";
		shaderConfig.vertexShader = Gdx.files.classpath(basePathName + ".vs.glsl").readString();
		shaderConfig.fragmentShader = Gdx.files.classpath(basePathName + ".fs.glsl").readString();
		ownShaderProvider = true;
		this.shaderProvider = new SkyboxShaderProvider(shaderConfig, prefix);
	}
	
	private void createQuad(Cubemap cubemap){
		// create screen quad
		quadModel = new ModelBuilder().createRect(
			-1,-1,0,
			1,-1,0,
			1,1,0,
			-1,1,0,
			0,0,-1,
			new Material(),
			VertexAttributes.Usage.Position);
		
		quad = quadModel.nodes.first().parts.first().setRenderable(new Renderable());
		
		// assign environment
		environment.set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
		quad.environment = environment;
		
		// set hint to render last but before transparent ones
		quad.userData = SceneRenderableSorter.Hints.OPAQUE_LAST;
		
		// set material options : preserve background depth
		quad.material = new Material(ColorAttribute.createDiffuse(Color.WHITE));
		quad.material.set(new DepthTestAttribute(false));
	}
	
	private class SkyboxShader extends DefaultShader {
		private int u_lod;
		
		public SkyboxShader(Renderable renderable, Config config) {
			super(renderable, config);
		}
		
		@Override
		public void init() {
			super.init();
			u_lod = Gdx.gl.glGetUniformLocation(program.getHandle(), "u_lod");
		}

		@Override
		protected void bindMaterial(Attributes attributes) {
			super.bindMaterial(attributes);
			if(u_lod >= 0) program.setUniformf(u_lod, lodBias);
		}
		
	}
	
	private class SkyboxShaderProvider extends DefaultShaderProvider {
		private String fsPrefix;
		public SkyboxShaderProvider(Config config, String fsPrefix) {
			super(config);
			this.fsPrefix = fsPrefix;
		}
		@Override
		protected Shader createShader (final Renderable renderable) {
			String oldFS = ShaderProgram.prependFragmentCode;
			ShaderProgram.prependFragmentCode = fsPrefix;
			SkyboxShader shader = new SkyboxShader(renderable, config);
			ShaderProgram.prependFragmentCode = oldFS;
			return shader;
		}
	}
	
	public SceneSkybox set(Cubemap cubemap){
		quad.environment.set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
		return this;
	}
	
	/**
	 * @return skybox material color to be modified (default is white)
	 */
	public Color getColor(){
		return quad.material.get(ColorAttribute.class, ColorAttribute.Diffuse).color;
	}
	
	@Override
	public void update(Camera camera, float delta){
		
		directionInverse.set(camera.view);
		directionInverse.setTranslation(0, 0, 0);
		
		PBRMatrixAttribute a = quad.environment.get(PBRMatrixAttribute.class, PBRMatrixAttribute.EnvRotation);
		if(a != null) {
			directionInverse.mul(envRotationInverse.set(a.matrix).tra());
		}
		
		quad.worldTransform.set(camera.projection).mul(directionInverse).inv();
		quad.worldTransform.val[Matrix4.M22] = 1f;
	}
	
	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		// assign shader
		quad.shader = shaderProvider.getShader(quad);
		renderables.add(quad);
	}

	@Override
	public void dispose() {
		if(shaderProvider != null && ownShaderProvider) shaderProvider.dispose();
		quadModel.dispose();
	}

	public void setRotation(float azymuthAngleDegree) {
		PBRMatrixAttribute attribute = quad.environment.get(PBRMatrixAttribute.class, PBRMatrixAttribute.EnvRotation);
		if(attribute != null){
			attribute.set(azymuthAngleDegree);
		}else{
			quad.environment.set(PBRMatrixAttribute.createEnvRotation(azymuthAngleDegree));
		}
	}
	
	public void setRotation(Matrix4 envRotation) {
		PBRMatrixAttribute attribute = quad.environment.get(PBRMatrixAttribute.class, PBRMatrixAttribute.EnvRotation);
		if(envRotation != null){
			if(attribute != null){
				attribute.matrix.set(envRotation);
			}else{
				quad.environment.set(PBRMatrixAttribute.createEnvRotation(envRotation));
			}
		}else if(attribute != null){
			quad.environment.remove(PBRMatrixAttribute.EnvRotation);
		}
	}
}
