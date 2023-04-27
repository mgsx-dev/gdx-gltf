package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;

public class MirrorAttribute extends Attribute
{
	public static final String SpecularAlias = "specularMirror";
	public static final long Specular = register(SpecularAlias);
	
	public static MirrorAttribute createSpecular(){
		return new MirrorAttribute(Specular);
	}
	
	public MirrorAttribute(long type) {
		super(type);
	}

	@Override
	public int compareTo(Attribute o) {
		if (type != o.type) return type < o.type ? -1 : 1;
		return 0;
	}

	@Override
	public Attribute copy() {
		return new MirrorAttribute(type);
	}

}
