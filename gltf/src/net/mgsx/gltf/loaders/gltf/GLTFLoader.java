package net.mgsx.gltf.loaders.gltf;

import com.badlogic.gdx.files.FileHandle;

import net.mgsx.gltf.loaders.shared.GLTFLoaderBase;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GLTFLoader extends GLTFLoaderBase 
{
	public SceneAsset load(FileHandle glFile){
		SeparatedDataFileResolver dataFileResolver = new SeparatedDataFileResolver();
		dataFileResolver.load(glFile);
		return loadInternal(dataFileResolver);
	}

}
