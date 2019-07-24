package net.mgsx.gltf.demo.util;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

public class EnvironmentUtil 
{
	public static final String [] FACE_NAMES_FULL = {"right", "left", "top", "bottom", "front", "back"};
	public static final String [] FACE_NAMES_NP = {"px", "nx", "py", "ny", "pz", "nz"};
	public static final String [] FACE_NAMES_NEG_POS = {"posx", "negx", "posy", "negy", "posz", "negz"};

	public static Cubemap createCubemap(FileHandleResolver resolver, String baseName, String extension, String [] faceNames) {
		Cubemap cubemap = new Cubemap(
				resolver.resolve(baseName + faceNames[0] + extension),  
				resolver.resolve(baseName + faceNames[1] + extension), 
				resolver.resolve(baseName + faceNames[2] + extension), 
				resolver.resolve(baseName + faceNames[3] + extension),
				resolver.resolve(baseName + faceNames[4] + extension),
				resolver.resolve(baseName + faceNames[5] + extension));
		cubemap.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		return cubemap;
	}
	
	public static Cubemap createCubemap(FileHandleResolver resolver, String baseName, String midName, String extension, int lods, String [] faceNames) {
		FileHandle [] files = new FileHandle[6 * lods];
		for(int level = 0 ; level<lods ; level++){
			for(int face = 0 ; face < 6 ; face++){
				files[level*6+face] = resolver.resolve(baseName + faceNames[face] + midName + level + extension);
			}
		}
		FacedMultiCubemapData data = new FacedMultiCubemapData(files, lods);
		Cubemap cubemap = new Cubemap(data);
		cubemap.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
		return cubemap;
	}
	
}
