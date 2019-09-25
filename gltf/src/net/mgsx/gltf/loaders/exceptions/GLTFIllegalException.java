package net.mgsx.gltf.loaders.exceptions;

/**
 * root exception for features not allowed by GLTF 2.0 specification.
 */
public class GLTFIllegalException extends GLTFRuntimeException {

	private static final long serialVersionUID = 5253133784286484602L;

	public GLTFIllegalException(String message) {
		super(message);
	}
	
}
