package net.mgsx.gltf.loaders.shared.material;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gltf.data.extensions.KHRMaterialsPBRSpecularGlossiness;
import net.mgsx.gltf.data.extensions.KHRTextureTransform;
import net.mgsx.gltf.data.material.GLTFMaterial;
import net.mgsx.gltf.data.material.GLTFpbrMetallicRoughness;
import net.mgsx.gltf.data.texture.GLTFTextureInfo;
import net.mgsx.gltf.loaders.shared.GLTFTypes;
import net.mgsx.gltf.loaders.shared.texture.TextureResolver;

/**
 * Legacy material loader using libgdx material definition.
 * Limitations :
 * - UV rotation not supported 
 * - normal scale
 * - normal texture (supported but not used in default shader)
 * - ambient factor
 * - metallic is mapped to reflection
 */
public class DefaultMaterialLoader extends MaterialLoaderBase {

	public DefaultMaterialLoader(TextureResolver textureResolver) {
		super(textureResolver, new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE)));
	}

	@Override
	public Material loadMaterial(GLTFMaterial glMaterial) 
	{
		// TODO handle unlit model (disable lighting)
		
		Material material = new Material();
		if(glMaterial.name != null) material.id = glMaterial.name;
		
		if(glMaterial.emissiveFactor != null){
			material.set(new ColorAttribute(ColorAttribute.Emissive, GLTFTypes.mapColor(glMaterial.emissiveFactor, Color.BLACK)));
		}
		
		if(glMaterial.emissiveTexture != null){
			material.set(getTexureMap(TextureAttribute.Emissive, glMaterial.emissiveTexture));
		}
		
		if(glMaterial.doubleSided == Boolean.TRUE){
			material.set(IntAttribute.createCullFace(0)); // 0 to disable culling
		}
		
		if(glMaterial.normalTexture != null){
			material.set(getTexureMap(TextureAttribute.Normal, glMaterial.normalTexture));
		}
		
		if(glMaterial.occlusionTexture != null){
			material.set(getTexureMap(TextureAttribute.Ambient, glMaterial.occlusionTexture));
		}
		
		if("OPAQUE".equals(glMaterial.alphaMode)){
			// nothing to do
		}else if("MASK".equals(glMaterial.alphaMode)){ 
			float value = glMaterial.alphaCutoff == null ? 0.5f : glMaterial.alphaCutoff;
			material.set(FloatAttribute.createAlphaTest(value));
			material.set(new BlendingAttribute()); // necessary
		}else if("BLEND".equals(glMaterial.alphaMode)){
			material.set(new BlendingAttribute());
		}else if(glMaterial.alphaMode != null){
			throw new GdxRuntimeException("unknow alpha mode : " + glMaterial.alphaMode);
		}
		
		if(glMaterial.pbrMetallicRoughness != null){
			GLTFpbrMetallicRoughness p = glMaterial.pbrMetallicRoughness;
			
			material.set(new ColorAttribute(ColorAttribute.Diffuse, GLTFTypes.mapColor(p.baseColorFactor, Color.WHITE)));

			material.set(ColorAttribute.createReflection(p.metallicFactor, p.metallicFactor, p.metallicFactor, 1.0f));
//			material.set(PBRFloatAttribute.createRoughness(p.roughnessFactor));
			
			if(p.metallicRoughnessTexture != null){
				material.set(getTexureMap(TextureAttribute.Reflection, p.metallicRoughnessTexture));
			}
			
			if(p.baseColorTexture != null){
				material.set(getTexureMap(TextureAttribute.Diffuse, p.baseColorTexture));
			}
		}
		
		// can have both PBR base and ext
		if(glMaterial.extensions != null){
			{
				KHRMaterialsPBRSpecularGlossiness ext = glMaterial.extensions.get(KHRMaterialsPBRSpecularGlossiness.class, KHRMaterialsPBRSpecularGlossiness.EXT);
				if(ext != null){
					material.set(new ColorAttribute(ColorAttribute.Diffuse, GLTFTypes.mapColor(ext.diffuseFactor, Color.WHITE)));
					material.set(new ColorAttribute(ColorAttribute.Specular, GLTFTypes.mapColor(ext.specularFactor, Color.WHITE)));
					
					// TODO not sure how to map normalized gloss to exponent ...
					material.set(new FloatAttribute(FloatAttribute.Shininess, MathUtils.lerp(1, 100, ext.glossinessFactor)));
					if(ext.diffuseTexture != null){
						// TODO use another attribe : DiffuseTexture
						material.set(getTexureMap(TextureAttribute.Diffuse, ext.diffuseTexture));
					}
					if(ext.specularGlossinessTexture != null){
						// TODO use another attribute : SpecularTexture
						material.set(getTexureMap(TextureAttribute.Specular, ext.specularGlossinessTexture));
					}
				}
			}
		}
		
		return material;
	}

	private TextureAttribute getTexureMap(long type, GLTFTextureInfo glMap) {
		TextureDescriptor<Texture> textureDescriptor = textureResolver.getTexture(glMap);
		
		TextureAttribute attribute = new TextureAttribute(type, textureDescriptor);
		attribute.uvIndex = glMap.texCoord;
		
		if(glMap.extensions != null){
			{
				KHRTextureTransform ext = glMap.extensions.get(KHRTextureTransform.class, KHRTextureTransform.EXT);
				if(ext != null){
					attribute.offsetU = ext.offset[0];
					attribute.offsetV = ext.offset[1];
					attribute.scaleU = ext.scale[0];
					attribute.scaleV = ext.scale[1];
					if(ext.texCoord != null){
						attribute.uvIndex = ext.texCoord;
					}
				}
			}
		}
		
		return attribute;
	}

}
