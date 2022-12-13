package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class MirrorSourceAttribute extends Attribute
{
	public static final String TypeAlias = "mirrorSource";
	public static final long Type = register(TypeAlias);
	
	public final TextureDescriptor<Texture> textureDescription = new TextureDescriptor<Texture>();
	public final Vector3 normal = new Vector3();
	
	public MirrorSourceAttribute() {
		super(Type);
	}

	@Override
	public int compareTo(Attribute o) {
		if (type != o.type) return type < o.type ? -1 : 1;
		MirrorSourceAttribute other = (MirrorSourceAttribute)o;
		final int c = textureDescription.compareTo(other.textureDescription);
		if (c != 0) return c;
		Vector3 otherNormal = other.normal;
		if(!MathUtils.isEqual(normal.x, otherNormal.x)) return normal.x < otherNormal.x ? -1 : 1;
		if(!MathUtils.isEqual(normal.y, otherNormal.y)) return normal.y < otherNormal.y ? -1 : 1;
		if(!MathUtils.isEqual(normal.z, otherNormal.z)) return normal.z < otherNormal.z ? -1 : 1;
		return 0;
	}

	@Override
	public Attribute copy() {
		return set(textureDescription, normal);
	}

	public MirrorSourceAttribute set(TextureDescriptor<Texture> textureDescription, Vector3 normal) {
		this.textureDescription.set(textureDescription);
		this.normal.set(normal);
		return this;
	}

}
