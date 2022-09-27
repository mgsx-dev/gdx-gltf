package net.mgsx.gltf.data.extensions;

import com.badlogic.gdx.graphics.Color;

import net.mgsx.gltf.data.texture.GLTFTextureInfo;

/**
 * {@link net.mgsx.gltf.data.material.GLTFMaterial} extension.
 * See https://github.com/KhronosGroup/glTF/blob/main/extensions/2.0/Khronos/KHR_materials_volume/README.md
 */
public class KHRMaterialsVolume {
	
	public static final String EXT = "KHR_materials_volume";
	
	public float thicknessFactor = 0f;
	public GLTFTextureInfo thicknessTexture = null;
	public Float attenuationDistance = null; // default +inf.
	public Color attenuationColor = new Color(Color.WHITE);
}
