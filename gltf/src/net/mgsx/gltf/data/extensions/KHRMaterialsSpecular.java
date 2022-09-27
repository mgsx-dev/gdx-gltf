package net.mgsx.gltf.data.extensions;

import com.badlogic.gdx.graphics.Color;

import net.mgsx.gltf.data.texture.GLTFTextureInfo;

/**
 * {@link net.mgsx.gltf.data.material.GLTFMaterial} extension.
 * See https://github.com/KhronosGroup/glTF/blob/main/extensions/2.0/Khronos/KHR_materials_specular/README.md
 */
public class KHRMaterialsSpecular {
	
	public static final String EXT = "KHR_materials_specular";
	
	public float specularFactor = 1f;
	public GLTFTextureInfo specularTexture = null;
	public Color specularColorFactor = new Color(Color.WHITE);
	public GLTFTextureInfo specularColorTexture = null;
}
