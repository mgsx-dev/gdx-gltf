package net.mgsx.gltf.loaders.shared.material;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.data.material.GLTFMaterial;
import net.mgsx.gltf.loaders.shared.texture.TextureResolver;

public interface MaterialLoader {

	public Material getDefaultMaterial();

	public Material get(int index);

	public void loadMaterials(Array<GLTFMaterial> materials, TextureResolver textureResolver);

}
