package net.mgsx.gltf.loaders.glb;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import net.mgsx.gltf.loaders.shared.SceneAssetLoaderParameters;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GLBAssetLoader extends AsynchronousAssetLoader<SceneAsset, SceneAssetLoaderParameters> {

  public GLBAssetLoader() {
    this(new InternalFileHandleResolver());
  }

  public GLBAssetLoader(FileHandleResolver resolver) {
    super(resolver);
  }

  @Override
  public void loadAsync(AssetManager manager, String fileName, FileHandle file,
                        SceneAssetLoaderParameters parameter) {

  }

  @Override
  public SceneAsset loadSync(AssetManager manager, String fileName, FileHandle file,
                             SceneAssetLoaderParameters parameter) {
    final boolean withData = parameter != null && parameter.withData;
    return new GLBLoader().load(file, withData);
  }

  @Override
  public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file,
                                                SceneAssetLoaderParameters parameter) {
    return null;
  }
}
