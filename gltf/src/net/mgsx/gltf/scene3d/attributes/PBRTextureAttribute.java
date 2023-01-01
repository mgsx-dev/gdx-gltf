package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.math.MathUtils;

public class PBRTextureAttribute extends TextureAttribute {

  public static final String BaseColorTextureAlias = "diffuseTexture";
  public static final long BaseColorTexture = register(BaseColorTextureAlias);

  public static final String EmissiveTextureAlias = "emissiveTexture";
  public static final long EmissiveTexture = register(EmissiveTextureAlias);

  public static final String NormalTextureAlias = "normalTexture";
  public static final long NormalTexture = register(NormalTextureAlias);

  public static final String MetallicRoughnessTextureAlias = "MetallicRoughnessSampler";
  public static final long MetallicRoughnessTexture = register(MetallicRoughnessTextureAlias);

  public static final String OcclusionTextureAlias = "OcclusionSampler";
  public static final long OcclusionTexture = register(OcclusionTextureAlias);

  // IBL environnement only
  public static final String BRDFLUTTextureAlias = "brdfLUTSampler";
  public static final long BRDFLUTTexture = register(BRDFLUTTextureAlias);

  public static final String TransmissionTextureAlias = "TransmissionTexture";
  public static final long TransmissionTexture = register(TransmissionTextureAlias);

  public static final String ThicknessTextureAlias = "ThicknessTexture";
  public static final long ThicknessTexture = register(ThicknessTextureAlias);

  public static final String SpecularFactorTextureAlias = "SpecularFactorTexture";
  public static final long SpecularFactorTexture = register(SpecularFactorTextureAlias);

  public static final String IridescenceTextureAlias = "IridescenceTexture";
  public static final long IridescenceTexture = register(IridescenceTextureAlias);

  public static final String IridescenceThicknessTextureAlias = "IridescenceThicknessTexture";
  public static final long IridescenceThicknessTexture = register(IridescenceThicknessTextureAlias);

  public static final String TransmissionSourceTextureAlias = "TransmissionSourceTexture";
  public static final long TransmissionSourceTexture = register(TransmissionSourceTextureAlias);

  public static final String SpecularColorTextureAlias = "SpecularColorTexture";
  public static final long SpecularColorTexture = register(SpecularColorTextureAlias);

  static {
    Mask |= MetallicRoughnessTexture | OcclusionTexture | BaseColorTexture | NormalTexture | EmissiveTexture | BRDFLUTTexture | TransmissionTexture | ThicknessTexture | SpecularFactorTexture | IridescenceTexture | IridescenceThicknessTexture | TransmissionSourceTexture | SpecularColorTexture;
  }

  public float rotationUV = 0f;

  public PBRTextureAttribute(long type) {
    super(type);
  }

  public PBRTextureAttribute(long type, TextureDescriptor<Texture> textureDescription) {
    super(type, textureDescription);
  }

  public PBRTextureAttribute(long type, Texture texture) {
    super(type, texture);
  }

  public PBRTextureAttribute(long type, TextureRegion region) {
    super(type, region);
  }

  public PBRTextureAttribute(PBRTextureAttribute attribute) {
    super(attribute);
    this.rotationUV = attribute.rotationUV;
  }

  public static PBRTextureAttribute createBaseColorTexture(Texture texture) {
    return new PBRTextureAttribute(BaseColorTexture, texture);
  }

  public static PBRTextureAttribute createEmissiveTexture(Texture texture) {
    return new PBRTextureAttribute(EmissiveTexture, texture);
  }

  public static PBRTextureAttribute createNormalTexture(Texture texture) {
    return new PBRTextureAttribute(NormalTexture, texture);
  }

  public static PBRTextureAttribute createMetallicRoughnessTexture(Texture texture) {
    return new PBRTextureAttribute(MetallicRoughnessTexture, texture);
  }

  public static PBRTextureAttribute createOcclusionTexture(Texture texture) {
    return new PBRTextureAttribute(OcclusionTexture, texture);
  }

  public static PBRTextureAttribute createBRDFLookupTexture(Texture texture) {
    return new PBRTextureAttribute(BRDFLUTTexture, texture);
  }

  public static PBRTextureAttribute createTransmissionTexture(Texture texture) {
    return new PBRTextureAttribute(TransmissionTexture, texture);
  }

  public static PBRTextureAttribute createThicknessTexture(Texture texture) {
    return new PBRTextureAttribute(ThicknessTexture, texture);
  }

  public static PBRTextureAttribute createSpecularFactorTexture(Texture texture) {
    return new PBRTextureAttribute(SpecularFactorTexture, texture);
  }

  public static PBRTextureAttribute createIridescenceTexture(Texture texture) {
    return new PBRTextureAttribute(IridescenceTexture, texture);
  }

  public static PBRTextureAttribute createIridescenceThicknessTexture(Texture texture) {
    return new PBRTextureAttribute(IridescenceThicknessTexture, texture);
  }

  public static PBRTextureAttribute createBaseColorTexture(TextureRegion region) {
    return new PBRTextureAttribute(BaseColorTexture, region);
  }

  public static PBRTextureAttribute createEmissiveTexture(TextureRegion region) {
    return new PBRTextureAttribute(EmissiveTexture, region);
  }

  public static PBRTextureAttribute createNormalTexture(TextureRegion region) {
    return new PBRTextureAttribute(NormalTexture, region);
  }

  public static PBRTextureAttribute createMetallicRoughnessTexture(TextureRegion region) {
    return new PBRTextureAttribute(MetallicRoughnessTexture, region);
  }

  public static PBRTextureAttribute createOcclusionTexture(TextureRegion region) {
    return new PBRTextureAttribute(OcclusionTexture, region);
  }

  public static PBRTextureAttribute createBRDFLookupTexture(TextureRegion region) {
    return new PBRTextureAttribute(BRDFLUTTexture, region);
  }

  public static PBRTextureAttribute createTransmissionTexture(TextureRegion region) {
    return new PBRTextureAttribute(TransmissionTexture, region);
  }

  public static PBRTextureAttribute createThicknessTexture(TextureRegion region) {
    return new PBRTextureAttribute(ThicknessTexture, region);
  }

  public static PBRTextureAttribute createSpecularFactorTexture(TextureRegion region) {
    return new PBRTextureAttribute(SpecularFactorTexture, region);
  }

  public static PBRTextureAttribute createIridescenceTexture(TextureRegion region) {
    return new PBRTextureAttribute(IridescenceTexture, region);
  }

  public static PBRTextureAttribute createIridescenceThicknessTexture(TextureRegion region) {
    return new PBRTextureAttribute(IridescenceThicknessTexture, region);
  }

  @Override
  public Attribute copy() {
    return new PBRTextureAttribute(this);
  }

  @Override
  public int compareTo(Attribute o) {
    int r = super.compareTo(o);
    if (r != 0) return r;
    if (o instanceof PBRTextureAttribute) {
      PBRTextureAttribute other = (PBRTextureAttribute) o;
      if (!MathUtils.isEqual(rotationUV, other.rotationUV)) return rotationUV < other.rotationUV ? -1 : 1;
    }
    return 0;
  }
}
