package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;

public class PBRVolumeAttribute extends Attribute
{
	public static final String Alias = "volume";
	public static final long Type = register(Alias);

	public float thicknessFactor = 0f;
	/** a value of zero means positive infinity (no attenuation) */
	public float attenuationDistance = 0f;
	public final Color attenuationColor = new Color(Color.WHITE);
	
	public PBRVolumeAttribute() {
		super(Type);
	}
	
	public PBRVolumeAttribute(float thicknessFactor, float attenuationDistance, Color attenuationColor) {
		super(Type);
		this.thicknessFactor = thicknessFactor;
		this.attenuationDistance = attenuationDistance;
		this.attenuationColor.set(attenuationColor);
	}

	@Override
	public int compareTo(Attribute o) {
		return (int)(type - o.type);
	}

	@Override
	public Attribute copy() {
		return new PBRVolumeAttribute(thicknessFactor, attenuationDistance, attenuationColor);
	}
	
	
}
