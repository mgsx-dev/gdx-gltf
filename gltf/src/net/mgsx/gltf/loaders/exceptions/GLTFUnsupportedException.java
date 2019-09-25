package net.mgsx.gltf.loaders.exceptions;

/**
 * root exception for features allowed by GLTF 2.0 specification but not supported by this implementation. 
 */
public class GLTFUnsupportedException extends GLTFRuntimeException
{
	private static final long serialVersionUID = 2530359716452090852L;

	public GLTFUnsupportedException(String message) {
		super(message);
	}
	
}
