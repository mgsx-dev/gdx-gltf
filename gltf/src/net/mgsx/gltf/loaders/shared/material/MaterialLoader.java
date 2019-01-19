package net.mgsx.gltf.loaders.shared.material;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.data.material.GLTFMaterial;

public interface MaterialLoader {

	public Material getDefaultMaterial();

	public Material get(int index);

	public void loadMaterials(Array<GLTFMaterial> materials);

}
