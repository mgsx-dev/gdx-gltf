package net.mgsx.gltf.loaders.gltf;

import com.badlogic.gdx.files.FileHandle;

import net.mgsx.gltf.loaders.shared.GLTFLoaderBase;
import net.mgsx.gltf.loaders.shared.data.DataResolver;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GLTFLoader extends GLTFLoaderBase 
{
	public SceneAsset load(FileHandle glFile){
		this.dataFileResolver = new SeparatedDataFileResolver();
		this.dataFileResolver.load(glFile);
		this.glModel = dataFileResolver.getRoot();
		dataResolver = new DataResolver(glModel, dataFileResolver);
		imageResolver = new SeparatedImageResolver(glFile.parent());
		return loadInternal();
	}

}
