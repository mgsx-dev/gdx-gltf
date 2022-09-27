package net.mgsx.gltf.data.extensions;

import net.mgsx.gltf.data.texture.GLTFTextureInfo;

/**
 * {@link net.mgsx.gltf.data.material.GLTFMaterial} extension.
 * See https://github.com/KhronosGroup/glTF/blob/main/extensions/2.0/Khronos/KHR_materials_iridescence/README.md
 */
public class KHRMaterialsIridescence {
	
	public static final String EXT = "KHR_materials_iridescence";
	
	public float iridescenceFactor = 0f;
	public GLTFTextureInfo iridescenceTexture;
	public float iridescenceIor = 1.3f;
	public float iridescenceThicknessMinimum = 100;
	public float iridescenceThicknessMaximum = 400;
	public GLTFTextureInfo iridescenceThicknessTexture;
}
