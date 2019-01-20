package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
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

	// morph targets
	private int u_morphTargets1;
	private int u_morphTargets2;
	
	// debug uniforms
	private int u_ScaleDiffBaseMR;
	private int u_ScaleFGDSpec;
	private int u_ScaleIBLAmbient;

	private int u_mipmapScale;

	private int u_texCoord0Transform;
	private int u_texCoord1Transform;

	private int u_ambientLight;
	
	private static final Matrix3 textureTransform = new Matrix3();
	
	public static final Color ScaleDiffBaseMR = new Color();
	public static final Color ScaleFGDSpec = new Color();
	public static final Color ScaleIBLAmbient = new Color(.5f, .5f, 0, 0);

	public PBRShader(Renderable renderable, Config config, String prefix) {
		super(renderable, config, prefix);
		
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
		
	}

	@Override
	public boolean canRender(Renderable renderable) {
		// TODO properly determine if current shader can render this renderable.
		return super.canRender(renderable);
	}


	@Override
	public void init(ShaderProgram program, Renderable renderable) {
		super.init(program, renderable);
		u_ScaleDiffBaseMR = program.fetchUniformLocation("u_ScaleDiffBaseMR", false);
		u_ScaleFGDSpec = program.fetchUniformLocation("u_ScaleFGDSpec", false);
		u_ScaleIBLAmbient = program.fetchUniformLocation("u_ScaleIBLAmbient", false);
		u_mipmapScale = program.fetchUniformLocation("u_mipmapScale", false);
		
		u_texCoord0Transform = program.fetchUniformLocation("u_texCoord0Transform", false);
		u_texCoord1Transform = program.fetchUniformLocation("u_texCoord1Transform", false);
		
		u_morphTargets1 = program.fetchUniformLocation("u_morphTargets1", false);
		u_morphTargets2 = program.fetchUniformLocation("u_morphTargets2", false);
		
		u_ambientLight = program.fetchUniformLocation("u_ambientLight", false);
	}
	
	@Override
	public void begin(Camera camera, RenderContext context) {
		super.begin(camera, context);
		
		program.setUniformf(u_ScaleDiffBaseMR, ScaleDiffBaseMR);
		program.setUniformf(u_ScaleFGDSpec, ScaleFGDSpec);
		if(u_ScaleIBLAmbient >= 0){
			program.setUniformf(u_ScaleIBLAmbient, ScaleIBLAmbient);
		}
	}
	
	@Override
	protected void bindMaterial(Attributes attributes) {
		super.bindMaterial(attributes);
		
		// TODO texCoords should be mapped in vertex shader to allow separated UV transform
		
		transformTexture[0] = null;
		transformTexture[1] = null;
		
		{
			PBRTextureAttribute attribute = attributes.get(PBRTextureAttribute.class, PBRTextureAttribute.BaseColorTexture);
			if(attribute != null){
				transformTexture[attribute.uvIndex] = attribute;
			}
		}
		{
			PBRTextureAttribute attribute = attributes.get(PBRTextureAttribute.class, PBRTextureAttribute.EmissiveTexture);
			if(attribute != null){
				transformTexture[attribute.uvIndex] = attribute;
			}
		}
		{
			PBRTextureAttribute attribute = attributes.get(PBRTextureAttribute.class, PBRTextureAttribute.NormalTexture);
			if(attribute != null){
				transformTexture[attribute.uvIndex] = attribute;
			}
		}
		{
			PBRTextureAttribute attribute = attributes.get(PBRTextureAttribute.class, PBRTextureAttribute.MetallicRoughnessTexture);
			if(attribute != null){
				transformTexture[attribute.uvIndex] = attribute;
			}
		}
		{
			PBRTextureAttribute attribute = attributes.get(PBRTextureAttribute.class, PBRTextureAttribute.OcclusionTexture);
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
				mipmapFactor = (float)Math.sqrt(specularEnv.textureDescription.texture.getWidth());
			}else{
				mipmapFactor = 1;
			}
			program.setUniformf(u_mipmapScale, mipmapFactor);
		}
		
		if(u_morphTargets1 >= 0){
			if(renderable.userData instanceof WeightVector){
				WeightVector weightVector = (WeightVector)renderable.userData;
				program.setUniformf(u_morphTargets1, weightVector.get(0), weightVector.get(1), weightVector.get(2), weightVector.get(3));
			}
		}
		if(u_morphTargets2 >= 0){
			if(renderable.userData instanceof WeightVector){
				WeightVector weightVector = (WeightVector)renderable.userData;
				program.setUniformf(u_morphTargets2, weightVector.get(4), weightVector.get(5), weightVector.get(6), weightVector.get(7));
			}
		}
		
		super.render(renderable, combinedAttributes);
	}
	
	@Override
	protected void bindLights(Renderable renderable, Attributes attributes) {
		super.bindLights(renderable, attributes);
		
		// XXX
		ColorAttribute ambiantLight = attributes.get(ColorAttribute.class, ColorAttribute.AmbientLight);
		if(ambiantLight != null){
			program.setUniformf(u_ambientLight, ambiantLight.color.r, ambiantLight.color.g, ambiantLight.color.b);
		}
	}
	
}
