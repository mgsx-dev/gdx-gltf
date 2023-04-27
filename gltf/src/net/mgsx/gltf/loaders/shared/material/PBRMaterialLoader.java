package net.mgsx.gltf.loaders.shared.material;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.math.MathUtils;

import net.mgsx.gltf.data.extensions.KHRMaterialsEmissiveStrength;
import net.mgsx.gltf.data.extensions.KHRMaterialsIOR;
import net.mgsx.gltf.data.extensions.KHRMaterialsIridescence;
import net.mgsx.gltf.data.extensions.KHRMaterialsPBRSpecularGlossiness;
import net.mgsx.gltf.data.extensions.KHRMaterialsSpecular;
import net.mgsx.gltf.data.extensions.KHRMaterialsTransmission;
import net.mgsx.gltf.data.extensions.KHRMaterialsUnlit;
import net.mgsx.gltf.data.extensions.KHRMaterialsVolume;
import net.mgsx.gltf.data.extensions.KHRTextureTransform;
import net.mgsx.gltf.data.material.GLTFMaterial;
import net.mgsx.gltf.data.material.GLTFpbrMetallicRoughness;
import net.mgsx.gltf.data.texture.GLTFTextureInfo;
import net.mgsx.gltf.loaders.exceptions.GLTFIllegalException;
import net.mgsx.gltf.loaders.shared.GLTFTypes;
import net.mgsx.gltf.loaders.shared.texture.TextureResolver;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFlagAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRHDRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRIridescenceAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRVolumeAttribute;

public class PBRMaterialLoader extends MaterialLoaderBase {

