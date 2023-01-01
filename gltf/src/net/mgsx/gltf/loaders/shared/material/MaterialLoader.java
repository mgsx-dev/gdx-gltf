package net.mgsx.gltf.loaders.shared.material;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.utils.Array;
import net.mgsx.gltf.data.material.GLTFMaterial;

public interface MaterialLoader {

  Material getDefaultMaterial();

  Material get(int index);

  void loadMaterials(Array<GLTFMaterial> materials);
}
