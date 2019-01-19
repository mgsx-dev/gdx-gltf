package net.mgsx.gltf.loaders.glb;

import com.badlogic.gdx.files.FileHandle;

import net.mgsx.gltf.loaders.shared.GLTFLoaderBase;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GLBLoader extends GLTFLoaderBase {

	public SceneAsset load(FileHandle file){
		BinaryDataFileResolver dataFileResolver = new BinaryDataFileResolver();
		dataFileResolver.load(file);
		return load(dataFileResolver);
	}
	
	public SceneAsset load(byte[] bytes) {
		BinaryDataFileResolver dataFileResolver = new BinaryDataFileResolver();
		dataFileResolver.load(bytes);
		return load(dataFileResolver);
	}
}
