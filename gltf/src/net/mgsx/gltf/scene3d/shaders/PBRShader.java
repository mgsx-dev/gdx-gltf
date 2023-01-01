package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRHDRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRIridescenceAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRMatrixAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRVertexAttributes;
import net.mgsx.gltf.scene3d.attributes.PBRVolumeAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.model.WeightVector;

public class PBRShader extends DefaultShader {

  private static final Vector2 v2 = new Vector2();

  public static final Uniform baseColorTextureUniform = new Uniform("u_diffuseTexture", PBRTextureAttribute.BaseColorTexture);
  public static final Setter baseColorTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute attribute = (TextureAttribute) (combinedAttributes.get(PBRTextureAttribute.BaseColorTexture));
      final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
      shader.set(inputID, unit);
    }
  };

  public static final Uniform baseColorFactorUniform = new Uniform("u_BaseColorFactor", PBRColorAttribute.BaseColorFactor);
  public static final Setter baseColorFactorSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      ColorAttribute attribute = combinedAttributes.get(ColorAttribute.class, PBRColorAttribute.BaseColorFactor);
      Color color = attribute == null ? Color.WHITE : attribute.color;
      shader.set(inputID, color);
    }
  };

  public static final Uniform emissiveTextureUniform = new Uniform("u_emissiveTexture", PBRTextureAttribute.EmissiveTexture);
  public static final Setter emissiveTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute attribute = (TextureAttribute) (combinedAttributes.get(PBRTextureAttribute.EmissiveTexture));
      final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
      shader.set(inputID, unit);
    }
  };

  public static final Uniform normalTextureUniform = new Uniform("u_normalTexture", PBRTextureAttribute.NormalTexture);
  public static final Setter normalTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute attribute = (TextureAttribute) (combinedAttributes.get(PBRTextureAttribute.NormalTexture));
      final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
      shader.set(inputID, unit);
    }
  };

  public static final Uniform metallicRoughnessTextureUniform = new Uniform("u_MetallicRoughnessSampler", PBRTextureAttribute.MetallicRoughnessTexture);
  public static final Setter metallicRoughnessTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute attribute = (TextureAttribute) (combinedAttributes.get(PBRTextureAttribute.MetallicRoughnessTexture));
      final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
      shader.set(inputID, unit);
    }
  };

  public static final Uniform metallicRoughnessUniform = new Uniform("u_MetallicRoughnessValues");
  public static final Setter metallicRoughnessSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRFloatAttribute metallicAttribute = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.Metallic);
      PBRFloatAttribute roughnessAttribute = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.Roughness);
      float metallic = metallicAttribute == null ? 1f : metallicAttribute.value;
      float roughness = roughnessAttribute == null ? 1f : roughnessAttribute.value;
      shader.set(inputID, v2.set(metallic, roughness));
    }
  };

  public static final Uniform normalScaleUniform = new Uniform("u_NormalScale");
  public static final Setter normalScaleSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRFloatAttribute normalScaleAttribute = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.NormalScale);
      float normalScale = normalScaleAttribute == null ? 1f : normalScaleAttribute.value;
      shader.set(inputID, normalScale);
    }
  };

  public static final Uniform occlusionStrengthUniform = new Uniform("u_OcclusionStrength");
  public static final Setter occlusionStrengthSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRFloatAttribute occlusionStrengthAttribute = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.OcclusionStrength);
      float occlusionStrength = occlusionStrengthAttribute == null ? 1f : occlusionStrengthAttribute.value;
      shader.set(inputID, occlusionStrength);
    }
  };


  public static final Uniform occlusionTextureUniform = new Uniform("u_OcclusionSampler", PBRTextureAttribute.OcclusionTexture);
  public static final Setter occlusionTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute attribute = (TextureAttribute) (combinedAttributes.get(PBRTextureAttribute.OcclusionTexture));
      final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
      shader.set(inputID, unit);
    }
  };

  public static final Uniform diffuseEnvTextureUniform = new Uniform("u_DiffuseEnvSampler", PBRCubemapAttribute.DiffuseEnv);
  public static final Setter diffuseEnvTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRCubemapAttribute diffuseEnvAttribute = combinedAttributes.get(PBRCubemapAttribute.class, PBRCubemapAttribute.DiffuseEnv);
      final int unit = shader.context.textureBinder.bind(diffuseEnvAttribute.textureDescription);
      shader.set(inputID, unit);
    }

  };

  public static final Uniform specularEnvTextureUniform = new Uniform("u_SpecularEnvSampler", PBRCubemapAttribute.SpecularEnv);
  public static final Setter specularEnvTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRCubemapAttribute specularEnvAttribute = combinedAttributes.get(PBRCubemapAttribute.class, PBRCubemapAttribute.SpecularEnv);
      final int unit = shader.context.textureBinder.bind(specularEnvAttribute.textureDescription);
      shader.set(inputID, unit);
    }

  };

  public static final Uniform envRotationUniform = new Uniform("u_envRotation", PBRMatrixAttribute.EnvRotation);
  public static final Setter envRotationSetter = new LocalSetter() {

    private final Matrix3 mat3 = new Matrix3();

    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRMatrixAttribute attribute = combinedAttributes.get(PBRMatrixAttribute.class, PBRMatrixAttribute.EnvRotation);
      shader.set(inputID, mat3.set(attribute.matrix));
    }
  };

  public static final Uniform brdfLUTTextureUniform = new Uniform("u_brdfLUT");
  public static final Setter brdfLUTTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRTextureAttribute attribute = combinedAttributes.get(PBRTextureAttribute.class, PBRTextureAttribute.BRDFLUTTexture);
      if (attribute != null) {
        final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
        shader.set(inputID, unit);
      }
    }
  };

  public static final Uniform shadowBiasUniform = new Uniform("u_shadowBias");
  public static final Setter shadowBiasSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRFloatAttribute attribute = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.ShadowBias);
      float value = attribute == null ? 0f : attribute.value;
      shader.set(inputID, value);
    }
  };

  public static final Uniform fogEquationUniform = new Uniform("u_fogEquation");
  public static final Setter fogEquationSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      FogAttribute attribute = combinedAttributes.get(FogAttribute.class, FogAttribute.FogEquation);
      Vector3 value = attribute == null ? Vector3.Zero : attribute.value;
      shader.set(inputID, value);
    }
  };

  // override default setter in order to scale by emissive intensity
  public static final Setter emissiveScaledColor = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      ColorAttribute emissive = combinedAttributes.get(ColorAttribute.class, ColorAttribute.Emissive);
      PBRFloatAttribute emissiveIntensity = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.EmissiveIntensity);
      if (emissiveIntensity != null) {
        shader.set(
          inputID,
          emissive.color.r * emissiveIntensity.value,
          emissive.color.g * emissiveIntensity.value,
          emissive.color.b * emissiveIntensity.value,
          emissive.color.a * emissiveIntensity.value
        );
      } else {
        shader.set(inputID, emissive.color);
      }
    }
  };

  public static final Uniform transmissionFactorUniform = new Uniform("u_transmissionFactor");
  public static final Setter transmissionFactorSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRFloatAttribute a = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.TransmissionFactor);
      float value = a == null ? 0f : a.value;
      shader.set(inputID, value);
    }
  };

  public static final Uniform transmissionTextureUniform = new Uniform("u_transmissionSampler", PBRTextureAttribute.TransmissionTexture);
  public static final Setter transmissionTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute attribute = (TextureAttribute) (combinedAttributes.get(PBRTextureAttribute.TransmissionTexture));
      final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
      shader.set(inputID, unit);
    }
  };

  public static final Uniform iorUniform = new Uniform("u_ior");
  public static final Setter iorSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRFloatAttribute a = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.IOR);
      shader.set(inputID, a.value);
    }
  };

  public static final Uniform thicknessFactorUniform = new Uniform("u_thicknessFactor");
  public static final Setter thicknessFactorSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRVolumeAttribute a = combinedAttributes.get(PBRVolumeAttribute.class, PBRVolumeAttribute.Type);
      shader.set(inputID, a.thicknessFactor);
    }
  };

  public static final Uniform volumeDistanceUniform = new Uniform("u_attenuationDistance");
  public static final Setter volumeDistanceSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRVolumeAttribute a = combinedAttributes.get(PBRVolumeAttribute.class, PBRVolumeAttribute.Type);
      shader.set(inputID, a.attenuationDistance);
    }
  };

  public static final Uniform volumeColorUniform = new Uniform("u_attenuationColor");
  public static final Setter volumeColorSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRVolumeAttribute a = combinedAttributes.get(PBRVolumeAttribute.class, PBRVolumeAttribute.Type);
      shader.set(inputID, a.attenuationColor.r, a.attenuationColor.g, a.attenuationColor.b);
    }
  };

  public static final Uniform thicknessTextureUniform = new Uniform("u_thicknessSampler", PBRTextureAttribute.ThicknessTexture);
  public static final Setter thicknessTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute attribute = (TextureAttribute) (combinedAttributes.get(PBRTextureAttribute.ThicknessTexture));
      final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
      shader.set(inputID, unit);
    }
  };

  public static final Uniform specularFactorUniform = new Uniform("u_specularFactor");
  public static final Setter specularFactorSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRFloatAttribute a = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.SpecularFactor);
      shader.set(inputID, a.value);
    }
  };

  public static final Uniform specularColorFactorUniform = new Uniform("u_specularColorFactor");
  public static final Setter specularColorFactorSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRHDRColorAttribute a = combinedAttributes.get(PBRHDRColorAttribute.class, PBRHDRColorAttribute.Specular);
      shader.set(inputID, a.r, a.g, a.b);
    }
  };

  public static final Uniform specularFactorTextureUniform = new Uniform("u_specularFactorSampler", PBRTextureAttribute.SpecularFactorTexture);
  public static final Setter specularFactorTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute attribute = (TextureAttribute) (combinedAttributes.get(PBRTextureAttribute.SpecularFactorTexture));
      final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
      shader.set(inputID, unit);
    }
  };

  public static final Uniform specularColorTextureUniform = new Uniform("u_specularColorSampler", PBRTextureAttribute.Specular);
  public static final Setter specularColorTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute attribute = (TextureAttribute) (combinedAttributes.get(PBRTextureAttribute.Specular));
      final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
      shader.set(inputID, unit);
    }
  };

  public static final Uniform iridescenceFactorUniform = new Uniform("u_iridescenceFactor");
  public static final Setter iridescenceFactorSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRIridescenceAttribute a = combinedAttributes.get(PBRIridescenceAttribute.class, PBRIridescenceAttribute.Type);
      shader.set(inputID, a.factor);
    }
  };

  public static final Uniform iridescenceIORUniform = new Uniform("u_iridescenceIOR");
  public static final Setter iridescenceIORSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRIridescenceAttribute a = combinedAttributes.get(PBRIridescenceAttribute.class, PBRIridescenceAttribute.Type);
      shader.set(inputID, a.ior);
    }
  };

  public static final Uniform iridescenceThicknessMinUniform = new Uniform("u_iridescenceThicknessMin");
  public static final Setter iridescenceThicknessMinSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRIridescenceAttribute a = combinedAttributes.get(PBRIridescenceAttribute.class, PBRIridescenceAttribute.Type);
      shader.set(inputID, a.thicknessMin);
    }
  };

  public static final Uniform iridescenceThicknessMaxUniform = new Uniform("u_iridescenceThicknessMax");
  public static final Setter iridescenceThicknessMaxSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      PBRIridescenceAttribute a = combinedAttributes.get(PBRIridescenceAttribute.class, PBRIridescenceAttribute.Type);
      shader.set(inputID, a.thicknessMax);
    }
  };

  public static final Uniform iridescenceTextureUniform = new Uniform("u_iridescenceSampler", PBRTextureAttribute.IridescenceTexture);
  public static final Setter iridescenceTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute attribute = (TextureAttribute) (combinedAttributes.get(PBRTextureAttribute.IridescenceTexture));
      final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
      shader.set(inputID, unit);
    }
  };

  public static final Uniform iridescenceThicknessTextureUniform = new Uniform("u_iridescenceThicknessSampler", PBRTextureAttribute.IridescenceThicknessTexture);
  public static final Setter iridescenceThicknessTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute attribute = (TextureAttribute) (combinedAttributes.get(PBRTextureAttribute.IridescenceThicknessTexture));
      final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
      shader.set(inputID, unit);
    }
  };

  public static final Uniform transmissionSourceTextureUniform = new Uniform("u_transmissionSourceSampler", PBRTextureAttribute.TransmissionSourceTexture);
  public static final Setter transmissionSourceTextureSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute attribute = (TextureAttribute) (combinedAttributes.get(PBRTextureAttribute.TransmissionSourceTexture));
      final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
      shader.set(inputID, unit);
    }
  };

  public static final Uniform transmissionSourceMipmapUniform = new Uniform("u_transmissionSourceMipmapScale");
  public static final Setter transmissionSourceMipmapSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      TextureAttribute a = combinedAttributes.get(PBRTextureAttribute.class, PBRTextureAttribute.TransmissionSourceTexture);
      float mipmapFactor;
      if (a != null) {
        mipmapFactor = (float) (Math.log(a.textureDescription.texture.getWidth()) / Math.log(2.0));
      } else {
        mipmapFactor = 1;
      }
      shader.set(inputID, mipmapFactor);
    }
  };

  public static final Uniform projViewTransUniform = new Uniform("u_projViewTrans");
  public static final Setter projViewTransSetter = new LocalSetter() {
    @Override
    public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
      shader.set(inputID, shader.camera.combined);
    }
  };

  private static final PBRTextureAttribute[] transformTexture = {null, null};

  public final int u_metallicRoughness;
  public final int u_occlusionStrength;
  public final int u_metallicRoughnessTexture;
  public final int u_occlusionTexture;
  public final int u_DiffuseEnvSampler;
  public final int u_SpecularEnvSampler;
  public final int u_envRotation;
  public final int u_brdfLUTTexture;
  public final int u_NormalScale;
  public final int u_BaseColorTexture;
  public final int u_NormalTexture;
  public final int u_EmissiveTexture;
  public final int u_BaseColorFactor;
  public final int u_FogEquation;
  public final int u_ShadowBias;

  // morph targets
  private int u_morphTargets1;
  private int u_morphTargets2;

  private int u_mipmapScale;

  private int u_texCoord0Transform;
  private int u_texCoord1Transform;

  private int u_ambientLight;

  private long textureCoordinateMapMask;

  private long morphTargetsMask;

  private int vertexColorLayers;

  public int u_emissive;

  public int u_transmissionFactor;
  public int u_transmissionTexture;

  public int u_ior;

  // Volume
  public int u_thicknessTexture;
  public int u_thicknessFactor;
  public int u_volumeDistance;
  public int u_volumeColor;

  // Specular
  public int u_specularFactor;
  public int u_specularColorFactor;
  public int u_specularFactorTexture;
  public int u_specularColorTexture;

  // Iridescence
  public int u_iridescenceFactor;
  public int u_iridescenceIOR;
  public int u_iridescenceThicknessMin;
  public int u_iridescenceThicknessMax;
  public int u_iridescenceTexture;
  public int u_iridescenceThicknessTexture;

  public int u_transmissionSourceTexture;
  public int u_transmissionSourceMipmap;

  private static final Matrix3 textureTransform = new Matrix3();

  public PBRShader(Renderable renderable, Config config, String prefix) {
    super(renderable, config, prefix);

    textureCoordinateMapMask = getTextureCoordinateMapMask(renderable.material);

    morphTargetsMask = computeMorphTargetsMask(renderable);

    vertexColorLayers = computeVertexColorLayers(renderable);

    // base color
    u_BaseColorTexture = register(baseColorTextureUniform, baseColorTextureSetter);
    u_BaseColorFactor = register(baseColorFactorUniform, baseColorFactorSetter);

    // emissive
    u_EmissiveTexture = register(emissiveTextureUniform, emissiveTextureSetter);

    // environment
    u_DiffuseEnvSampler = register(diffuseEnvTextureUniform, diffuseEnvTextureSetter);
    u_SpecularEnvSampler = register(specularEnvTextureUniform, specularEnvTextureSetter);
    u_envRotation = register(envRotationUniform, envRotationSetter);

    // metallic roughness
    u_metallicRoughness = register(metallicRoughnessUniform, metallicRoughnessSetter);
    u_metallicRoughnessTexture = register(metallicRoughnessTextureUniform, metallicRoughnessTextureSetter);

    // occlusion
    u_occlusionTexture = register(occlusionTextureUniform, occlusionTextureSetter);
    u_occlusionStrength = register(occlusionStrengthUniform, occlusionStrengthSetter);

    u_brdfLUTTexture = register(brdfLUTTextureUniform, brdfLUTTextureSetter);

    // normal map
    u_NormalTexture = register(normalTextureUniform, normalTextureSetter);
    u_NormalScale = register(normalScaleUniform, normalScaleSetter);

    u_ShadowBias = register(shadowBiasUniform, shadowBiasSetter);

    u_FogEquation = register(fogEquationUniform, fogEquationSetter);

    u_emissive = register(Inputs.emissiveColor, emissiveScaledColor);

    u_transmissionFactor = register(transmissionFactorUniform, transmissionFactorSetter);
    u_transmissionTexture = register(transmissionTextureUniform, transmissionTextureSetter);

    u_ior = register(iorUniform, iorSetter);

    u_thicknessFactor = register(thicknessFactorUniform, thicknessFactorSetter);
    u_volumeDistance = register(volumeDistanceUniform, volumeDistanceSetter);
    u_volumeColor = register(volumeColorUniform, volumeColorSetter);
    u_thicknessTexture = register(thicknessTextureUniform, thicknessTextureSetter);

    u_transmissionSourceTexture = register(transmissionSourceTextureUniform, transmissionSourceTextureSetter);
    u_transmissionSourceMipmap = register(transmissionSourceMipmapUniform, transmissionSourceMipmapSetter);


    // specular
    u_specularFactor = register(specularFactorUniform, specularFactorSetter);
    u_specularColorFactor = register(specularColorFactorUniform, specularColorFactorSetter);
    u_specularFactorTexture = register(specularFactorTextureUniform, specularFactorTextureSetter);
    u_specularColorTexture = register(specularColorTextureUniform, specularColorTextureSetter);

    // iridescence
    u_iridescenceFactor = register(iridescenceFactorUniform, iridescenceFactorSetter);
    u_iridescenceIOR = register(iridescenceIORUniform, iridescenceIORSetter);
    u_iridescenceThicknessMin = register(iridescenceThicknessMinUniform, iridescenceThicknessMinSetter);
    u_iridescenceThicknessMax = register(iridescenceThicknessMaxUniform, iridescenceThicknessMaxSetter);
    u_iridescenceTexture = register(iridescenceTextureUniform, iridescenceTextureSetter);
    u_iridescenceThicknessTexture = register(iridescenceThicknessTextureUniform, iridescenceThicknessTextureSetter);
  }

  private int computeVertexColorLayers(Renderable renderable) {
    int num = 0;
    VertexAttributes vertexAttributes = renderable.meshPart.mesh.getVertexAttributes();
    final int n = vertexAttributes.size();
    for (int i = 0; i < n; i++) {
      final VertexAttribute attr = vertexAttributes.get(i);
      if (attr.usage == VertexAttributes.Usage.ColorUnpacked) {
        num++;
      }
    }
    return num;
  }

  @Override
  public boolean canRender(Renderable renderable) {
    // TODO properly determine if current shader can render this renderable.

    // compare texture coordinates mapping
    long textureCoordinateMapMask = getTextureCoordinateMapMask(renderable.material);
    if (textureCoordinateMapMask != this.textureCoordinateMapMask) {
      return false;
    }

    // compare morph targets
    if (this.morphTargetsMask != computeMorphTargetsMask(renderable)) {
      return false;
    }

    // compare vertex colors count
    if (this.vertexColorLayers != computeVertexColorLayers(renderable)) {
      return false;
    }

    return super.canRender(renderable);
  }

  public long computeMorphTargetsMask(Renderable renderable) {
    int morphTargetsFlag = 0;
    VertexAttributes vertexAttributes = renderable.meshPart.mesh.getVertexAttributes();
    final int n = vertexAttributes.size();
    for (int i = 0; i < n; i++) {
      final VertexAttribute attr = vertexAttributes.get(i);
      if (attr.usage == PBRVertexAttributes.Usage.PositionTarget) {
        morphTargetsFlag |= (1 << attr.unit);
      }
      if (attr.usage == PBRVertexAttributes.Usage.NormalTarget) {
        morphTargetsFlag |= (1 << (attr.unit + 8));
      }
      if (attr.usage == PBRVertexAttributes.Usage.TangentTarget) {
        morphTargetsFlag |= (1 << (attr.unit + 16));
      }
    }
    return morphTargetsFlag;
  }

  private static final long[] allTextureTypes = {
    PBRTextureAttribute.BaseColorTexture,
    PBRTextureAttribute.EmissiveTexture,
    PBRTextureAttribute.NormalTexture,
    PBRTextureAttribute.MetallicRoughnessTexture,
    PBRTextureAttribute.OcclusionTexture,
    PBRTextureAttribute.TransmissionTexture,
    PBRTextureAttribute.ThicknessTexture,
    PBRTextureAttribute.IridescenceTexture,
    PBRTextureAttribute.IridescenceThicknessTexture
  };

  private static long getTextureCoordinateMapMask(Attributes attributes) {
    // encode texture coordinate unit in a 5 bits integer.
    // 5 texture types with 1 bits per texture type.
    // 0 means no texture or unit 0
    // 1 means unit 1
    // only 2 units are supported.
    long mask = 0L;
    int maskShift = 0;
    for (long textureType : allTextureTypes) {
      PBRTextureAttribute attribute = attributes.get(PBRTextureAttribute.class, textureType);
      if (attribute != null) {
        mask |= (long) (attribute.uvIndex & 1) << maskShift;
      }
      maskShift++;
    }
    return mask;
  }

  @Override
  public void init(ShaderProgram program, Renderable renderable) {
    super.init(program, renderable);
    u_mipmapScale = program.fetchUniformLocation("u_mipmapScale", false);

    u_texCoord0Transform = program.fetchUniformLocation("u_texCoord0Transform", false);
    u_texCoord1Transform = program.fetchUniformLocation("u_texCoord1Transform", false);

    u_morphTargets1 = program.fetchUniformLocation("u_morphTargets1", false);
    u_morphTargets2 = program.fetchUniformLocation("u_morphTargets2", false);

    u_ambientLight = program.fetchUniformLocation("u_ambientLight", false);
  }

  @Override
  protected void bindMaterial(Attributes attributes) {
    super.bindMaterial(attributes);

    // TODO texCoords should be mapped in vertex shader to allow separated UV transform

    transformTexture[0] = null;
    transformTexture[1] = null;

    for (long textureType : allTextureTypes) {
      PBRTextureAttribute attribute = attributes.get(PBRTextureAttribute.class, textureType);
      if (attribute != null) {
        transformTexture[attribute.uvIndex] = attribute;
      }
    }

    if (u_texCoord0Transform >= 0) {
      if (transformTexture[0] != null) {
        PBRTextureAttribute attribute = transformTexture[0];
        textureTransform.idt();
        textureTransform.translate(attribute.offsetU, attribute.offsetV);
        textureTransform.rotateRad(-attribute.rotationUV);
        textureTransform.scale(attribute.scaleU, attribute.scaleV);
      } else {
        textureTransform.idt();
      }
      program.setUniformMatrix(u_texCoord0Transform, textureTransform);
    }
    if (u_texCoord1Transform >= 0) {
      if (transformTexture[1] != null) {
        PBRTextureAttribute attribute = transformTexture[1];
        textureTransform.setToTranslation(attribute.offsetU, attribute.offsetV);
        textureTransform.rotateRad(-attribute.rotationUV);
        textureTransform.scale(attribute.scaleU, attribute.scaleV);
      } else {
        textureTransform.idt();
      }
      program.setUniformMatrix(u_texCoord1Transform, textureTransform);
    }
  }

  @Override
  public void render(Renderable renderable, Attributes combinedAttributes) {
    if (u_mipmapScale >= 0) {
      PBRCubemapAttribute specularEnv = combinedAttributes.get(PBRCubemapAttribute.class, PBRCubemapAttribute.SpecularEnv);
      float mipmapFactor;
      if (specularEnv != null) {
        mipmapFactor = (float) (Math.log(specularEnv.textureDescription.texture.getWidth()) / Math.log(2.0));
      } else {
        mipmapFactor = 1;
      }
      program.setUniformf(u_mipmapScale, mipmapFactor);
    }

    if (u_morphTargets1 >= 0) {
      if (renderable.userData instanceof WeightVector) {
        WeightVector weightVector = (WeightVector) renderable.userData;
        program.setUniformf(u_morphTargets1, weightVector.get(0), weightVector.get(1), weightVector.get(2), weightVector.get(3));
      } else {
        program.setUniformf(u_morphTargets1, 0, 0, 0, 0);
      }
    }
    if (u_morphTargets2 >= 0) {
      if (renderable.userData instanceof WeightVector) {
        WeightVector weightVector = (WeightVector) renderable.userData;
        program.setUniformf(u_morphTargets2, weightVector.get(4), weightVector.get(5), weightVector.get(6), weightVector.get(7));
      } else {
        program.setUniformf(u_morphTargets2, 0, 0, 0, 0);
      }
    }

    super.render(renderable, combinedAttributes);
  }

  @Override
  protected void bindLights(Renderable renderable, Attributes attributes) {
    // XXX update color (to apply intensity) before default binding
    DirectionalLightsAttribute dla = attributes.get(DirectionalLightsAttribute.class, DirectionalLightsAttribute.Type);
    if (dla != null) {
      for (DirectionalLight light : dla.lights) {
        if (light instanceof DirectionalLightEx) {
          ((DirectionalLightEx) light).updateColor();
        }
      }
    }

    super.bindLights(renderable, attributes);

    // XXX
    ColorAttribute ambiantLight = attributes.get(ColorAttribute.class, ColorAttribute.AmbientLight);
    if (ambiantLight != null) {
      program.setUniformf(u_ambientLight, ambiantLight.color.r, ambiantLight.color.g, ambiantLight.color.b);
    }
  }
}
