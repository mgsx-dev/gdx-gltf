package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;

public class PBRCubemapAttribute extends CubemapAttribute {

  public static final String DiffuseEnvAlias = "DiffuseEnvSampler";
  public static final long DiffuseEnv = register(DiffuseEnvAlias);

  public static final String SpecularEnvAlias = "SpecularEnvSampler";
  public static final long SpecularEnv = register(SpecularEnvAlias);

  static {
    Mask |= DiffuseEnv | SpecularEnv;
  }

  public PBRCubemapAttribute(long type, TextureDescriptor<Cubemap> textureDescription) {
    super(type, textureDescription);
  }

  public PBRCubemapAttribute(long type, Cubemap cubemap) {
    super(type, cubemap);
  }

  public static Attribute createDiffuseEnv(Cubemap diffuseCubemap) {
    return new PBRCubemapAttribute(DiffuseEnv, diffuseCubemap);
  }

  public static Attribute createSpecularEnv(Cubemap specularCubemap) {
    return new PBRCubemapAttribute(SpecularEnv, specularCubemap);
  }

  @Override
  public Attribute copy() {
    return new PBRCubemapAttribute(type, textureDescription);
  }
}
