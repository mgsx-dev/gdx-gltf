package net.mgsx.gltf.data.material;

import net.mgsx.gltf.data.GLTFObject;
import net.mgsx.gltf.data.texture.GLTFTextureInfo;

public class GLTFpbrMetallicRoughness extends GLTFObject {
	public float[] baseColorFactor;
	public float metallicFactor = 1;
	public float roughnessFactor = 1;
	public GLTFTextureInfo baseColorTexture, metallicRoughnessTexture;
}
