package net.mgsx.gltf.data.camera;

import net.mgsx.gltf.data.GLTFEntity;

public class GLTFCamera extends GLTFEntity {
	public String type;
	public GLTFPerspective perspective;
	public GLTFOrthographic orthographic;
}
