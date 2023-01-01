package net.mgsx.gltf.data.data;

import net.mgsx.gltf.data.GLTFEntity;

public class GLTFBufferView extends GLTFEntity {

  public int byteOffset = 0;
  public int byteLength;

  public Integer buffer;
  public Integer byteStride;
  public Integer target;
}
