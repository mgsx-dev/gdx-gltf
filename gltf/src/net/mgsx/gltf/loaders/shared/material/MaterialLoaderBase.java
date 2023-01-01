package net.mgsx.gltf.loaders.shared.material;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.utils.Array;
import net.mgsx.gltf.data.material.GLTFMaterial;
import net.mgsx.gltf.loaders.shared.texture.TextureResolver;

public abstract class MaterialLoaderBase implements MaterialLoader {

  protected TextureResolver textureResolver;
  private final Array<Material> materials = new Array<>();
  private final Material defaultMaterial;

  public MaterialLoaderBase(TextureResolver textureResolver, Material defaultMaterial) {
    super();
    this.textureResolver = textureResolver;
    this.defaultMaterial = defaultMaterial;
  }

  @Override
  public Material getDefaultMaterial() {
    return defaultMaterial;
  }

  @Override
  public Material get(int index) {
    return materials.get(index);
  }

  @Override
  public void loadMaterials(Array<GLTFMaterial> glMaterials) {
    if (glMaterials != null) {
      for (int i = 0; i < glMaterials.size; i++) {
        GLTFMaterial glMaterial = glMaterials.get(i);
        Material material = loadMaterial(glMaterial);
        materials.add(material);
      }
    }
  }

  protected abstract Material loadMaterial(GLTFMaterial glMaterial);
}
