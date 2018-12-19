package net.mgsx.gltf.data.extensions;

/**
 * {@link net.mgsx.gltf.model.texture.GLTFTextureInfo} extension
 * @see https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_texture_transform/README.md
 */
public class KHRTextureTransform {
	public static final String EXT = "KHR_texture_transform";
	
	public float [] offset = {0f, 0f};
	public float rotation = 0f;
	public float [] scale = {1f, 1f};
	public Integer texCoord;
}
