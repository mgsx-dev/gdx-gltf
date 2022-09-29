
package net.mgsx.gltf.ibl.util;

import java.nio.*;
import java.util.function.*;

import org.lwjgl.opengl.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.graphics.profiling.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;

public class GLUtils {

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

	private static GLProfiler profiler;

	public static GLProfiler getProfiler() {
		if (profiler == null) profiler = new GLProfiler(Gdx.graphics);
		return profiler;
	}

	public static void onGlError(Consumer<Integer> handler) {
		getProfiler().enable();
		getProfiler().setListener(new GLErrorListener() {
			@Override
			public void onError(int error) {
				handler.accept(error);
			}
		});
	}

	public static final String GL_NVX_gpu_memory_info_ext = "GL_NVX_gpu_memory_info";
	public static final int GL_GPU_MEM_INFO_TOTAL_AVAILABLE_MEM_NVX = 0x9048;
	public static final int GL_GPU_MEM_INFO_CURRENT_AVAILABLE_MEM_NVX = 0x9049;

	public static int getMaxMemoryKB() {
		final IntBuffer intBuffer = localBuffer.get();
		intBuffer.clear();
		Gdx.gl.glGetIntegerv(GL_GPU_MEM_INFO_TOTAL_AVAILABLE_MEM_NVX, intBuffer);
		return intBuffer.get();
	}

	public static int getAvailableMemoryKB() {
		final IntBuffer intBuffer = localBuffer.get();
		intBuffer.clear();
		Gdx.gl.glGetIntegerv(GL_GPU_MEM_INFO_CURRENT_AVAILABLE_MEM_NVX, intBuffer);
		return intBuffer.get();
	}

	public static boolean hasMemoryInfo() {
		return Gdx.graphics.supportsExtension(GL_NVX_gpu_memory_info_ext);
	}

	public static int getMaxCubemapSize() {
		final IntBuffer intBuffer = localBuffer.get();
		intBuffer.clear();
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_CUBE_MAP_TEXTURE_SIZE, intBuffer);
		int value = intBuffer.get();
		return value;
	}

	public static int getMaxFrameBufferSize() {
		final IntBuffer intBuffer = localBuffer.get();
		intBuffer.clear();
		Gdx.gl.glGetIntegerv(GL43.GL_MAX_FRAMEBUFFER_WIDTH, intBuffer);
		int maxWidth = intBuffer.get();
		intBuffer.clear();
		Gdx.gl.glGetIntegerv(GL43.GL_MAX_FRAMEBUFFER_HEIGHT, intBuffer);
		int maxHeight = intBuffer.get();
		return Math.min(maxWidth, maxHeight);
	}

	public static int getMaxFrameBufferCubeMapSizeRGB888() {
		for (int size = getMaxCubemapSize(); size > 0; size = size >> 1) {
			try {
				FrameBufferCubemap fbo = new FrameBufferCubemap(Format.RGB888, size, size, false);
				fbo.dispose();
			} catch (IllegalStateException e) {
				continue;
			}
			return size;
		}
		return 0;
	}

	public static int getMaxCubemapSizeRGB888() {
		return getMaxCubemapSize(GL30.GL_RGB8, GL20.GL_RGB, GL30.GL_UNSIGNED_BYTE);
	}

	public static int getMaxCubemapSize(int internalFormat, int format, int type) {
		for (int size = getMaxCubemapSize(); size > 0; size = size >> 1) {
			if (isCubemapSupported(size, internalFormat, format, type)) return size;
		}
		return 0;
	}

	private static boolean isCubemapSupported(int size, int internalFormat, int format, int type) {
		final IntBuffer intBuffer = localBuffer.get();
		GL11.glTexImage2D(GL13.GL_PROXY_TEXTURE_CUBE_MAP, 0, internalFormat, size, size, 0, format, type, (ByteBuffer)null);
		intBuffer.clear();
		GL11.glGetTexLevelParameter(GL13.GL_PROXY_TEXTURE_CUBE_MAP, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT, intBuffer);
		int internalFormatResult = intBuffer.get();
		intBuffer.clear();
		GL11.glGetTexLevelParameter(GL13.GL_PROXY_TEXTURE_CUBE_MAP, 0, GL11.GL_TEXTURE_WIDTH, intBuffer);
		int widthResult = intBuffer.get();
		intBuffer.clear();
		GL11.glGetTexLevelParameter(GL13.GL_PROXY_TEXTURE_CUBE_MAP, 0, GL11.GL_TEXTURE_HEIGHT, intBuffer);
		int heightResult = intBuffer.get();
		return internalFormatResult == internalFormat && widthResult == size && heightResult == size;
	}

	public static int sizeToPOT(int size) {
		return MathUtils.round((float)(Math.log(size) / Math.log(2.0)));
	}
}
