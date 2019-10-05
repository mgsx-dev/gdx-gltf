package net.mgsx.gltf.loaders.shared.texture;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gltf.data.texture.GLTFSampler;
import net.mgsx.gltf.data.texture.GLTFTexture;
import net.mgsx.gltf.data.texture.GLTFTextureInfo;
import net.mgsx.gltf.loaders.shared.GLTFTypes;

public class TextureResolver implements Disposable
{
	protected ObjectMap<Integer, Texture> textures = new ObjectMap<Integer, Texture>();
	protected Array<GLTFTexture> glTextures;
	protected Array<GLTFSampler> glSamplers;
	
	public void loadTextures(Array<GLTFTexture> glTextures, Array<GLTFSampler> glSamplers, ImageResolver imageResolver) {
		this.glTextures = glTextures;
		this.glSamplers = glSamplers;
		if(glTextures != null){
			for(int i=0 ; i<glTextures.size ; i++){
				GLTFTexture glTexture = glTextures.get(i);
				Pixmap pixmap = imageResolver.get(glTexture.source);
				boolean useMipMaps = false;
				if(glTexture.sampler != null){
					GLTFSampler sampler = glSamplers.get(glTexture.sampler);
					if(GLTFTypes.isMipMapFilter(sampler)){
						useMipMaps = true;
					}
				}
				if(pixmap != null){
					Texture texture = new Texture(pixmap, useMipMaps);
					textures.put(i, texture);
				}
			}
		}
	}
	
	public TextureDescriptor<Texture> getTexture(GLTFTextureInfo glMap) {
		GLTFTexture glTexture = glTextures.get(glMap.index);
		Texture texture = textures.get(glTexture.source);
		
		TextureDescriptor<Texture> textureDescriptor = new TextureDescriptor<Texture>();

		if(glTexture.sampler != null){
			GLTFSampler glSampler = glSamplers.get(glTexture.sampler);
			GLTFTypes.mapTextureSampler(textureDescriptor, glSampler);
		}else{
			// default sampler options.
			// https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#texture
			textureDescriptor.minFilter = TextureFilter.Linear;
			textureDescriptor.magFilter = TextureFilter.Linear;
			textureDescriptor.uWrap = TextureWrap.Repeat;
			textureDescriptor.vWrap = TextureWrap.Repeat;
		}
		textureDescriptor.texture = texture;
		return textureDescriptor;
	}

	@Override
	public void dispose() {
		for(Entry<Integer, Texture> entry : textures){
			entry.value.dispose();
		}
		textures.clear();
	}

	public Array<Texture> getTextures(Array<Texture> textures) {
		for(Entry<Integer, Texture> entry : this.textures){
			textures.add(entry.value);
		}
		return textures;
	}
}
