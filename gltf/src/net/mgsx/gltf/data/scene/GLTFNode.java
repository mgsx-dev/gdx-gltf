package net.mgsx.gltf.data.scene;

import com.badlogic.gdx.utils.Array;
import net.mgsx.gltf.data.GLTFEntity;

public class GLTFNode extends GLTFEntity {

  public Array<Integer> children;
  public float[] matrix;
  public float[] translation;
  public float[] rotation;
  public float[] scale;

  public Integer mesh;
  public Integer camera;
  public Integer skin;

  public float[] weights;
}
