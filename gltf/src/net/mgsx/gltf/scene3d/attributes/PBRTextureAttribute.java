package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;

public class PBRTextureAttribute extends TextureAttribute
{
	public final static String BaseColorTextureAlias = "diffuseTexture";
	public final static long BaseColorTexture = register(BaseColorTextureAlias);
	
	public final static String EmissiveTextureAlias = "emissiveTexture";
	public final static long EmissiveTexture = register(EmissiveTextureAlias);
	
	public final static String NormalTextureAlias = "normalTexture";
	public final static long NormalTexture = register(NormalTextureAlias);
	
	public final static String MetallicRoughnessTextureAlias = "MetallicRoughnessSampler";
	public final static long MetallicRoughnessTexture = register(MetallicRoughnessTextureAlias);

	public final static String OcclusionTextureAlias = "OcclusionSampler";
	public final static long OcclusionTexture = register(OcclusionTextureAlias);
	
	// IBL environnement only
	public final static String BRDFLUTTextureAlias = "brdfLUTSampler";
	public final static long BRDFLUTTexture = register(BRDFLUTTextureAlias);
	
	
	static{
		Mask |= MetallicRoughnessTexture | OcclusionTexture | BaseColorTexture | NormalTexture | EmissiveTexture | BRDFLUTTexture;
	}
	
	public float rotationUV = 0f;
	
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

	@Override
	public Attribute copy() {
		return new PBRTextureAttribute(this);
	}
}
