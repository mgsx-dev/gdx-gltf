package net.mgsx.gltf.data.data;

import net.mgsx.gltf.data.GLTFEntity;

public class GLTFAccessor extends GLTFEntity {

  public Integer bufferView;

  public boolean normalized = false;

  public int byteOffset = 0;
  public int componentType;
  public int count;

  public String type;

  public float[] min;

  public float[] max;

  public GLTFAccessorSparse sparse;
}
