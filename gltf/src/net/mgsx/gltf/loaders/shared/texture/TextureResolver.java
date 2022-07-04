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
import net.mgsx.gltf.loaders.exceptions.GLTFRuntimeException;
import net.mgsx.gltf.loaders.shared.GLTFTypes;

public class TextureResolver implements Disposable
{
	protected final ObjectMap<Integer, Texture> texturesSimple = new ObjectMap<Integer, Texture>();
	protected final ObjectMap<Integer, Texture> texturesMipmap = new ObjectMap<Integer, Texture>();
	protected Array<GLTFTexture> glTextures;
	protected Array<GLTFSampler> glSamplers;
	
	public void loadTextures(Array<GLTFTexture> glTextures, Array<GLTFSampler> glSamplers, ImageResolver imageResolver) {
		this.glTextures = glTextures;
		this.glSamplers = glSamplers;
		if(glTextures != null){
			for(int i=0 ; i<glTextures.size ; i++){
				GLTFTexture glTexture = glTextures.get(i);
				
				// check if mipmap needed for this texture configuration
				boolean useMipMaps = false;
				if(glTexture.sampler != null){
					GLTFSampler sampler = glSamplers.get(glTexture.sampler);
					if(GLTFTypes.isMipMapFilter(sampler)){
						useMipMaps = true;
					}
				}
				
				ObjectMap<Integer, Texture> textureMap = useMipMaps ? texturesMipmap : texturesSimple;
				
				if(!textureMap.containsKey(glTexture.source)){
					Pixmap pixmap = imageResolver.get(glTexture.source);
					Texture texture = new Texture(pixmap, useMipMaps);
					textureMap.put(glTexture.source, texture);
				}
			}
		}
	}
	
	public TextureDescriptor<Texture> getTexture(GLTFTextureInfo glMap) {
		GLTFTexture glTexture = glTextures.get(glMap.index);
		
		TextureDescriptor<Texture> textureDescriptor = new TextureDescriptor<Texture>();

		boolean useMipMaps;
		if(glTexture.sampler != null){
			GLTFSampler glSampler = glSamplers.get(glTexture.sampler);
			GLTFTypes.mapTextureSampler(textureDescriptor, glSampler);
			useMipMaps = GLTFTypes.isMipMapFilter(glSampler);
		}else{
			// default sampler options.
			// https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#texture
			textureDescriptor.minFilter = TextureFilter.Linear;
			textureDescriptor.magFilter = TextureFilter.Linear;
			textureDescriptor.uWrap = TextureWrap.Repeat;
			textureDescriptor.vWrap = TextureWrap.Repeat;
			useMipMaps = false;
		}
		
		ObjectMap<Integer, Texture> textureMap = useMipMaps ? texturesMipmap : texturesSimple;
		
		Texture texture = textureMap.get(glTexture.source);
		if(texture == null){
			throw new GLTFRuntimeException("texture not loaded");
		}
		textureDescriptor.texture = texture;
		return textureDescriptor;
	}

	@Override
	public void dispose() {
		for(Entry<Integer, Texture> e : texturesSimple){
			e.value.dispose();
		}
		texturesSimple.clear();
		for(Entry<Integer, Texture> e : texturesMipmap){
			e.value.dispose();
		}
		texturesMipmap.clear();
	}

	public Array<Texture> getTextures(Array<Texture> textures) {
		for(Entry<Integer, Texture> e : texturesSimple){
			textures.add(e.value);
		}
		for(Entry<Integer, Texture> e : texturesMipmap){
			textures.add(e.value);
		}
		return textures;
	}
}
