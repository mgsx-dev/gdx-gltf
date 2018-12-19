package net.mgsx.gltf.data.extensions;

import net.mgsx.gltf.data.texture.GLTFTextureInfo;

/**
 * {@link net.mgsx.gltf.model.material.GLTFMaterial} extension
 * @see https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_materials_pbrSpecularGlossiness/README.md 
 */
public class KHRMaterialsPBRSpecularGlossiness 
{
	public static final String EXT = "KHR_materials_pbrSpecularGlossiness";
	
	public float [] diffuseFactor;
	public float [] specularFactor;
	public float glossinessFactor = 1f;
	
	public GLTFTextureInfo diffuseTexture, specularGlossinessTexture;
}
