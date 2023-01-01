package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

public class PBRColorAttribute extends ColorAttribute {

  public static final String BaseColorFactorAlias = "BaseColorFactor";
  public static final long BaseColorFactor = register(BaseColorFactorAlias);

  public static PBRColorAttribute createBaseColorFactor(Color color) {
    return new PBRColorAttribute(BaseColorFactor, color);
  }

  static {
    Mask |= BaseColorFactor;
  }

  public PBRColorAttribute(long type, Color color) {
    super(type, color);
  }

  @Override
  public Attribute copy() {
    return new PBRColorAttribute(type, color);
  }
}
