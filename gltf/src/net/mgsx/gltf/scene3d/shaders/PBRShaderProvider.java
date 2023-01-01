package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFlagAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRHDRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRIridescenceAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRMatrixAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRVertexAttributes;
import net.mgsx.gltf.scene3d.attributes.PBRVolumeAttribute;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;
import net.mgsx.gltf.scene3d.utils.LightUtils;
import net.mgsx.gltf.scene3d.utils.LightUtils.LightsInfo;
import net.mgsx.gltf.scene3d.utils.ShaderParser;

import static net.mgsx.gltf.constant.CommonConstants.EOL;

public class PBRShaderProvider extends DefaultShaderProvider {

  public static final String TAG = "PBRShader";

  private static final LightsInfo lightsInfo = new LightsInfo();

  private static String defaultVertexShader = null;

  public static String getDefaultVertexShader() {
    if (defaultVertexShader == null) {
      defaultVertexShader = ShaderParser.parse(Gdx.files.classpath("net/mgsx/gltf/shaders/pbr/pbr.vs.glsl"));
    }
    return defaultVertexShader;
  }

  private static String defaultFragmentShader = null;

  public static String getDefaultFragmentShader() {
    if (defaultFragmentShader == null) {
      defaultFragmentShader = ShaderParser.parse(Gdx.files.classpath("net/mgsx/gltf/shaders/pbr/pbr.fs.glsl"));
    }
    return defaultFragmentShader;
  }

  public static PBRShaderConfig createDefaultConfig() {
    PBRShaderConfig config = new PBRShaderConfig();
    config.vertexShader = getDefaultVertexShader();
    config.fragmentShader = getDefaultFragmentShader();
    return config;
  }

