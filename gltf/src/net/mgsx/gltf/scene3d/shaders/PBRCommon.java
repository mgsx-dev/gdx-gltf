
package net.mgsx.gltf.scene3d.shaders;

import java.nio.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Application.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.utils.*;

public class PBRCommon {
	public static final int MAX_MORPH_TARGETS = 8;

	/** ClassX: thread-safety support
	 * 
	 * @author dar */
	static class IntBufferLocal extends ThreadLocal<IntBuffer> {
		/*
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		@Override
		protected IntBuffer initialValue() {
			return BufferUtils.newIntBuffer(16);
		}
	}

	// ClassX: thread-safety support
	private static final IntBufferLocal localBuffer = new IntBufferLocal();

	public static int getCapability(int pname) {
		final IntBuffer intBuffer = localBuffer.get();
		intBuffer.clear();
		Gdx.gl.glGetIntegerv(pname, intBuffer);
		return intBuffer.get();
	}

	public static void checkVertexAttributes(Renderable renderable) {
		final int numVertexAttributes = renderable.meshPart.mesh.getVertexAttributes().size();
		final int maxVertexAttribs = getCapability(GL20.GL_MAX_VERTEX_ATTRIBS);
		if (numVertexAttributes > maxVertexAttribs) {
			throw new GdxRuntimeException("too many vertex attributes : " + numVertexAttributes + " > " + maxVertexAttribs);
		}
	}

	private static Boolean seamlessCubemapsShouldBeEnabled = null;

	public static void enableSeamlessCubemaps() {
		if (seamlessCubemapsShouldBeEnabled == null) {
			boolean seamlessCubemapsSupported;
			if (Gdx.app.getType() == ApplicationType.Desktop) {
				// Cubemaps seamless are partially supported for desktop and if so, it's required to enable it.
				seamlessCubemapsSupported = Gdx.graphics.getGLVersion().isVersionEqualToOrHigher(3, 2) || Gdx.graphics.supportsExtension("GL_ARB_seamless_cube_map");
				seamlessCubemapsShouldBeEnabled = seamlessCubemapsSupported;
			} else {
				// Cubemaps seamless supported and always enabled for GLES 3 and WebGL 2.
				// this feature is not supported at all for older versions : GLES 2 and WebGL 1.
				seamlessCubemapsSupported = Gdx.gl30 != null;
				seamlessCubemapsShouldBeEnabled = false;
			}
			if (!seamlessCubemapsSupported) {
				Gdx.app.error("PBR", "Warning seamless CubeMap is not supported by this platform and may cause filtering artifacts");
			}
		}
		if (seamlessCubemapsShouldBeEnabled) {
			final int GL_TEXTURE_CUBE_MAP_SEAMLESS = 0x884F; // from GL32
			Gdx.gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
		}
	}
}
