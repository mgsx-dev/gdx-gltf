package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;

public class PBRFloatAttribute extends FloatAttribute {

  public static final String MetallicAlias = "Metallic";
  public static final long Metallic = register(MetallicAlias);

  public static final String RoughnessAlias = "Roughness";
  public static final long Roughness = register(RoughnessAlias);

  public static final String NormalScaleAlias = "NormalScale";
  public static final long NormalScale = register(NormalScaleAlias);

  public static final String OcclusionStrengthAlias = "OcclusionStrength";
  public static final long OcclusionStrength = register(OcclusionStrengthAlias);

  public static final String ShadowBiasAlias = "ShadowBias";
  public static final long ShadowBias = register(ShadowBiasAlias);

  public static final String EmissiveIntensityAlias = "EmissiveIntensity";
  public static final long EmissiveIntensity = register(EmissiveIntensityAlias);

  public static final String TransmissionFactorAlias = "TransmissionFactor";
  public static final long TransmissionFactor = register(TransmissionFactorAlias);

  public static final String IORAlias = "IOR";
  public static final long IOR = register(IORAlias);

  public static final String SpecularFactorAlias = "SpecularFactor";
  public static final long SpecularFactor = register(SpecularFactorAlias);

  public PBRFloatAttribute(long type, float value) {
    super(type, value);
  }

  @Override
  public Attribute copy() {
    return new PBRFloatAttribute(type, value);
  }

  public static Attribute createMetallic(float value) {
    return new PBRFloatAttribute(Metallic, value);
  }

  public static Attribute createRoughness(float value) {
    return new PBRFloatAttribute(Roughness, value);
  }

  public static Attribute createNormalScale(float value) {
    return new PBRFloatAttribute(NormalScale, value);
  }

  public static Attribute createOcclusionStrength(float value) {
    return new PBRFloatAttribute(OcclusionStrength, value);
  }

  public static Attribute createEmissiveIntensity(float value) {
    return new PBRFloatAttribute(EmissiveIntensity, value);
  }

  public static Attribute createTransmissionFactor(float value) {
    return new PBRFloatAttribute(TransmissionFactor, value);
  }

  public static Attribute createIOR(float value) {
    return new PBRFloatAttribute(IOR, value);
  }

  public static Attribute createSpecularFactor(float value) {
    return new PBRFloatAttribute(SpecularFactor, value);
  }
}
