package net.mgsx.gltf.ibl.util;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;

public class GLCapabilities {
	public static GLCapabilities i;
	
	public int maxSizeCubemap8bits;
	public int maxSizeCubemap32bits;
	public int maxSizeFramebuffer;
	
	public GLCapabilities() {
		maxSizeCubemap8bits = GLUtils.getMaxCubemapSize(GL30.GL_RGB8, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE);
		maxSizeCubemap32bits = GLUtils.getMaxCubemapSize(GL30.GL_RGB32F, GL20.GL_RGB, GL20.GL_FLOAT);
		maxSizeFramebuffer = GLUtils.getMaxFrameBufferSize();
	}
	
}
