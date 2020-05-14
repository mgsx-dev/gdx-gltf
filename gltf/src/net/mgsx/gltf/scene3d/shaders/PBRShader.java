package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRVertexAttributes;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.model.WeightVector;

public class PBRShader extends DefaultShader
{
	private static final Vector2 v2 = new Vector2();
	
	public final static Uniform baseColorTextureUniform = new Uniform("u_diffuseTexture", PBRTextureAttribute.BaseColorTexture);
	public final static Setter baseColorTextureSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			final int unit = shader.context.textureBinder.bind(((TextureAttribute)(combinedAttributes
				.get(PBRTextureAttribute.BaseColorTexture))).textureDescription);
			shader.set(inputID, unit);
		}
	};

	public final static Uniform baseColorFactorUniform = new Uniform("u_BaseColorFactor", PBRColorAttribute.BaseColorFactor);
	public final static Setter baseColorFactorSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			ColorAttribute attribute = combinedAttributes.get(ColorAttribute.class, PBRColorAttribute.BaseColorFactor);
			Color color = attribute == null ? Color.WHITE : attribute.color;
			shader.set(inputID, color);
		}
	};

	public final static Uniform emissiveTextureUniform = new Uniform("u_emissiveTexture", PBRTextureAttribute.EmissiveTexture);
	public final static Setter emissiveTextureSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			final int unit = shader.context.textureBinder.bind(((TextureAttribute)(combinedAttributes
				.get(PBRTextureAttribute.EmissiveTexture))).textureDescription);
			shader.set(inputID, unit);
		}
	};

	public final static Uniform normalTextureUniform = new Uniform("u_normalTexture", PBRTextureAttribute.NormalTexture);
	public final static Setter normalTextureSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			final int unit = shader.context.textureBinder.bind(((TextureAttribute)(combinedAttributes
				.get(PBRTextureAttribute.NormalTexture))).textureDescription);
			shader.set(inputID, unit);
		}
	};

	public final static Uniform metallicRoughnessTextureUniform = new Uniform("u_MetallicRoughnessSampler", PBRTextureAttribute.MetallicRoughnessTexture);
	public final static Setter metallicRoughnessTextureSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			final int unit = shader.context.textureBinder.bind(((TextureAttribute)(combinedAttributes
				.get(PBRTextureAttribute.MetallicRoughnessTexture))).textureDescription);
			shader.set(inputID, unit);
		}
	};
	
	public final static Uniform metallicRoughnessUniform = new Uniform("u_MetallicRoughnessValues");
	public final static Setter metallicRoughnessSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			PBRFloatAttribute metallicAttribute = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.Metallic);
			PBRFloatAttribute roughnessAttribute = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.Roughness);
			float metallic = metallicAttribute == null ? 1f : metallicAttribute.value;
			float roughness = roughnessAttribute == null ? 1f : roughnessAttribute.value;
			shader.set(inputID, v2.set(metallic, roughness));
		}
	};

	public final static Uniform normalScaleUniform = new Uniform("u_NormalScale");
	public final static Setter normalScaleSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			PBRFloatAttribute normalScaleAttribute = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.NormalScale);
			float normalScale = normalScaleAttribute == null ? 1f : normalScaleAttribute.value;
			shader.set(inputID, normalScale);
		}
	};

	public final static Uniform occlusionStrengthUniform = new Uniform("u_OcclusionStrength");
	public final static Setter occlusionStrengthSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			PBRFloatAttribute occlusionStrengthAttribute = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.OcclusionStrength);
			float occlusionStrength = occlusionStrengthAttribute == null ? 1f : occlusionStrengthAttribute.value;
			shader.set(inputID, occlusionStrength);
		}
	};

	
	public final static Uniform occlusionTextureUniform = new Uniform("u_OcclusionSampler", PBRTextureAttribute.OcclusionTexture);
	public final static Setter occlusionTextureSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			final int unit = shader.context.textureBinder.bind(((TextureAttribute)(combinedAttributes
				.get(PBRTextureAttribute.OcclusionTexture))).textureDescription);
			shader.set(inputID, unit);
		}
	};

	public final static Uniform diffuseEnvTextureUniform = new Uniform("u_DiffuseEnvSampler", PBRCubemapAttribute.DiffuseEnv);
	public final static Setter diffuseEnvTextureSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			PBRCubemapAttribute diffuseEnvAttribute = combinedAttributes.get(PBRCubemapAttribute.class, PBRCubemapAttribute.DiffuseEnv);
			final int unit = shader.context.textureBinder.bind(diffuseEnvAttribute.textureDescription);
			shader.set(inputID, unit);
		}
		
	};
	
	public final static Uniform specularEnvTextureUniform = new Uniform("u_SpecularEnvSampler", PBRCubemapAttribute.SpecularEnv);
	public final static Setter specularEnvTextureSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			PBRCubemapAttribute specularEnvAttribute = combinedAttributes.get(PBRCubemapAttribute.class, PBRCubemapAttribute.SpecularEnv);
			final int unit = shader.context.textureBinder.bind(specularEnvAttribute.textureDescription);
			shader.set(inputID, unit);
		}
		
	};
	
	public final static Uniform brdfLUTTextureUniform = new Uniform("u_brdfLUT");
	public final static Setter brdfLUTTextureSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			PBRTextureAttribute attribute = combinedAttributes.get(PBRTextureAttribute.class, PBRTextureAttribute.BRDFLUTTexture);
			if(attribute != null){
				final int unit = shader.context.textureBinder.bind(attribute.textureDescription);
				shader.set(inputID, unit);
			}
		}
	};
	
	public final static Uniform shadowBiasUniform = new Uniform("u_shadowBias");
	public final static Setter shadowBiasSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			PBRFloatAttribute attribute = combinedAttributes.get(PBRFloatAttribute.class, PBRFloatAttribute.ShadowBias);
			float value = attribute == null ? 0f : attribute.value;
			shader.set(inputID, value);
		}
	};

	public final static Uniform fogEquationUniform = new Uniform("u_fogEquation");
	public final static Setter fogEquationSetter = new LocalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			FogAttribute attribute = combinedAttributes.get(FogAttribute.class, FogAttribute.FogEquation);
			Vector3 value = attribute == null ? Vector3.Zero : attribute.value;
			shader.set(inputID, value);
		}
	};

	private static final PBRTextureAttribute transformTexture [] = {null, null};

	public final int u_metallicRoughness;
	public final int u_occlusionStrength; 
	public final int u_metallicRoughnessTexture;
	public final int u_occlusionTexture;
	public final int u_DiffuseEnvSampler;
	public final int u_SpecularEnvSampler;
	public final int u_brdfLUTTexture;
	public final int u_NormalScale;
	public final int u_BaseColorTexture;
	public final int u_NormalTexture;
	public final int u_EmissiveTexture;
	public final int u_BaseColorFactor;
	public final int u_FogEquation;
	public final int u_ShadowBias;

	// morph targets
	private int u_morphTargets1;
	private int u_morphTargets2;
	
	private int u_mipmapScale;

	private int u_texCoord0Transform;
	private int u_texCoord1Transform;

	private int u_ambientLight;
	
	private long textureCoordinateMapMask;

	private long morphTargetsMask;
	
	private int vertexColorLayers;
	
	private static final Matrix3 textureTransform = new Matrix3();
	
	public PBRShader(Renderable renderable, Config config, String prefix) {
		super(renderable, config, prefix);
		
		textureCoordinateMapMask = getTextureCoordinateMapMask(renderable.material);
		
		morphTargetsMask = computeMorphTargetsMask(renderable);
		
		vertexColorLayers = computeVertexColorLayers(renderable);
		
		// base color
		u_BaseColorTexture = register(baseColorTextureUniform, baseColorTextureSetter);
		u_BaseColorFactor = register(baseColorFactorUniform, baseColorFactorSetter);
		
		// emissive
		u_EmissiveTexture = register(emissiveTextureUniform, emissiveTextureSetter);
		
		// environment
		u_DiffuseEnvSampler = register(diffuseEnvTextureUniform, diffuseEnvTextureSetter);
		u_SpecularEnvSampler = register(specularEnvTextureUniform, specularEnvTextureSetter);
		
		// metallic roughness
		u_metallicRoughness = register(metallicRoughnessUniform, metallicRoughnessSetter);
		u_metallicRoughnessTexture = register(metallicRoughnessTextureUniform, metallicRoughnessTextureSetter);

		// occlusion
		u_occlusionTexture = register(occlusionTextureUniform, occlusionTextureSetter);
		u_occlusionStrength = register(occlusionStrengthUniform, occlusionStrengthSetter);
		
		u_brdfLUTTexture = register(brdfLUTTextureUniform, brdfLUTTextureSetter);
		
		// normal map
		u_NormalTexture = register(normalTextureUniform, normalTextureSetter);
		u_NormalScale = register(normalScaleUniform, normalScaleSetter);
		
		u_ShadowBias = register(shadowBiasUniform, shadowBiasSetter);
		
		u_FogEquation = register(fogEquationUniform, fogEquationSetter);
		
	}

	private int computeVertexColorLayers(Renderable renderable) {
		int num = 0;
		VertexAttributes vertexAttributes = renderable.meshPart.mesh.getVertexAttributes();
		final int n = vertexAttributes.size();
		for (int i = 0; i < n; i++) {
			final VertexAttribute attr = vertexAttributes.get(i);
			if (attr.usage == VertexAttributes.Usage.ColorUnpacked) num++;
		}
		return num;
	}

	@Override
	public boolean canRender(Renderable renderable) {
		// TODO properly determine if current shader can render this renderable.
		
		// compare texture coordinates mapping
		long textureCoordinateMapMask = getTextureCoordinateMapMask(renderable.material);
		if(textureCoordinateMapMask != this.textureCoordinateMapMask){
			return false;
		}
		
		// compare morph targets
		if(this.morphTargetsMask != computeMorphTargetsMask(renderable)) return false;
		
		// compare vertex colors count
		if(this.vertexColorLayers != computeVertexColorLayers(renderable)) return false;
		
		return super.canRender(renderable);
	}
	
	public long computeMorphTargetsMask(Renderable renderable){
		int morphTargetsFlag = 0;
		VertexAttributes vertexAttributes = renderable.meshPart.mesh.getVertexAttributes();
		final int n = vertexAttributes.size();
		for (int i = 0; i < n; i++) {
			final VertexAttribute attr = vertexAttributes.get(i);
			if (attr.usage == PBRVertexAttributes.Usage.PositionTarget) morphTargetsFlag |= (1 << attr.unit);
			if (attr.usage == PBRVertexAttributes.Usage.NormalTarget) morphTargetsFlag |= (1 << (attr.unit + 8));
			if (attr.usage == PBRVertexAttributes.Usage.TangentTarget) morphTargetsFlag |= (1 << (attr.unit + 16));
		}
		return morphTargetsFlag;
	}
	
	private static long[] allTextureTypes = {
		PBRTextureAttribute.BaseColorTexture,
		PBRTextureAttribute.EmissiveTexture,
		PBRTextureAttribute.NormalTexture,
		PBRTextureAttribute.MetallicRoughnessTexture,
		PBRTextureAttribute.OcclusionTexture
	};

	private static long getTextureCoordinateMapMask(Attributes attributes){
		// encode texture coordinate unit in a 5 bits integer.
		// 5 texture types with 1 bits per texture type.
		// 0 means no texture or unit 0
		// 1 means unit 1
		// only 2 units are supported.
		long mask = 0L;
		int maskShift = 0;
		for(long textureType : allTextureTypes){
			PBRTextureAttribute attribute = attributes.get(PBRTextureAttribute.class, textureType);
			if(attribute != null){
				mask |= (attribute.uvIndex & 1) << maskShift;
			}
			maskShift++;
		}
		return mask;
	}

	@Override
	public void init(ShaderProgram program, Renderable renderable) {
		super.init(program, renderable);
		u_mipmapScale = program.fetchUniformLocation("u_mipmapScale", false);
		
		u_texCoord0Transform = program.fetchUniformLocation("u_texCoord0Transform", false);
		u_texCoord1Transform = program.fetchUniformLocation("u_texCoord1Transform", false);
		
		u_morphTargets1 = program.fetchUniformLocation("u_morphTargets1", false);
		u_morphTargets2 = program.fetchUniformLocation("u_morphTargets2", false);
		
		u_ambientLight = program.fetchUniformLocation("u_ambientLight", false);
	}
	
	@Override
	protected void bindMaterial(Attributes attributes) {
		super.bindMaterial(attributes);
		
		// TODO texCoords should be mapped in vertex shader to allow separated UV transform
		
		transformTexture[0] = null;
		transformTexture[1] = null;
		
		for(long textureType : allTextureTypes){
			PBRTextureAttribute attribute = attributes.get(PBRTextureAttribute.class, textureType);
			if(attribute != null){
				transformTexture[attribute.uvIndex] = attribute;
			}
		}
		
		if(u_texCoord0Transform >= 0){
			if(transformTexture[0] != null){
				PBRTextureAttribute attribute = transformTexture[0];
				textureTransform.idt();
				textureTransform.translate(attribute.offsetU, attribute.offsetV);
				textureTransform.rotateRad(-attribute.rotationUV);
				textureTransform.scale(attribute.scaleU, attribute.scaleV);
			}else{
				textureTransform.idt();
			}
			program.setUniformMatrix(u_texCoord0Transform, textureTransform);
		}
		if(u_texCoord1Transform >= 0){
			if(transformTexture[1] != null){
				PBRTextureAttribute attribute = transformTexture[1];
				textureTransform.setToTranslation(attribute.offsetU, attribute.offsetV);
				textureTransform.rotateRad(attribute.rotationUV);
				textureTransform.scale(attribute.scaleU, attribute.scaleV);
			}else{
				textureTransform.idt();
			}
			program.setUniformMatrix(u_texCoord1Transform, textureTransform);
		}
	}
	
	@Override
	public void render(Renderable renderable, Attributes combinedAttributes) {
		
		if(u_mipmapScale >= 0){
			PBRCubemapAttribute specularEnv = combinedAttributes.get(PBRCubemapAttribute.class, PBRCubemapAttribute.SpecularEnv);
			float mipmapFactor;
			if(specularEnv != null){
				mipmapFactor = (float)(Math.log(specularEnv.textureDescription.texture.getWidth()) / Math.log(2.0));
			}else{
				mipmapFactor = 1;
			}
			program.setUniformf(u_mipmapScale, mipmapFactor);
		}
		
		if(u_morphTargets1 >= 0){
			if(renderable.userData instanceof WeightVector){
				WeightVector weightVector = (WeightVector)renderable.userData;
				program.setUniformf(u_morphTargets1, weightVector.get(0), weightVector.get(1), weightVector.get(2), weightVector.get(3));
			}else{
				program.setUniformf(u_morphTargets1, 0, 0, 0, 0);
			}
		}
		if(u_morphTargets2 >= 0){
			if(renderable.userData instanceof WeightVector){
				WeightVector weightVector = (WeightVector)renderable.userData;
				program.setUniformf(u_morphTargets2, weightVector.get(4), weightVector.get(5), weightVector.get(6), weightVector.get(7));
			}else{
				program.setUniformf(u_morphTargets2, 0, 0, 0, 0);
			}
		}
		
		super.render(renderable, combinedAttributes);
	}
	
	@Override
	protected void bindLights(Renderable renderable, Attributes attributes) {
		
		// XXX update color (to apply intensity) before default binding
		DirectionalLightsAttribute dla = attributes.get(DirectionalLightsAttribute.class, DirectionalLightsAttribute.Type);
		if(dla != null){
			for(DirectionalLight light : dla.lights){
				if(light instanceof DirectionalLightEx){
					((DirectionalLightEx) light).updateColor();
				}
			}
		}
		
		super.bindLights(renderable, attributes);
			
		// XXX
		ColorAttribute ambiantLight = attributes.get(ColorAttribute.class, ColorAttribute.AmbientLight);
		if(ambiantLight != null){
			program.setUniformf(u_ambientLight, ambiantLight.color.r, ambiantLight.color.g, ambiantLight.color.b);
		}
	}
	
}
