package net.mgsx.gltf.data.extensions;

import net.mgsx.gltf.data.texture.GLTFTextureInfo;

/**
 * {@link net.mgsx.gltf.data.material.GLTFMaterial} extension.
 * See https://github.com/KhronosGroup/glTF/blob/main/extensions/2.0/Khronos/KHR_materials_specular/README.md
 */
public class KHRMaterialsSpecular {

  public static final String EXT = "KHR_materials_specular";

  public float specularFactor = 1f;
  public GLTFTextureInfo specularTexture = null;
  public float[] specularColorFactor = {1, 1, 1};
  public GLTFTextureInfo specularColorTexture = null;
}
