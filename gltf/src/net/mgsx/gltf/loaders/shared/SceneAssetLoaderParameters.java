package net.mgsx.gltf.loaders.shared;

import com.badlogic.gdx.assets.AssetLoaderParameters;

import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class SceneAssetLoaderParameters extends AssetLoaderParameters<SceneAsset> {

	/** load scene asset with underlying GLTF {@link SceneAsset#data} structure */
	public boolean withData = false;
}
