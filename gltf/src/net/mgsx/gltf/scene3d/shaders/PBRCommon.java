package net.mgsx.gltf.scene3d.shaders;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class PBRCommon {
	public static final int MAX_MORPH_TARGETS = 8;
	
	private static final IntBuffer intBuffer = BufferUtils.newIntBuffer(16);
	
	public static int getCapability(int pname){
		intBuffer.clear();
		Gdx.gl.glGetIntegerv(pname, intBuffer);
		return intBuffer.get();
	}
	
	public static void checkVertexAttributes(Renderable renderable){
		final int numVertexAttributes = renderable.meshPart.mesh.getVertexAttributes().size();
		final int maxVertexAttribs = getCapability(GL20.GL_MAX_VERTEX_ATTRIBS);
		if(numVertexAttributes > maxVertexAttribs){
			throw new GdxRuntimeException("too many vertex attributes : " + numVertexAttributes + " > " + maxVertexAttribs);
		}
	}
}