	public PBRMaterialLoader(TextureResolver textureResolver) {
		super(textureResolver, new Material(new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, Color.WHITE)));
	}
	
	@Override
	public Material loadMaterial(GLTFMaterial glMaterial) 
	{
		Material material = new Material();
		if(glMaterial.name != null) material.id = glMaterial.name;
		
		if(glMaterial.emissiveFactor != null){
			material.set(new ColorAttribute(ColorAttribute.Emissive, GLTFTypes.mapColor(glMaterial.emissiveFactor, Color.BLACK)));
		}
		
		if(glMaterial.emissiveTexture != null){
			material.set(getTexureMap(PBRTextureAttribute.EmissiveTexture, glMaterial.emissiveTexture));
		}
		
		if(glMaterial.doubleSided == Boolean.TRUE){
			material.set(IntAttribute.createCullFace(0)); // 0 to disable culling
		}
		
		if(glMaterial.normalTexture != null){
			material.set(getTexureMap(PBRTextureAttribute.NormalTexture, glMaterial.normalTexture));
			material.set(PBRFloatAttribute.createNormalScale(glMaterial.normalTexture.scale));
		}
		
		if(glMaterial.occlusionTexture != null){
			material.set(getTexureMap(PBRTextureAttribute.OcclusionTexture, glMaterial.occlusionTexture));
			material.set(PBRFloatAttribute.createOcclusionStrength(glMaterial.occlusionTexture.strength));
		}
		
		boolean alphaBlend = false;
		if("OPAQUE".equals(glMaterial.alphaMode)){
			// nothing to do
		}else if("MASK".equals(glMaterial.alphaMode)){ 
			float value = glMaterial.alphaCutoff == null ? 0.5f : glMaterial.alphaCutoff;
			material.set(FloatAttribute.createAlphaTest(value));
			material.set(new BlendingAttribute()); // necessary
		}else if("BLEND".equals(glMaterial.alphaMode)){
			material.set(new BlendingAttribute()); // opacity is set by pbrMetallicRoughness below
			alphaBlend = true;
		}else if(glMaterial.alphaMode != null){
			throw new GLTFIllegalException("unknow alpha mode : " + glMaterial.alphaMode);
		}
		
		if(glMaterial.pbrMetallicRoughness != null){
			GLTFpbrMetallicRoughness p = glMaterial.pbrMetallicRoughness;
			
			Color baseColorFactor = GLTFTypes.mapColor(p.baseColorFactor, Color.WHITE);
			
			material.set(new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, baseColorFactor));

			material.set(PBRFloatAttribute.createMetallic(p.metallicFactor));
			material.set(PBRFloatAttribute.createRoughness(p.roughnessFactor));
			
			if(p.metallicRoughnessTexture != null){
				material.set(getTexureMap(PBRTextureAttribute.MetallicRoughnessTexture, p.metallicRoughnessTexture));
			}
			
			if(p.baseColorTexture != null){
				material.set(getTexureMap(PBRTextureAttribute.BaseColorTexture, p.baseColorTexture));
			}
			
			if(alphaBlend){
				material.get(BlendingAttribute.class, BlendingAttribute.Type).opacity = baseColorFactor.a;
			}
		}
		
		// can have both PBR base and ext
		if(glMaterial.extensions != null){
			{
				KHRMaterialsPBRSpecularGlossiness ext = glMaterial.extensions.get(KHRMaterialsPBRSpecularGlossiness.class, KHRMaterialsPBRSpecularGlossiness.EXT);
				if(ext != null){
					Gdx.app.error("GLTF", KHRMaterialsPBRSpecularGlossiness.EXT + " extension is deprecated by glTF 2.0 specification and not fully supported.");
					
					material.set(new ColorAttribute(ColorAttribute.Diffuse, GLTFTypes.mapColor(ext.diffuseFactor, Color.WHITE)));
					material.set(new ColorAttribute(ColorAttribute.Specular, GLTFTypes.mapColor(ext.specularFactor, Color.WHITE)));
					
					// not sure how to map normalized gloss to exponent ...
					material.set(new FloatAttribute(FloatAttribute.Shininess, MathUtils.lerp(1, 100, ext.glossinessFactor)));
					if(ext.diffuseTexture != null){
						material.set(getTexureMap(PBRTextureAttribute.Diffuse, ext.diffuseTexture));
					}
					if(ext.specularGlossinessTexture != null){
						material.set(getTexureMap(PBRTextureAttribute.Specular, ext.specularGlossinessTexture));
					}
				}
			}
			{
				KHRMaterialsUnlit ext = glMaterial.extensions.get(KHRMaterialsUnlit.class, KHRMaterialsUnlit.EXT);
				if(ext != null){
					material.set(new PBRFlagAttribute(PBRFlagAttribute.Unlit));
				}
			}
			{
				KHRMaterialsTransmission ext = glMaterial.extensions.get(KHRMaterialsTransmission.class, KHRMaterialsTransmission.EXT);
				if(ext != null){
					material.set(PBRFloatAttribute.createTransmissionFactor(ext.transmissionFactor));
					if(ext.transmissionTexture != null){
						material.set(getTexureMap(PBRTextureAttribute.TransmissionTexture, ext.transmissionTexture));
					}
				}
			}
			{
				KHRMaterialsVolume ext = glMaterial.extensions.get(KHRMaterialsVolume.class, KHRMaterialsVolume.EXT);
				if(ext != null){
					material.set(new PBRVolumeAttribute(ext.thicknessFactor, ext.attenuationDistance == null ? 0f : ext.attenuationDistance, GLTFTypes.mapColor(ext.attenuationColor, Color.WHITE)));
					if(ext.thicknessTexture != null){
						material.set(getTexureMap(PBRTextureAttribute.ThicknessTexture, ext.thicknessTexture));
					}
				}
			}
			{
				KHRMaterialsIOR ext = glMaterial.extensions.get(KHRMaterialsIOR.class, KHRMaterialsIOR.EXT);
				if(ext != null){
					material.set(PBRFloatAttribute.createIOR(ext.ior));
				}
			}
			{
				KHRMaterialsSpecular ext = glMaterial.extensions.get(KHRMaterialsSpecular.class, KHRMaterialsSpecular.EXT);
				if(ext != null){
					material.set(PBRFloatAttribute.createSpecularFactor(ext.specularFactor));
					material.set(new PBRHDRColorAttribute(PBRHDRColorAttribute.Specular, ext.specularColorFactor[0], ext.specularColorFactor[1], ext.specularColorFactor[2]));
					if(ext.specularTexture != null){
						material.set(getTexureMap(PBRTextureAttribute.SpecularFactorTexture, ext.specularTexture));
					}
					if(ext.specularColorTexture != null){
						material.set(getTexureMap(PBRTextureAttribute.SpecularColorTexture, ext.specularColorTexture));
					}
				}
			}
			{
				KHRMaterialsIridescence ext = glMaterial.extensions.get(KHRMaterialsIridescence.class, KHRMaterialsIridescence.EXT);
				if(ext != null){
					material.set(new PBRIridescenceAttribute(ext.iridescenceFactor, ext.iridescenceIor, ext.iridescenceThicknessMinimum, ext.iridescenceThicknessMaximum));
					if(ext.iridescenceTexture != null){
						material.set(getTexureMap(PBRTextureAttribute.IridescenceTexture, ext.iridescenceTexture));
					}
					if(ext.iridescenceThicknessTexture != null){
						material.set(getTexureMap(PBRTextureAttribute.IridescenceThicknessTexture, ext.iridescenceThicknessTexture));
					}
				}
			}
			{
				KHRMaterialsEmissiveStrength ext = glMaterial.extensions.get(KHRMaterialsEmissiveStrength.class, KHRMaterialsEmissiveStrength.EXT);
				if(ext != null){
					material.set(PBRFloatAttribute.createEmissiveIntensity(ext.emissiveStrength));
				}
			}
		}
		
		return material;
	}

	protected PBRTextureAttribute getTexureMap(long type, GLTFTextureInfo glMap) {
		TextureDescriptor<Texture> textureDescriptor = textureResolver.getTexture(glMap);
		
		PBRTextureAttribute attribute = new PBRTextureAttribute(type, textureDescriptor);
		
		// pbr texture attr currently supports up 2 texture coords (0, 1), clamp index to avoid loading errors
		attribute.uvIndex = Math.min(glMap.texCoord, 1);
		
		if(glMap.extensions != null){
			{
				KHRTextureTransform ext = glMap.extensions.get(KHRTextureTransform.class, KHRTextureTransform.EXT);
				if(ext != null){
					attribute.offsetU = ext.offset[0];
					attribute.offsetV = ext.offset[1];
					attribute.scaleU = ext.scale[0];
					attribute.scaleV = ext.scale[1];
					attribute.rotationUV = ext.rotation;
					if(ext.texCoord != null){
						attribute.uvIndex = ext.texCoord;
					}
				}
			}
		}
		
		return attribute;
	}
	
}
