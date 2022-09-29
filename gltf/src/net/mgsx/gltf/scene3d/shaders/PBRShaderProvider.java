
package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Application.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.*;
import com.badlogic.gdx.graphics.VertexAttributes.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.shaders.*;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.utils.*;

import net.mgsx.gltf.scene3d.attributes.*;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.*;
import net.mgsx.gltf.scene3d.utils.*;
import net.mgsx.gltf.scene3d.utils.LightUtils.*;

public class PBRShaderProvider extends DefaultShaderProvider {
	public static final String TAG = "PBRShader";

	private final LightsInfo lightsInfo = new LightsInfo();

	private static String defaultVertexShader = null;

	public static String getDefaultVertexShader() {
		if (defaultVertexShader == null) defaultVertexShader = Gdx.files.classpath("net/mgsx/gltf/shaders/gdx-pbr.vs.glsl").readString();
		return defaultVertexShader;
	}

	private static String defaultFragmentShader = null;

	public static String getDefaultFragmentShader() {
		if (defaultFragmentShader == null) defaultFragmentShader = Gdx.files.classpath("net/mgsx/gltf/shaders/gdx-pbr.fs.glsl").readString();
		return defaultFragmentShader;
	}

	public static PBRShaderConfig createDefaultConfig() {
		PBRShaderConfig config = new PBRShaderConfig();
		config.vertexShader = getDefaultVertexShader();
		config.fragmentShader = getDefaultFragmentShader();
		return config;
	};

	public static DepthShader.Config createDefaultDepthConfig() {
		return PBRDepthShaderProvider.createDefaultConfig();
	};

	public static PBRShaderProvider createDefault(int maxBones) {
		PBRShaderConfig config = createDefaultConfig();
		config.numBones = maxBones;
		return createDefault(config);
	}

	public static PBRShaderProvider createDefault(PBRShaderConfig config) {
		return new PBRShaderProvider(config);
	}

	public static DepthShaderProvider createDefaultDepth(int maxBones) {
		DepthShader.Config config = createDefaultDepthConfig();
		config.numBones = maxBones;
		return createDefaultDepth(config);
	}

	public static DepthShaderProvider createDefaultDepth(DepthShader.Config config) {
		return new PBRDepthShaderProvider(config);
	}

	public PBRShaderProvider(PBRShaderConfig config) {
		super(config == null ? createDefaultConfig() : config);
		if (this.config.vertexShader == null) this.config.vertexShader = getDefaultVertexShader();
		if (this.config.fragmentShader == null) this.config.fragmentShader = getDefaultFragmentShader();
	}

	public int getShaderCount() {
		return shaders.size;
	}

	public static String morphTargetsPrefix(Renderable renderable) {
		String prefix = "";
		// TODO optimize double loop
		for (VertexAttribute att : renderable.meshPart.mesh.getVertexAttributes()) {
			for (int i = 0; i < PBRCommon.MAX_MORPH_TARGETS; i++) {
				if (att.usage == PBRVertexAttributes.Usage.PositionTarget && att.unit == i) {
					prefix += "#define " + "position" + i + "Flag\n";
				} else if (att.usage == PBRVertexAttributes.Usage.NormalTarget && att.unit == i) {
					prefix += "#define " + "normal" + i + "Flag\n";
				} else if (att.usage == PBRVertexAttributes.Usage.TangentTarget && att.unit == i) {
					prefix += "#define " + "tangent" + i + "Flag\n";
				}
			}
		}
		return prefix;
	}

	/** @return if target platform is running with at least OpenGL ES 3 (GLSL 300 es), WebGL 2.0 (GLSL 300 es) or desktop OpenGL 3.0 (GLSL 130). */
	protected boolean isGL3() {
		return Gdx.graphics.getGLVersion().isVersionEqualToOrHigher(3, 0);
	}

	/** override this method in order to add your own prefix.
	 * 
	 * @param renderable
	 * @param config */
	public String createPrefixBase(Renderable renderable, PBRShaderConfig config) {

		String defaultPrefix = DefaultShader.createPrefix(renderable, config);
		String version = config.glslVersion;
		if (isGL3()) {
			if (Gdx.app.getType() == ApplicationType.Desktop) {
				if (version == null) version = "#version 130\n" + "#define GLSL3\n";
			} else if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS || Gdx.app.getType() == ApplicationType.WebGL) {
				if (version == null) version = "#version 300 es\n" + "#define GLSL3\n";
			}
		}
		String prefix = "";
		if (version != null) prefix += version;
		if (config.prefix != null) prefix += config.prefix;
		prefix += defaultPrefix;

