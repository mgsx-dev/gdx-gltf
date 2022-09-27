package net.mgsx.gltf.data.extensions;

import net.mgsx.gltf.data.texture.GLTFTextureInfo;

/**
 * {@link net.mgsx.gltf.data.material.GLTFMaterial} extension.
 * See https://github.com/KhronosGroup/glTF/blob/main/extensions/2.0/Khronos/KHR_materials_transmission/README.md
 */
public class KHRMaterialsTransmission {
	
	public static final String EXT = "KHR_materials_transmission";
	
	public float transmissionFactor = 0;
	public GLTFTextureInfo transmissionTexture = null;
}
