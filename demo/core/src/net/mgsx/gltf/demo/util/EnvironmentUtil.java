package net.mgsx.gltf.demo.util;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

public class EnvironmentUtil {
	
	public static Cubemap createCubemap(FileHandleResolver resolver, String baseName, String extension) {
		Cubemap cubemap = new Cubemap(
				resolver.resolve(baseName + "right" + extension),  
				resolver.resolve(baseName + "left" + extension), 
				resolver.resolve(baseName + "top" + extension), 
				resolver.resolve(baseName + "bottom" + extension),
				resolver.resolve(baseName + "front" + extension),
				resolver.resolve(baseName + "back" + extension));
		cubemap.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		return cubemap;
	}
	
	public static Cubemap createCubemap(FileHandleResolver resolver, String baseName, String midName, String extension, int lods) {
		FacedMultiCubemapData data = new FacedMultiCubemapData(resolver, baseName, midName, extension, lods);
		Cubemap cubemap = new Cubemap(data);
		cubemap.setFilter(TextureFilter.MipMap, TextureFilter.MipMap);
		return cubemap;
	}
	
}