  public static DepthShader.Config createDefaultDepthConfig() {
    return PBRDepthShaderProvider.createDefaultConfig();
  }

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
    if (this.config.vertexShader == null) {
      this.config.vertexShader = getDefaultVertexShader();
    }
    if (this.config.fragmentShader == null) {
      this.config.fragmentShader = getDefaultFragmentShader();
    }
  }

  public int getShaderCount() {
    return shaders.size;
  }

  public static String morphTargetsPrefix(Renderable renderable) {
    StringBuilder prefix = new StringBuilder();
    // TODO optimize double loop
    for (VertexAttribute att : renderable.meshPart.mesh.getVertexAttributes()) {
      for (int i = 0; i < PBRCommon.MAX_MORPH_TARGETS; i++) {
        if (att.usage == PBRVertexAttributes.Usage.PositionTarget && att.unit == i) {
          prefix.append("#define position").append(i).append("Flag").append(EOL);
        } else if (att.usage == PBRVertexAttributes.Usage.NormalTarget && att.unit == i) {
          prefix.append("#define normal").append(i).append("Flag").append(EOL);
        } else if (att.usage == PBRVertexAttributes.Usage.TangentTarget && att.unit == i) {
          prefix.append("#define tangent").append(i).append("Flag").append(EOL);
        }
      }
    }
    return prefix.toString();
  }

  /**
   * @return if target platform is running with at least OpenGL ES 3 (GLSL 300 es), WebGL 2.0 (GLSL 300 es)
   * or desktop OpenGL 3.0 (GLSL 130).
   */
  protected boolean isGL3() {
    return Gdx.graphics.getGLVersion().isVersionEqualToOrHigher(3, 0);
  }

  /**
   * override this method in order to add your own prefix.
   *
   * @param renderable
   * @param config
   */
  public String createPrefixBase(Renderable renderable, PBRShaderConfig config) {
    String defaultPrefix = DefaultShader.createPrefix(renderable, config);
    String version = config.glslVersion;
    if (isGL3()) {
      if (Gdx.app.getType() == ApplicationType.Desktop) {
        if (version == null) {
          version = "#version 130" + EOL + "#define GLSL3" + EOL;
        }
      } else if (Gdx.app.getType() == ApplicationType.Android ||
        Gdx.app.getType() == ApplicationType.iOS ||
        Gdx.app.getType() == ApplicationType.WebGL) {
        if (version == null) {
          version = "#version 300 es" + EOL + "#define GLSL3" + EOL;
        }
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
      prefix += "#define MANUAL_SRGB" + EOL;
      if (config.manualSRGB == SRGB.FAST) {
        prefix += "#define SRGB_FAST_APPROXIMATION" + EOL;
      }
    }
    if (config.manualGammaCorrection) {
      prefix += "#define GAMMA_CORRECTION " + config.gamma + EOL;
    }
    if (config.transmissionSRGB != SRGB.NONE) {
      prefix += "#define TS_MANUAL_SRGB" + EOL;
      if (config.transmissionSRGB == SRGB.FAST) {
        prefix += "#define TS_SRGB_FAST_APPROXIMATION" + EOL;
      }
    }
    return prefix;
  }

  @Override
  protected Shader createShader(Renderable renderable) {
    PBRShaderConfig config = (PBRShaderConfig) this.config;

    StringBuilder prefix = new StringBuilder(createPrefixBase(renderable, config));

    // Morph targets
    prefix.append(morphTargetsPrefix(renderable));

    // optional base color factor
    if (renderable.material.has(PBRColorAttribute.BaseColorFactor)) {
      prefix.append("#define baseColorFactorFlag").append(EOL);
    }

    // Lighting
    int primitiveType = renderable.meshPart.primitiveType;
    boolean isLineOrPoint = primitiveType == GL20.GL_POINTS || primitiveType == GL20.GL_LINES || primitiveType == GL20.GL_LINE_LOOP || primitiveType == GL20.GL_LINE_STRIP;
    boolean unlit = isLineOrPoint || renderable.material.has(PBRFlagAttribute.Unlit) || renderable.meshPart.mesh.getVertexAttribute(Usage.Normal) == null;

    if (unlit) {
      prefix.append("#define unlitFlag").append(EOL);
    } else {
      if (renderable.material.has(PBRTextureAttribute.MetallicRoughnessTexture)) {
        prefix.append("#define metallicRoughnessTextureFlag").append(EOL);
      }
      if (renderable.material.has(PBRTextureAttribute.OcclusionTexture)) {
        prefix.append("#define occlusionTextureFlag").append(EOL);
      }
      if (renderable.material.has(PBRFloatAttribute.TransmissionFactor)) {
        prefix.append("#define transmissionFlag").append(EOL);
      }
      if (renderable.material.has(PBRTextureAttribute.TransmissionTexture)) {
        prefix.append("#define transmissionTextureFlag").append(EOL);
      }
      if (renderable.material.has(PBRVolumeAttribute.Type)) {
        prefix.append("#define volumeFlag").append(EOL);
      }
      if (renderable.material.has(PBRTextureAttribute.ThicknessTexture)) {
        prefix.append("#define thicknessTextureFlag").append(EOL);
      }
      if (renderable.material.has(PBRFloatAttribute.IOR)) {
        prefix.append("#define iorFlag").append(EOL);
      }

      // Material specular
      boolean hasSpecular = false;
      if (renderable.material.has(PBRFloatAttribute.SpecularFactor)) {
        prefix.append("#define specularFactorFlag").append(EOL);
        hasSpecular = true;
      }
      if (renderable.material.has(PBRHDRColorAttribute.Specular)) {
        prefix.append("#define specularColorFlag").append(EOL);
        hasSpecular = true;
      }
      if (renderable.material.has(PBRTextureAttribute.SpecularFactorTexture)) {
        prefix.append("#define specularFactorTextureFlag").append(EOL);
        hasSpecular = true;
      }
      if (renderable.material.has(PBRTextureAttribute.SpecularColorTexture)) {
        prefix.append("#define specularColorTextureFlag").append(EOL);
        hasSpecular = true;
      }
      if (hasSpecular) {
        prefix.append("#define specularFlag").append(EOL);
      }

      // Material Iridescence
      if (renderable.material.has(PBRIridescenceAttribute.Type)) {
        prefix.append("#define iridescenceFlag").append(EOL);
      }
      if (renderable.material.has(PBRTextureAttribute.IridescenceTexture)) {
        prefix.append("#define iridescenceTextureFlag").append(EOL);
      }
      if (renderable.material.has(PBRTextureAttribute.IridescenceThicknessTexture)) {
        prefix.append("#define iridescenceThicknessTextureFlag").append(EOL);
      }

      // IBL options
      PBRCubemapAttribute specualarCubemapAttribute = null;
      if (renderable.environment != null) {
        if (renderable.environment.has(PBRTextureAttribute.TransmissionSourceTexture)) {
          prefix.append("#define transmissionSourceFlag").append(EOL);
        }
        if (renderable.environment.has(PBRCubemapAttribute.SpecularEnv)) {
          prefix.append("#define diffuseSpecularEnvSeparateFlag").append(EOL);
          specualarCubemapAttribute = renderable.environment.get(PBRCubemapAttribute.class, PBRCubemapAttribute.SpecularEnv);
        } else if (renderable.environment.has(PBRCubemapAttribute.DiffuseEnv)) {
          specualarCubemapAttribute = renderable.environment.get(PBRCubemapAttribute.class, PBRCubemapAttribute.DiffuseEnv);
        } else if (renderable.environment.has(PBRCubemapAttribute.EnvironmentMap)) {
          specualarCubemapAttribute = renderable.environment.get(PBRCubemapAttribute.class, PBRCubemapAttribute.EnvironmentMap);
        }
        if (specualarCubemapAttribute != null) {
          prefix.append("#define USE_IBL").append(EOL);

          boolean textureLodSupported;
          if (isGL3()) {
            textureLodSupported = true;
          } else if (Gdx.graphics.supportsExtension("EXT_shader_texture_lod")) {
            prefix.append("#define USE_TEXTURE_LOD_EXT").append(EOL);
            textureLodSupported = true;
          } else {
            textureLodSupported = false;
          }

          TextureFilter textureFilter = specualarCubemapAttribute.textureDescription.minFilter != null ? specualarCubemapAttribute.textureDescription.minFilter : specualarCubemapAttribute.textureDescription.texture.getMinFilter();
          if (textureLodSupported && textureFilter.equals(TextureFilter.MipMap)) {
            prefix.append("#define USE_TEX_LOD").append(EOL);
          }

          if (renderable.environment.has(PBRTextureAttribute.BRDFLUTTexture)) {
            prefix.append("#define brdfLUTTexture").append(EOL);
          }
        }
        // TODO check GLSL extension 'OES_standard_derivatives' for WebGL

        if (renderable.environment.has(ColorAttribute.AmbientLight)) {
          prefix.append("#define ambientLightFlag").append(EOL);
        }

        if (renderable.environment.has(PBRMatrixAttribute.EnvRotation)) {
          prefix.append("#define ENV_ROTATION").append(EOL);
        }
      }
    }

    // SRGB
    prefix.append(createPrefixSRGB(renderable, config));

    // multi UVs
    int maxUVIndex = -1;

    TextureAttribute diffuseAttribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Diffuse);
    if (diffuseAttribute != null) {
      prefix.append("#define v_diffuseUV v_texCoord").append(diffuseAttribute.uvIndex).append(EOL);
      maxUVIndex = Math.max(maxUVIndex, diffuseAttribute.uvIndex);
    }

    TextureAttribute emissiveAttribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Emissive);
    if (emissiveAttribute != null) {
      prefix.append("#define v_emissiveUV v_texCoord").append(emissiveAttribute.uvIndex).append(EOL);
      maxUVIndex = Math.max(maxUVIndex, emissiveAttribute.uvIndex);
    }

    TextureAttribute normalAttribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Normal);
    if (normalAttribute != null) {
      prefix.append("#define v_normalUV v_texCoord").append(normalAttribute.uvIndex).append(EOL);
      maxUVIndex = Math.max(maxUVIndex, normalAttribute.uvIndex);
    }

    TextureAttribute metallicRoughnessTextireAttribute = renderable.material.get(TextureAttribute.class, PBRTextureAttribute.MetallicRoughnessTexture);
    if (metallicRoughnessTextireAttribute != null) {
      prefix.append("#define v_metallicRoughnessUV v_texCoord").append(metallicRoughnessTextireAttribute.uvIndex).append(EOL);
      maxUVIndex = Math.max(maxUVIndex, metallicRoughnessTextireAttribute.uvIndex);
    }

    TextureAttribute occlusionTextureAttribute = renderable.material.get(TextureAttribute.class, PBRTextureAttribute.OcclusionTexture);
    if (occlusionTextureAttribute != null) {
      prefix.append("#define v_occlusionUV v_texCoord").append(occlusionTextureAttribute.uvIndex).append(EOL);
      maxUVIndex = Math.max(maxUVIndex, occlusionTextureAttribute.uvIndex);
    }

    TextureAttribute transmissionTextureAttribute = renderable.material.get(TextureAttribute.class, PBRTextureAttribute.TransmissionTexture);
    if (transmissionTextureAttribute != null) {
      prefix.append("#define v_transmissionUV v_texCoord").append(transmissionTextureAttribute.uvIndex).append(EOL);
      maxUVIndex = Math.max(maxUVIndex, transmissionTextureAttribute.uvIndex);
    }

    TextureAttribute thicknessTextureAttribute = renderable.material.get(TextureAttribute.class, PBRTextureAttribute.ThicknessTexture);
    if (thicknessTextureAttribute != null) {
      prefix.append("#define v_thicknessUV v_texCoord").append(thicknessTextureAttribute.uvIndex).append(EOL);
      maxUVIndex = Math.max(maxUVIndex, thicknessTextureAttribute.uvIndex);
    }

    TextureAttribute specularFactorTextureAttribute = renderable.material.get(TextureAttribute.class, PBRTextureAttribute.SpecularFactorTexture);
    if (specularFactorTextureAttribute != null) {
      prefix.append("#define v_specularFactorUV v_texCoord").append(specularFactorTextureAttribute.uvIndex).append(EOL);
      maxUVIndex = Math.max(maxUVIndex, specularFactorTextureAttribute.uvIndex);
    }

    TextureAttribute specularAttribute = renderable.material.get(TextureAttribute.class, PBRTextureAttribute.Specular);
    if (specularAttribute != null) {
      prefix.append("#define v_specularColorUV v_texCoord").append(specularAttribute.uvIndex).append(EOL);
      maxUVIndex = Math.max(maxUVIndex, specularAttribute.uvIndex);
    }

    TextureAttribute iridescenceTextureAttribute = renderable.material.get(TextureAttribute.class, PBRTextureAttribute.IridescenceTexture);
    if (iridescenceTextureAttribute != null) {
      prefix.append("#define v_iridescenceUV v_texCoord").append(iridescenceTextureAttribute.uvIndex).append(EOL);
      maxUVIndex = Math.max(maxUVIndex, iridescenceTextureAttribute.uvIndex);
    }

    TextureAttribute iridescenceThicknessTextureAttribute = renderable.material.get(TextureAttribute.class, PBRTextureAttribute.IridescenceThicknessTexture);
    if (iridescenceThicknessTextureAttribute != null) {
      prefix.append("#define v_iridescenceThicknessUV v_texCoord").append(iridescenceThicknessTextureAttribute.uvIndex).append(EOL);
      maxUVIndex = Math.max(maxUVIndex, iridescenceThicknessTextureAttribute.uvIndex);
    }

    if (maxUVIndex >= 0) {
      prefix.append("#define textureFlag").append(EOL);
    }
    if (maxUVIndex == 1) {
      prefix.append("#define textureCoord1Flag").append(EOL);
    } else if (maxUVIndex > 1) {
      throw new GdxRuntimeException("more than 2 texture coordinates attribute not supported");
    }

    // Fog
    if (renderable.environment != null && renderable.environment.has(FogAttribute.FogEquation)) {
      prefix.append("#define fogEquationFlag").append(EOL);
    }

    // colors
    for (VertexAttribute attribute : renderable.meshPart.mesh.getVertexAttributes()) {
      if (attribute.usage == VertexAttributes.Usage.ColorUnpacked) {
        prefix.append("#define color").append(attribute.unit).append("Flag").append(EOL);
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
      } else if (attribute.usage == PBRVertexAttributes.Usage.PositionTarget && attribute.unit >= PBRCommon.MAX_MORPH_TARGETS ||
        attribute.usage == PBRVertexAttributes.Usage.NormalTarget && attribute.unit >= PBRCommon.MAX_MORPH_TARGETS ||
        attribute.usage == PBRVertexAttributes.Usage.TangentTarget && attribute.unit >= PBRCommon.MAX_MORPH_TARGETS) {
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

    PBRShader shader = createShader(renderable, config, prefix.toString());
    checkShaderCompilation(shader.program);

    // prevent infinite loop (TODO remove this for libgdx 1.9.12+)
    if (!shader.canRender(renderable)) {
      throw new GdxRuntimeException("cannot render with this shader");
    }

    return shader;
  }

  /**
   * override this method in order to provide your own PBRShader subclass.
   *
   * @param renderable
   * @param config
   * @param prefix
   */
  protected PBRShader createShader(Renderable renderable, PBRShaderConfig config, String prefix) {
    return new PBRShader(renderable, config, prefix);
  }

  protected void checkShaderCompilation(ShaderProgram program) {
    String shaderLog = program.getLog();
    if (program.isCompiled()) {
      if (shaderLog.isEmpty()) {
        Gdx.app.debug(TAG, "Shader compilation success");
      } else {
        Gdx.app.error(TAG, "Shader compilation warnings:" + EOL + shaderLog);
      }
    } else {
      throw new GdxRuntimeException("Shader compilation failed:" + EOL + shaderLog);
    }
  }
}
