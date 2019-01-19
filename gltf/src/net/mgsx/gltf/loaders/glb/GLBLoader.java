package net.mgsx.gltf.loaders.glb;

import com.badlogic.gdx.files.FileHandle;

import net.mgsx.gltf.loaders.shared.GLTFLoaderBase;
import net.mgsx.gltf.loaders.shared.data.DataFileResolver;
import net.mgsx.gltf.loaders.shared.data.DataResolver;
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

	private SceneAsset load(DataFileResolver dataFileResolver){
		this.dataFileResolver = dataFileResolver;
		glModel = dataFileResolver.getRoot();
		
		dataResolver = new DataResolver(glModel, dataFileResolver);
		imageResolver = new BinaryImageResolver(dataResolver);
		return loadInternal();
	}
	
}
