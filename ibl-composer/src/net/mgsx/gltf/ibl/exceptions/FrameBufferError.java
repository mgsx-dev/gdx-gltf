package net.mgsx.gltf.ibl.exceptions;

import com.badlogic.gdx.utils.GdxRuntimeException;

@SuppressWarnings("serial")
public class FrameBufferError extends GdxRuntimeException {

	public FrameBufferError(String message, Throwable t) {
		super(message, t);
	}

	public FrameBufferError(String message) {
		super(message);
	}

	public FrameBufferError(Throwable t) {
		super(t);
	}

}
