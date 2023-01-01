package net.mgsx.gltf.loaders.exceptions;

/**
 * root GLTF loading error
 */
public class GLTFRuntimeException extends RuntimeException {

  private static final long serialVersionUID = -8571720960735308661L;

  public GLTFRuntimeException(String message) {
    super(message);
  }

  public GLTFRuntimeException(Throwable t) {
    super(t);
  }

  public GLTFRuntimeException(String message, Throwable t) {
    super(message, t);
  }
}
