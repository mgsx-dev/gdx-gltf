package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;

public class PBRTextureAttribute extends TextureAttribute
{
	public final static String BaseColorTextureAlias = "baseColorTexture";
	public final static long BaseColorTexture = register(BaseColorTextureAlias);
	
	public final static String EmissiveTextureAlias = "PBR_emissiveTexture";
	public final static long EmissiveTexture = register(EmissiveTextureAlias);
	
	public final static String NormalTextureAlias = "PBR_normalTexture";
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
	public PBRTextureAttribute(PBRTextureAttribute attribute) {
		super(attribute);
		this.rotationUV = attribute.rotationUV;
	}
	public static PBRTextureAttribute createMetallicRoughnessTexture(Texture texture) {
		return new PBRTextureAttribute(MetallicRoughnessTexture, texture);
	}
	public static PBRTextureAttribute createOcclusionTexture(Texture texture) {
		return new PBRTextureAttribute(OcclusionTexture, texture);
	}

	@Override
	public Attribute copy() {
		return new PBRTextureAttribute(this);
	}
}
