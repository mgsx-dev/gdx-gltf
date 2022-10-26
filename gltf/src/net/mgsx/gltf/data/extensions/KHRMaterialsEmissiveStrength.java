package net.mgsx.gltf.data.extensions;

/**
 * {@link net.mgsx.gltf.data.material.GLTFMaterial} extension.
 * See https://github.com/KhronosGroup/glTF/blob/main/extensions/2.0/Khronos/KHR_materials_emissive_strength/README.md
 */
public class KHRMaterialsEmissiveStrength {
	
	public static final String EXT = "KHR_materials_emissive_strength";
	
	public float emissiveStrength = 1f;
}
