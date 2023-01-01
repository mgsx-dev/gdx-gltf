package net.mgsx.gltf.exporters;

public class GLTFExporterConfig {

  /**
   * max binary file size (default 10 MB)
   */
  public int maxBinaryFileSize = 10 * 1024 * 1024;

  public boolean exportCameras = true;
  public boolean exportLights = true;
}
