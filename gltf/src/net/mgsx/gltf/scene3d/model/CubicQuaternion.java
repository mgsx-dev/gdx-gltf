package net.mgsx.gltf.scene3d.model;

import com.badlogic.gdx.math.Quaternion;

public class CubicQuaternion extends Quaternion
{
	public final Quaternion tangentIn = new Quaternion();
	public final Quaternion tangentOut = new Quaternion();

}
