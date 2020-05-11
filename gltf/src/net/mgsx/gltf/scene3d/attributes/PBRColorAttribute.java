package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

public class PBRColorAttribute extends ColorAttribute
{
	public final static String BaseColorFactorAlias = "BaseColorFactor";
	public final static long BaseColorFactor = register(BaseColorFactorAlias);

	static{
		Mask |= BaseColorFactor;
	}
	
	public PBRColorAttribute(long type, Color color) {
		super(type, color);
	}
	
	@Override
	public Attribute copy() {
		return new PBRColorAttribute(type, color);
	}
	
}