		return prefix;
	}

	public String createPrefixSRGB(Renderable renderable, PBRShaderConfig config) {
		String prefix = "";
		if (config.manualSRGB != SRGB.NONE) {
			prefix += "#define MANUAL_SRGB\n";
			if (config.manualSRGB == SRGB.FAST) {
				prefix += "#define SRGB_FAST_APPROXIMATION\n";
			}
		}
		if (config.manualGammaCorrection) {
			prefix += "#define GAMMA_CORRECTION " + config.gamma + "\n";
		}
		return prefix;
	}

	@Override
	protected Shader createShader(Renderable renderable) {

		PBRShaderConfig config = (PBRShaderConfig)this.config;

		String prefix = createPrefixBase(renderable, config);

		// Morph targets
		prefix += morphTargetsPrefix(renderable);

		// optional base color factor
		if (renderable.material.has(PBRColorAttribute.BaseColorFactor)) {
			prefix += "#define baseColorFactorFlag\n";
		}

		// Lighting
		int primitiveType = renderable.meshPart.primitiveType;
		boolean isLineOrPoint = primitiveType == GL20.GL_POINTS || primitiveType == GL20.GL_LINES || primitiveType == GL20.GL_LINE_LOOP || primitiveType == GL20.GL_LINE_STRIP;
		boolean unlit = isLineOrPoint || renderable.material.has(PBRFlagAttribute.Unlit) || renderable.meshPart.mesh.getVertexAttribute(Usage.Normal) == null;

		if (unlit) {

			prefix += "#define unlitFlag\n";

		} else {

			if (renderable.material.has(PBRTextureAttribute.MetallicRoughnessTexture)) {
				prefix += "#define metallicRoughnessTextureFlag\n";
			}
			if (renderable.material.has(PBRTextureAttribute.OcclusionTexture)) {
				prefix += "#define occlusionTextureFlag\n";
			}

			// IBL options
			PBRCubemapAttribute specualarCubemapAttribute = null;
			if (renderable.environment != null) {
				if (renderable.environment.has(PBRCubemapAttribute.SpecularEnv)) {
					prefix += "#define diffuseSpecularEnvSeparateFlag\n";
					specualarCubemapAttribute = renderable.environment.get(PBRCubemapAttribute.class, PBRCubemapAttribute.SpecularEnv);
				} else if (renderable.environment.has(PBRCubemapAttribute.DiffuseEnv)) {
					specualarCubemapAttribute = renderable.environment.get(PBRCubemapAttribute.class, PBRCubemapAttribute.DiffuseEnv);
				} else if (renderable.environment.has(PBRCubemapAttribute.EnvironmentMap)) {
					specualarCubemapAttribute = renderable.environment.get(PBRCubemapAttribute.class, PBRCubemapAttribute.EnvironmentMap);
				}
				if (specualarCubemapAttribute != null) {
					prefix += "#define USE_IBL\n";

					boolean textureLodSupported;
					if (isGL3()) {
						textureLodSupported = true;
					} else if (Gdx.graphics.supportsExtension("EXT_shader_texture_lod")) {
						prefix += "#define USE_TEXTURE_LOD_EXT\n";
						textureLodSupported = true;
					} else {
						textureLodSupported = false;
					}

					TextureFilter textureFilter = specualarCubemapAttribute.textureDescription.minFilter != null ? specualarCubemapAttribute.textureDescription.minFilter
						: specualarCubemapAttribute.textureDescription.texture.getMinFilter();
					if (textureLodSupported && textureFilter.equals(TextureFilter.MipMap)) {
						prefix += "#define USE_TEX_LOD\n";
					}

					if (renderable.environment.has(PBRTextureAttribute.BRDFLUTTexture)) {
						prefix += "#define brdfLUTTexture\n";
					}
				}
				// TODO check GLSL extension 'OES_standard_derivatives' for WebGL

				if (renderable.environment.has(ColorAttribute.AmbientLight)) {
					prefix += "#define ambientLightFlag\n";
				}

				if (renderable.environment.has(PBRMatrixAttribute.EnvRotation)) {
					prefix += "#define ENV_ROTATION\n";
				}
			}

		}

		// SRGB
		prefix += createPrefixSRGB(renderable, config);

		// multi UVs
		int maxUVIndex = -1;

		{
			TextureAttribute attribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Diffuse);
			if (attribute != null) {
				prefix += "#define v_diffuseUV v_texCoord" + attribute.uvIndex + "\n";
				maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex);
			}
		}
		{
			TextureAttribute attribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Emissive);
			if (attribute != null) {
				prefix += "#define v_emissiveUV v_texCoord" + attribute.uvIndex + "\n";
				maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex);
			}
		}
		{
			TextureAttribute attribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Normal);
			if (attribute != null) {
				prefix += "#define v_normalUV v_texCoord" + attribute.uvIndex + "\n";
				maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex);
			}
		}
		{
			TextureAttribute attribute = renderable.material.get(TextureAttribute.class, PBRTextureAttribute.MetallicRoughnessTexture);
			if (attribute != null) {
				prefix += "#define v_metallicRoughnessUV v_texCoord" + attribute.uvIndex + "\n";
				maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex);
			}
		}
		{
			TextureAttribute attribute = renderable.material.get(TextureAttribute.class, PBRTextureAttribute.OcclusionTexture);
			if (attribute != null) {
				prefix += "#define v_occlusionUV v_texCoord" + attribute.uvIndex + "\n";
				maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex);
			}
		}

		if (maxUVIndex >= 0) {
			prefix += "#define textureFlag\n";
		}
		if (maxUVIndex == 1) {
			prefix += "#define textureCoord1Flag\n";
		} else if (maxUVIndex > 1) {
			throw new GdxRuntimeException("more than 2 texture coordinates attribute not supported");
		}

		// Fog

		if (renderable.environment != null && renderable.environment.has(FogAttribute.FogEquation)) {
			prefix += "#define fogEquationFlag\n";
		}

		// colors
		for (VertexAttribute attribute : renderable.meshPart.mesh.getVertexAttributes()) {
			if (attribute.usage == VertexAttributes.Usage.ColorUnpacked) {
				prefix += "#define color" + attribute.unit + "Flag\n";
			}
		}

		//

		int numBoneInfluence = 0;
		int numMorphTarget = 0;
		int numColor = 0;

		for (VertexAttribute attribute : renderable.meshPart.mesh.getVertexAttributes()) {
			if (attribute.usage == VertexAttributes.Usage.ColorPacked) {
				throw new GdxRuntimeException("color packed attribute not supported");
			} else if (attribute.usage == VertexAttributes.Usage.ColorUnpacked) {
				numColor = Math.max(numColor, attribute.unit + 1);
			} else if (attribute.usage == PBRVertexAttributes.Usage.PositionTarget && attribute.unit >= PBRCommon.MAX_MORPH_TARGETS
				|| attribute.usage == PBRVertexAttributes.Usage.NormalTarget && attribute.unit >= PBRCommon.MAX_MORPH_TARGETS
				|| attribute.usage == PBRVertexAttributes.Usage.TangentTarget && attribute.unit >= PBRCommon.MAX_MORPH_TARGETS) {
				numMorphTarget = Math.max(numMorphTarget, attribute.unit + 1);
			} else if (attribute.usage == VertexAttributes.Usage.BoneWeight) {
				numBoneInfluence = Math.max(numBoneInfluence, attribute.unit + 1);
			}
		}

		PBRCommon.checkVertexAttributes(renderable);

		if (numBoneInfluence > 8) {
			Gdx.app.error(TAG, "more than 8 bones influence attributes not supported: " + numBoneInfluence + " found.");
		}
		if (numMorphTarget > PBRCommon.MAX_MORPH_TARGETS) {
			Gdx.app.error(TAG, "more than 8 morph target attributes not supported: " + numMorphTarget + " found.");
		}
		if (numColor > config.numVertexColors) {
			Gdx.app.error(TAG, "more than " + config.numVertexColors + " color attributes not supported: " + numColor + " found.");
		}

		if (renderable.environment != null) {
			LightUtils.getLightsInfo(lightsInfo, renderable.environment);
			if (lightsInfo.dirLights > config.numDirectionalLights) {
				Gdx.app.error(TAG, "too many directional lights detected: " + lightsInfo.dirLights + "/" + config.numDirectionalLights);
			}
			if (lightsInfo.pointLights > config.numPointLights) {
				Gdx.app.error(TAG, "too many point lights detected: " + lightsInfo.pointLights + "/" + config.numPointLights);
			}
			if (lightsInfo.spotLights > config.numSpotLights) {
				Gdx.app.error(TAG, "too many spot lights detected: " + lightsInfo.spotLights + "/" + config.numSpotLights);
			}
			if (lightsInfo.miscLights > 0) {
				Gdx.app.error(TAG, "unknow type lights not supported.");
			}
		}

		PBRShader shader = createShader(renderable, config, prefix);
		checkShaderCompilation(shader.program);

		// prevent infinite loop (TODO remove this for libgdx 1.9.12+)
		if (!shader.canRender(renderable)) {
			throw new GdxRuntimeException("cannot render with this shader");
		}

		return shader;
	}

	/** override this method in order to provide your own PBRShader subclass.
	 * 
	 * @param renderable
	 * @param config
	 * @param prefix */
	protected PBRShader createShader(Renderable renderable, PBRShaderConfig config, String prefix) {
		return new PBRShader(renderable, config, prefix);
	}

	protected void checkShaderCompilation(ShaderProgram program) {
		String shaderLog = program.getLog();
		if (program.isCompiled()) {
			if (shaderLog.isEmpty()) {
				Gdx.app.debug(TAG, "Shader compilation success");
			} else {
				Gdx.app.error(TAG, "Shader compilation warnings:\n" + shaderLog);
			}
		} else {
			throw new GdxRuntimeException("Shader compilation failed:\n" + shaderLog);
		}
	}
}
