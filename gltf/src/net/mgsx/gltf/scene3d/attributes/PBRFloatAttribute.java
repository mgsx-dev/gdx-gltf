package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;

public class PBRFloatAttribute extends FloatAttribute
{
	public final static String MetallicAlias = "Metallic";
	public final static long Metallic = register(MetallicAlias);
	
	public final static String RoughnessAlias = "Roughness";
	public final static long Roughness = register(RoughnessAlias);
	
	public final static String NormalScaleAlias = "NormalScale";
	public final static long NormalScale = register(NormalScaleAlias);
	
	public final static String OcclusionStrengthAlias = "OcclusionStrength";
	public final static long OcclusionStrength = register(OcclusionStrengthAlias);
	
	public final static String ShadowBiasAlias = "ShadowBias";
	public final static long ShadowBias = register(NormalScaleAlias);
	
	public PBRFloatAttribute(long type, float value) {
		super(type, value);
	}

	
	@Override
	public Attribute copy () {
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
}
