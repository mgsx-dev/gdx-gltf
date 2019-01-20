package net.mgsx.gltf.loaders.gltf;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.data.texture.GLTFSampler;
import net.mgsx.gltf.data.texture.GLTFTexture;
import net.mgsx.gltf.loaders.shared.GLTFLoaderBase;
import net.mgsx.gltf.loaders.shared.GLTFTypes;
import net.mgsx.gltf.loaders.shared.texture.ImageResolver;
import net.mgsx.gltf.loaders.shared.texture.TextureResolver;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GLTFAssetLoader  extends AsynchronousAssetLoader<SceneAsset, AssetLoaderParameters<SceneAsset>>{

	private class ManagedTextureResolver extends TextureResolver {
		
		private Array<AssetDescriptor<Texture>> textureDescriptors = new Array<AssetDescriptor<Texture>>();
		
		private GLTF glModel;
		
		public ManagedTextureResolver(GLTF glModel) {
			super();
			this.glModel = glModel;
		}

		@Override
		public void loadTextures(Array<GLTFTexture> glTextures, Array<GLTFSampler> glSamplers, ImageResolver imageResolver) {
		}

		public void fetch(AssetManager manager) {
			for(int i=0 ; i<textureDescriptors.size ; i++){
				textures.put(i, manager.get(textureDescriptors.get(i)));
			}
		}

		public void getDependencies(Array<AssetDescriptor> deps) {
			this.glTextures = glModel.textures;
			this.glSamplers = glModel.samplers;
			if(glTextures != null){
				for(int i=0 ; i<glTextures.size ; i++){
					GLTFTexture glTexture = glTextures.get(i);
					
					GLTFImage glImage = glModel.images.get(glTexture.source);
					FileHandle imageFile = dataFileResolver.getImageFile(glImage);
					if(imageFile != null){
						TextureParameter textureParameter = new TextureParameter();
						if(glTexture.sampler != null){
							GLTFSampler sampler = glSamplers.get(glTexture.sampler);
							if(GLTFTypes.isMipMapFilter(sampler)){
								textureParameter.genMipMaps = true;
							}
							GLTFTypes.mapTextureSampler(textureParameter, sampler);
						}
						AssetDescriptor<Texture> assetDescriptor = new AssetDescriptor<Texture>(imageFile, Texture.class, textureParameter);
						deps.add(assetDescriptor);
						textureDescriptors.add(assetDescriptor);
					}
				}
			}
		}
	}
	
	private SeparatedDataFileResolver dataFileResolver;
	private ManagedTextureResolver textureResolver;

	public GLTFAssetLoader() {
		this(new InternalFileHandleResolver());
	}
	
	public GLTFAssetLoader(FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file,
			AssetLoaderParameters<SceneAsset> parameter) {
		
		textureResolver.fetch(manager);
	}

	@Override
	public SceneAsset loadSync(AssetManager manager, String fileName, FileHandle file,
			AssetLoaderParameters<SceneAsset> parameter) {
		
		GLTFLoaderBase loader = new GLTFLoaderBase(textureResolver);
		SceneAsset sceneAsset = loader.load(dataFileResolver);
		this.textureResolver = null;
		this.dataFileResolver = null;
		return sceneAsset;
	}

	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file,
			AssetLoaderParameters<SceneAsset> parameter) {
		
		Array<AssetDescriptor> deps = new Array<AssetDescriptor>();
		
		dataFileResolver = new SeparatedDataFileResolver();
		dataFileResolver.load(file);
		GLTF glModel = dataFileResolver.getRoot();
		
		textureResolver = new ManagedTextureResolver(glModel);
		textureResolver.getDependencies(deps);
		
		return deps;
	}

}
