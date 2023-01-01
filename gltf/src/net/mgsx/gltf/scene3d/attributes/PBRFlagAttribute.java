package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;

public class PBRFlagAttribute extends Attribute {

  public static final String UnlitAlias = "unlit";
  public static final long Unlit = register(UnlitAlias);

  public PBRFlagAttribute(long type) {
    super(type);
  }

  @Override
  public Attribute copy() {
    return new PBRFlagAttribute(type);
  }

  @Override
  public int compareTo(Attribute o) {
    return (int) (type - o.type);
  }
}
