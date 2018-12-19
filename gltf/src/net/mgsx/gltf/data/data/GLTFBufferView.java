package net.mgsx.gltf.data.data;

import net.mgsx.gltf.data.GLTFEntity;

public class GLTFBufferView extends GLTFEntity {
	public int buffer, byteOffset = 0, byteLength;
	public Integer byteStride, target;
}
