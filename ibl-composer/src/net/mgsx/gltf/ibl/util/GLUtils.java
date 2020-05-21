package net.mgsx.gltf.ibl.util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL43;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.BufferUtils;

public class GLUtils {
	private static final IntBuffer intBuffer = BufferUtils.newIntBuffer(16);
	
	private static GLProfiler profiler;
	
	public static GLProfiler getProfiler(){
		if(profiler == null) profiler = new GLProfiler(Gdx.graphics);
		return profiler;
	}
	
	public static void onGlError(Consumer<Integer> handler){
		getProfiler().enable();
		getProfiler().setListener(new GLErrorListener() {
			@Override
			public void onError(int error) {
				handler.accept(error);
			}
		});
	}
	
	public static int getMaxCubemapSize() {
		intBuffer.clear();
		Gdx.gl.glGetIntegerv(GL20.GL_MAX_CUBE_MAP_TEXTURE_SIZE, intBuffer);
		int value = intBuffer.get();
		return value;
	}
	
	public static int getMaxFrameBufferSize() {
		intBuffer.clear();
		Gdx.gl.glGetIntegerv(GL43.GL_MAX_FRAMEBUFFER_WIDTH, intBuffer);
		int maxWidth = intBuffer.get();
		intBuffer.clear();
		Gdx.gl.glGetIntegerv(GL43.GL_MAX_FRAMEBUFFER_HEIGHT, intBuffer);
		int maxHeight = intBuffer.get();
		return Math.min(maxWidth, maxHeight);
	}
	
	public static int getMaxCubemapSizeRGB888(){
		return getMaxCubemapSize(GL30.GL_RGB8, GL20.GL_RGB, GL30.GL_UNSIGNED_BYTE);
	}
	
	public static int getMaxCubemapSize(int internalFormat, int format, int type){
		for(int size=getMaxCubemapSize() ; size>0 ; size=size>>1){
			if(isCubemapSupported(size, internalFormat, format, type)) return size;
		}
		return 0;
	}
	
	private static boolean isCubemapSupported(int size, int internalFormat, int format, int type){
		intBuffer.clear();
		GL11.glTexImage2D(GL13.GL_PROXY_TEXTURE_CUBE_MAP, 0, internalFormat, size, size, 0, format, type, (ByteBuffer)null);
		GL11.glGetTexLevelParameter(GL13.GL_PROXY_TEXTURE_CUBE_MAP, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT, intBuffer);
		int result = intBuffer.get();
		return result == internalFormat;
	}

	public static int sizeToPOT(int size) {
		return MathUtils.round((float)(Math.log(size) / Math.log(2.0)));
	}

}
