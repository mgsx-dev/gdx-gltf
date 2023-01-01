package net.mgsx.gltf.exporters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import static java.lang.String.format;

class GLTFExportTypes {

  static float[] rgb(ColorAttribute a) {
    return a == null ? null : rgb(a.color);
  }

  static float[] rgba(ColorAttribute a) {
    return a == null ? null : rgba(a.color);
  }

  static float[] rgb(Color color) {
    return color == null ? null : new float[]{color.r, color.g, color.b};
  }

  static float[] rgba(Color color) {
    return color == null ? null : new float[]{color.r, color.g, color.b, color.a};
  }

  static float[] rgb(Color color, Color nullColor) {
    return color.equals(nullColor) ? null : rgb(color);
  }

  static float[] toArray(Vector3 v) {
    return new float[]{v.x, v.y, v.z};
  }

  static float[] toArray(Quaternion v) {
    return new float[]{v.x, v.y, v.z, v.w};
  }

  private GLTFExportTypes() {
    throw new IllegalStateException(format("Cannot create instance of %s", getClass()));
  }
}
