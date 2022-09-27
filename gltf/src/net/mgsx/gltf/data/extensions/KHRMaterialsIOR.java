package net.mgsx.gltf.data.extensions;

/**
 * {@link net.mgsx.gltf.data.material.GLTFMaterial} extension.
 * See https://github.com/KhronosGroup/glTF/blob/main/extensions/2.0/Khronos/KHR_materials_ior/README.md
 */
public class KHRMaterialsIOR {
	
	public static final String EXT = "KHR_materials_ior";
	
	public float ior = 1.5f;
}
