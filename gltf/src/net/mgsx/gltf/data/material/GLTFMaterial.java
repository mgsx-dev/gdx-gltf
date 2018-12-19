package net.mgsx.gltf.data.material;

import net.mgsx.gltf.data.GLTFEntity;
import net.mgsx.gltf.data.texture.GLTFNormalTextureInfo;
import net.mgsx.gltf.data.texture.GLTFOcclusionTextureInfo;
import net.mgsx.gltf.data.texture.GLTFTextureInfo;

public class GLTFMaterial extends GLTFEntity{
	
	public float [] emissiveFactor;

	public GLTFNormalTextureInfo normalTexture;
	public GLTFOcclusionTextureInfo occlusionTexture;
	public GLTFTextureInfo emissiveTexture;
	
	public String alphaMode;
	public Float alphaCutoff;
	
	public Boolean doubleSided;
	
	public GLTFpbrMetallicRoughness pbrMetallicRoughness;
	
}
