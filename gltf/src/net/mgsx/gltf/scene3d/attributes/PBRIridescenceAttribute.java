package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;

public class PBRIridescenceAttribute extends Attribute
{
	public static final String Alias = "iridescence";
	public static final long Type = register(Alias);

	public float factor = 1, ior = 1.3f, thicknessMin = 100, thicknessMax = 400;
	
	public PBRIridescenceAttribute() {
		super(Type);
	}
	
	public PBRIridescenceAttribute(float factor, float ior, float thicknessMin, float thicknessMax) {
		super(Type);
		this.factor = factor;
		this.ior = ior;
		this.thicknessMin = thicknessMin;
		this.thicknessMax = thicknessMax;
	}

	@Override
	public int compareTo(Attribute o) {
		return (int)(type - o.type);
	}

	@Override
	public Attribute copy() {
		return new PBRIridescenceAttribute(factor, ior, thicknessMin, thicknessMax);
	}
}
