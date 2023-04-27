package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

public class ClippingPlaneAttribute extends Attribute
{
	public static final String TypeAlias = "clippingPlane";
	public static final long Type = register(TypeAlias);
	
	public final Plane plane;
	
	public ClippingPlaneAttribute(Plane plane) {
		super(Type);
		this.plane = plane;
	}

	public ClippingPlaneAttribute(Vector3 normal, float d) {
		super(Type);
		this.plane = new Plane(normal, d);
	}

	@Override
	public int compareTo(Attribute o) {
		if(type != o.type) return type < o.type ? -1 : 1;
		ClippingPlaneAttribute other = (ClippingPlaneAttribute)o;
		Vector3 normal = plane.normal;
		Vector3 otherNormal = other.plane.normal;
		if(!MathUtils.isEqual(normal.x, otherNormal.x)) return normal.x < otherNormal.x ? -1 : 1;
		if(!MathUtils.isEqual(normal.y, otherNormal.y)) return normal.y < otherNormal.y ? -1 : 1;
		if(!MathUtils.isEqual(normal.z, otherNormal.z)) return normal.z < otherNormal.z ? -1 : 1;
		if(!MathUtils.isEqual(plane.d, other.plane.d)) return plane.d < other.plane.d ? -1 : 1;
		return 0;
	}

	@Override
	public Attribute copy() {
		return new ClippingPlaneAttribute(plane.normal, plane.d);
	}

}
