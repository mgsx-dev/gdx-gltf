package net.mgsx.gltf.loaders.shared.texture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
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
	private static int pboHandle = 0;
	private static final ObjectMap<String, Texture> textureCache = new ObjectMap<>();
    private final boolean isUseGL30;

	public TextureResolver() {
        this.isUseGL30 = Gdx.gl30 != null;
		//init PBO Buffer
		if (pboHandle == 0 && isUseGL30) {
			if (Gdx.graphics.getFrameId() > 0){
				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run() {
						initPBO();
					}
				});
			} else {
				initPBO();
			}
		}
	}

	private void initPBO(){
		pboHandle = Gdx.gl.glGenBuffer();
		Gdx.gl.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, pboHandle);
		Gdx.gl.glBufferData(GL30.GL_PIXEL_UNPACK_BUFFER, 100000000, null, GL30.GL_STREAM_DRAW);
		Gdx.gl.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, 0);
	}

	private static class CreateTextureRunnable implements Runnable{
		private final Object waitObj;
		private final int[] textureHandle;
		private CreateTextureRunnable(int[] textureHandle, Object waitObj){
			this.waitObj = waitObj;
			this.textureHandle = textureHandle;
		}

		@Override
		public void run() {
			textureHandle[0] = Gdx.gl.glGenTexture();
			synchronized (waitObj){
				waitObj.notify();
			}
		}
	}

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

				if (!textureCache.containsKey(imageResolver.getUri(i))) {
					final int[] textureHandle = new int[1];
					//check if render begin
					if (Gdx.app.getGraphics().getFrameId() > 0 && isUseGL30) {
						//Create texture postRunnable
						Gdx.app.postRunnable(new CreateTextureRunnable(textureHandle, this));
						//Waiting for texture creation
						synchronized (this) {
							try {
								this.wait(3000);
							} catch (InterruptedException ex) {
								ex.printStackTrace();
							}
						}
						TexturePBO texture = new TexturePBO(textureHandle[0], pboHandle, imageResolver.get(glTexture.source), useMipMaps);
						textureMap.put(glTexture.source, texture);
					} else {
						TexturePBO texture = new TexturePBO(imageResolver.get(glTexture.source), useMipMaps);
						textureMap.put(glTexture.source, texture);
					}
					textureCache.put(imageResolver.getUri(i), textureMap.get(glTexture.source));
				} else {
					//if the texture is loaded, increase the usage counter
					textureMap.put(glTexture.source, textureCache.get(imageResolver.getUri(i)));
					Texture texture = textureCache.get(imageResolver.getUri(i));
					if (texture instanceof TexturePBO){
						((TexturePBO) texture).incrementUsesCount();
					}
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
		Gdx.app.postRunnable(new DisposeRunnable(texturesSimple, texturesMipmap));
	}

	private static class DisposeRunnable implements Runnable {
		private final ObjectMap<Integer, Texture> texturesSimple;
		private final ObjectMap<Integer, Texture> texturesMipmap;

		DisposeRunnable(ObjectMap<Integer, Texture> texturesSimple, ObjectMap<Integer, Texture> texturesMipmap) {
			this.texturesMipmap = texturesMipmap;
			this.texturesSimple = texturesSimple;
		}
		@Override
		public void run() {
			processDispose(texturesSimple);
			processDispose(texturesMipmap);
		}

		private void processDispose(ObjectMap<Integer, Texture> textures) {
			for (ObjectMap.Entry<Integer, Texture> e : textures) {
				Texture texture = e.value;
				if (texture instanceof TexturePBO) {
					((TexturePBO) texture).decrementUsesCount();
					//if the usage counter is less than "1", then we call the "dispose()" method, otherwise we decrement the counter
					if (((TexturePBO) texture).getUsesCount() < 1) {
						for (ObjectMap.Entry<String, Texture> e2 : textureCache.entries()) {
							if (e2.value == e.value) {
								textureCache.remove(e2.key);
								break;
							}
						}
						texture.dispose();
					}
				} else {
					texture.dispose();
				}
			}
			textures.clear();
		}
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
