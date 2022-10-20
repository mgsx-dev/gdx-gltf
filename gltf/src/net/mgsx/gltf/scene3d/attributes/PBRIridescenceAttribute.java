package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.MathUtils;

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
		if(type != o.type) return type < o.type ? -1 : 1;
		PBRIridescenceAttribute other = (PBRIridescenceAttribute)o;
		if(!MathUtils.isEqual(factor, other.factor)) return factor < other.factor ? -1 : 1;
		if(!MathUtils.isEqual(ior, other.ior)) return ior < other.ior ? -1 : 1;
		if(!MathUtils.isEqual(thicknessMin, other.thicknessMin)) return thicknessMin < other.thicknessMin ? -1 : 1;
		if(!MathUtils.isEqual(thicknessMax, other.thicknessMax)) return thicknessMax < other.thicknessMax ? -1 : 1;
		return 0;
	}

	@Override
	public Attribute copy() {
		return new PBRIridescenceAttribute(factor, ior, thicknessMin, thicknessMax);
	}
}
