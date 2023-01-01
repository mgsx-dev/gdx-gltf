package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class PBRMatrixAttribute extends Attribute {
  public static final String EnvRotationAlias = "envRotation";
  public static final long EnvRotation = register(EnvRotationAlias);

  public static PBRMatrixAttribute createEnvRotation(float azymuthAngleDegree) {
    return new PBRMatrixAttribute(EnvRotation).set(azymuthAngleDegree);
  }

  public static PBRMatrixAttribute createEnvRotation(Matrix4 matrix) {
    return new PBRMatrixAttribute(EnvRotation).set(matrix);
  }

  public final Matrix4 matrix = new Matrix4();

  public PBRMatrixAttribute(long type) {
    super(type);
  }

  private PBRMatrixAttribute set(Matrix4 matrix) {
    this.matrix.set(matrix);
    return this;
  }

  public PBRMatrixAttribute set(float azymuthAngleDegree) {
    this.matrix.setToRotation(Vector3.Y, azymuthAngleDegree);
    return this;
  }

  @Override
  public Attribute copy() {
    return new PBRMatrixAttribute(type).set(matrix);
  }

  @Override
  public int compareTo(Attribute o) {
    return (int) (type - o.type);
  }
}
