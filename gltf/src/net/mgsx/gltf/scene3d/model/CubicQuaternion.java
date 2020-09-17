package net.mgsx.gltf.scene3d.model;

import com.badlogic.gdx.math.Quaternion;

@SuppressWarnings("serial")
public class CubicQuaternion extends Quaternion
{
	public final Quaternion tangentIn = new Quaternion();
	public final Quaternion tangentOut = new Quaternion();

}
