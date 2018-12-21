package net.mgsx.gltf.loaders;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 * Hack {@link Pixmap} loading from binary data via reflection in order to avoid GWT compilation issues. 
 */
public class PixmapBinaryLoaderHack {

	public static Pixmap load(byte [] encodedData, int offset, int len){
		if(Gdx.app.getType() == ApplicationType.WebGL){
			throw new GdxRuntimeException("load pixmap from bytes not supported for WebGL");
		}else{
			// call new Pixmap(encodedData, offset, len); via reflection to
			// avoid compilation error with GWT.
			try {
				return (Pixmap)ClassReflection.getConstructor(Pixmap.class, byte[].class, int.class, int.class).newInstance(encodedData, offset, len);
			} catch (ReflectionException e) {
				throw new GdxRuntimeException(e);
			}
		}
	}
}
