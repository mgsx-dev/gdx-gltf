package net.mgsx.gltf.exporters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.data.material.GLTFMaterial;
import net.mgsx.gltf.data.material.GLTFpbrMetallicRoughness;
import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.data.texture.GLTFNormalTextureInfo;
import net.mgsx.gltf.data.texture.GLTFOcclusionTextureInfo;
import net.mgsx.gltf.data.texture.GLTFSampler;
import net.mgsx.gltf.data.texture.GLTFTexture;
import net.mgsx.gltf.data.texture.GLTFTextureInfo;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;

class GLTFMaterialExporter {
	private final GLTFExporter base;

	public GLTFMaterialExporter(GLTFExporter base) {
		super();
		this.base = base;
	}
	
	public void export(Iterable<Node> nodes) {
		for(Node node : nodes){
			for(NodePart nodePart : node.parts){
				export(nodePart.material);
			}
			export(node.getChildren());
		}
	}

	private void export(Material material) {
		if(base.materialMapping.contains(material, true)) return;
		base.materialMapping.add(material);
		
		GLTFMaterial m = new GLTFMaterial();
		if(base.root.materials == null) base.root.materials = new Array<GLTFMaterial>();
		base.root.materials.add(m);
		

		m.name = material.id;
		
		boolean blending = false;
		for(Attribute a : material){
			if(a.type == ColorAttribute.Diffuse){
				pbr(m).baseColorFactor = GLTFExportTypes.rgba(defaultNull(Color.WHITE, (ColorAttribute)a));
			}
			else if(a.type == PBRColorAttribute.BaseColorFactor){
				pbr(m).baseColorFactor = GLTFExportTypes.rgba(defaultNull(Color.WHITE, (PBRColorAttribute)a));
			}
			else if(a.type == ColorAttribute.Emissive){
				m.emissiveFactor = GLTFExportTypes.rgb(defaultNull(Color.BLACK, (ColorAttribute)a));
			}
			else if(a.type == BlendingAttribute.Type){
				blending = true;
			}
			else if(a.type == IntAttribute.CullFace){
				m.doubleSided = defaultNull(true, ((IntAttribute)a).value == 0);
			}
			else if(a.type == FloatAttribute.AlphaTest){
				m.alphaCutoff = ((FloatAttribute)a).value;
			}
			else if(a.type == PBRFloatAttribute.Metallic){
				pbr(m).metallicFactor = ((PBRFloatAttribute)a).value;
			}
			else if(a.type == PBRFloatAttribute.Roughness){
				pbr(m).roughnessFactor = ((PBRFloatAttribute)a).value;
			}
			else if(a.type == PBRTextureAttribute.BaseColorTexture){
				pbr(m).baseColorTexture = texture((TextureAttribute)a);
			}
			else if(a.type == PBRTextureAttribute.MetallicRoughnessTexture){
				pbr(m).metallicRoughnessTexture = texture((TextureAttribute)a);
			}
			else if(a.type == PBRTextureAttribute.EmissiveTexture){
				m.emissiveTexture = texture((TextureAttribute)a);
			}
			else if(a.type == PBRTextureAttribute.NormalTexture){
				m.normalTexture = normalTexture((PBRTextureAttribute)a, material);
			}
			else if(a.type == PBRTextureAttribute.OcclusionTexture){
				m.occlusionTexture = occlusionTexture((PBRTextureAttribute)a, material);
			}
		}
		if(blending){
			if(m.alphaCutoff != null){
				m.alphaMode = "MASK";
			}else{
				m.alphaMode = "BLEND";
			}
		}
	}

	private Boolean defaultNull(boolean defValue, boolean value) {
		return defValue == value ? null : value;
	}

	protected Color defaultNull(Color defaultColor, Color color) {
		return color.equals(defaultColor) ? null : color;
	}
	private ColorAttribute defaultNull(Color defaultColor, ColorAttribute a) {
		return a.color.equals(defaultColor) ? null : a;
	}

	private GLTFOcclusionTextureInfo occlusionTexture(PBRTextureAttribute a, Material material) {
		GLTFOcclusionTextureInfo ti = new GLTFOcclusionTextureInfo();
		ti.strength = material.get(PBRFloatAttribute.class, PBRFloatAttribute.OcclusionStrength).value;
		ti.texCoord = a.uvIndex;
		ti.index = getTexture(a);
		return ti;
	}

	private GLTFNormalTextureInfo normalTexture(PBRTextureAttribute a, Material material) {
		GLTFNormalTextureInfo ti = new GLTFNormalTextureInfo();
		ti.scale = material.get(PBRFloatAttribute.class, PBRFloatAttribute.NormalScale).value;
		ti.texCoord = a.uvIndex;
		ti.index = getTexture(a);
		return ti;
	}

	private GLTFTextureInfo texture(TextureAttribute a) {
		GLTFTextureInfo ti = new GLTFTextureInfo();
		ti.texCoord = a.uvIndex;
		ti.index = getTexture(a);
		return ti;
	}

	private int getTexture(TextureAttribute a) {
		GLTFTexture t = new GLTFTexture();
		t.sampler = sampler(a);
		t.source = source(a.textureDescription.texture);
		
		if(base.root.textures == null) base.root.textures = new Array<GLTFTexture>();
		base.root.textures.add(t);
		return base.root.textures.size - 1;
	}

	private Integer source(Texture texture) {
		int imageIndex = base.textureMapping.indexOf(texture, true);
		if(imageIndex >= 0) return imageIndex;
		
		GLTFImage image = new GLTFImage();
		if(base.root.images == null) base.root.images = new Array<GLTFImage>();
		base.root.images.add(image);
		base.textureMapping.add(texture);
		base.binManager.export(image, texture, base.getImageName(texture));
		return base.root.images.size - 1;
	}

	private Integer sampler(TextureAttribute a) {
		GLTFSampler sampler = new GLTFSampler();
		sampler.minFilter = mapMin(a.textureDescription.minFilter);
		sampler.magFilter = mapMag(a.textureDescription.magFilter);
		sampler.wrapS = map(a.textureDescription.uWrap);
		sampler.wrapT = map(a.textureDescription.vWrap);
		if(sampler.minFilter == null && sampler.magFilter == null && sampler.wrapS == null && sampler.wrapT == null) return null;
		if(base.root.samplers == null) base.root.samplers = new Array<GLTFSampler>();
		base.root.samplers.add(sampler);
		return base.root.samplers.size-1;
	}

	private Integer map(TextureWrap wrap) {
		switch (wrap) {
		case ClampToEdge: return 33071;
		case MirroredRepeat:return 33648;
		default:
		case Repeat:return null;
		}
	}

	private Integer mapMag(TextureFilter filter) {
		switch (filter) {
		default:
		case Linear: return null;
		case Nearest:return 9728;
		}
	}
	private Integer mapMin(TextureFilter filter) {
		switch (filter) {
		default:
		case Linear: return null;
		case MipMap:
		case MipMapLinearLinear:return 9987;
		case MipMapLinearNearest:return 9985;
		case MipMapNearestLinear:return 9986;
		case MipMapNearestNearest:return 9984;
		case Nearest:return 9728;
		}
	}

	private GLTFpbrMetallicRoughness pbr(GLTFMaterial m) {
		if(m.pbrMetallicRoughness == null){
			m.pbrMetallicRoughness = new GLTFpbrMetallicRoughness();
		}
		return m.pbrMetallicRoughness;
	}
}
