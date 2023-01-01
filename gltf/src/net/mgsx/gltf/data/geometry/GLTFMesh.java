package net.mgsx.gltf.data.geometry;

import com.badlogic.gdx.utils.Array;
import net.mgsx.gltf.data.GLTFEntity;

public class GLTFMesh extends GLTFEntity {

  public Array<GLTFPrimitive> primitives;
  public float[] weights;
}
