package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.Vector3;

public class FogAttribute  extends Attribute
{
	public static final String FogEquationAlias = "fogEquation";
	public static final long FogEquation = register(FogEquationAlias);
	
	public static FogAttribute createFog(float near, float far, float exponent){
		return new FogAttribute(FogEquation).set(near, far, exponent);
	}
	
	public final Vector3 value = new Vector3();

	public FogAttribute (long type) {
		super(type);
	}

	public Attribute set(Vector3 value) {
		this.value.set(value);
		return this;
	}
	
	public FogAttribute set(float near, float far, float exponent) {
		this.value.set(near, far, exponent);
		return this;
	}
	
	@Override
	public Attribute copy () {
		return new FogAttribute(type).set(value);
	}

	@Override
	public int compareTo (Attribute o) {
		return (int)(type - o.type);
	}

}
