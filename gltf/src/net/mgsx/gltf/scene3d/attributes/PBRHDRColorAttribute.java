package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;

/**
 * HDR Color attribute only contains RGB values with High Dynamic Range.
 * RGB values are not clamped to 0-1 range.
 */
public class PBRHDRColorAttribute extends Attribute {

  public static final String SpecularAlias = "specularColorHDR";
  public static final long Specular = register(SpecularAlias);

  public float r;
  public float g;
  public float b;

  public PBRHDRColorAttribute(long type, float r, float g, float b) {
    super(type);
    this.r = r;
    this.g = g;
    this.b = b;
  }

  public PBRHDRColorAttribute set(float r, float g, float b) {
    this.r = r;
    this.g = g;
    this.b = b;
    return this;
  }

  @Override
  public Attribute copy() {
    return new PBRHDRColorAttribute(type, r, g, b);
  }

  @Override
  public int compareTo(Attribute o) {
    if (type != o.type) return (int) (type - o.type);
    PBRHDRColorAttribute a = (PBRHDRColorAttribute) o;
    int cr = Float.compare(r, a.r);
    if (cr != 0) return cr;
    int cg = Float.compare(g, a.g);
    if (cg != 0) return cg;
    int cb = Float.compare(b, a.b);
    if (cb != 0) return cb;
    return 0;
  }
}
