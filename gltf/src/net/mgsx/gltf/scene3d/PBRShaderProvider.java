package net.mgsx.gltf.scene3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gltf.scene3d.PBRShaderConfig.SRGB;

public class PBRShaderProvider extends DefaultShaderProvider
{
	public static PBRShaderProvider createDefault(int maxBones){
		PBRShaderConfig config = new PBRShaderConfig();
		config.numBones = maxBones;
		return new PBRShaderProvider(config);
	}
	
	public static PBRShaderProvider createDefault(PBRShaderConfig config){
		String mode = "gdx-pbr";
		config.vertexShader = Gdx.files.classpath("net/mgsx/gltf/shaders/" + mode + ".vs").readString();
		config.fragmentShader = Gdx.files.classpath("net/mgsx/gltf/shaders/" + mode + ".fs").readString();
		
		return new PBRShaderProvider(config);
	}
	
	private PBRShaderProvider(PBRShaderConfig config) {
		super(config);
	}
	
	protected Shader createShader(Renderable renderable) {
		
		PBRShaderConfig config = (PBRShaderConfig)this.config;
		
		String prefix = DefaultShader.createPrefix(renderable, config);
		
		for(VertexAttribute att : renderable.meshPart.mesh.getVertexAttributes()){
			for(int i=0 ; i<8 ; i++){
				if(att.alias.equals(ShaderProgram.POSITION_ATTRIBUTE + i)){
					prefix += "#define " + "position" + i + "Flag\n";
				}else if(att.alias.equals(ShaderProgram.NORMAL_ATTRIBUTE + i)){
					prefix += "#define " + "normal" + i + "Flag\n";
				}else if(att.alias.equals(ShaderProgram.TANGENT_ATTRIBUTE + i)){
					prefix += "#define " + "tangent" + i + "Flag\n";
				}
			}
		}
		
		if(renderable.material.has(PBRTextureAttribute.MetallicRoughnessTexture)){
			prefix += "#define metallicRoughnessTextureFlag\n";
		}
		if(renderable.material.has(PBRTextureAttribute.OcclusionTexture)){
			prefix += "#define occlusionTextureFlag\n";
		}
		
		// IBL options
		PBRCubemapAttribute specualarCubemapAttribute = null;
		if(renderable.environment.has(PBRCubemapAttribute.SpecularEnv)){
			prefix += "#define diffuseSpecularEnvSeparateFlag\n";
			specualarCubemapAttribute = renderable.environment.get(PBRCubemapAttribute.class, PBRCubemapAttribute.SpecularEnv);
		}else if(renderable.environment.has(PBRCubemapAttribute.DiffuseEnv)){
			specualarCubemapAttribute = renderable.environment.get(PBRCubemapAttribute.class, PBRCubemapAttribute.DiffuseEnv);
		}else if(renderable.environment.has(PBRCubemapAttribute.EnvironmentMap)){
			specualarCubemapAttribute = renderable.environment.get(PBRCubemapAttribute.class, PBRCubemapAttribute.EnvironmentMap);
		}
		if(specualarCubemapAttribute != null){
			prefix += "#define USE_IBL\n";
			
			TextureFilter textureFilter = specualarCubemapAttribute.textureDescription.minFilter != null ? specualarCubemapAttribute.textureDescription.minFilter : specualarCubemapAttribute.textureDescription.texture.getMinFilter();
			if(textureFilter.equals(TextureFilter.MipMap)){
				prefix += "#define USE_TEX_LOD\n";
			}
			
			if(renderable.environment.has(PBRTextureAttribute.BRDFLUTTexture)){
				prefix += "#define brdfLUTTexture\n";
			}
		}
		
		if(renderable.environment.has(ColorAttribute.AmbientLight)){
			prefix += "#define ambientLightFlag\n";
		}
		
		// SRGB
		if(config.manualSRGB != SRGB.NONE){
			prefix += "#define MANUAL_SRGB\n";
			if(config.manualSRGB == SRGB.FAST){
				prefix += "#define SRGB_FAST_APPROXIMATION\n";
			}
		}
		
		// DEBUG
		if(config.debug){
			prefix += "#define DEBUG\n";
		}
		
		// multi UVs
		int maxUVIndex = 0;
		
		{
			TextureAttribute attribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Diffuse);
			if(attribute != null){
				prefix += "#define v_diffuseUV v_texCoord" + attribute.uvIndex + "\n";
				maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex);
			}
		}
		{
			TextureAttribute attribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Emissive);
			if(attribute != null){
				prefix += "#define v_emissiveUV v_texCoord" + attribute.uvIndex + "\n";
				maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex);
			}
		}
		{
			TextureAttribute attribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Normal);
			if(attribute != null){
				prefix += "#define v_normalUV v_texCoord" + attribute.uvIndex + "\n";
				maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex);
			}
		}
		{
			TextureAttribute attribute = renderable.material.get(TextureAttribute.class, PBRTextureAttribute.MetallicRoughnessTexture);
			if(attribute != null){
				prefix += "#define v_metallicRoughnessUV v_texCoord" + attribute.uvIndex + "\n";
				maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex);
			}
		}
		{
			TextureAttribute attribute = renderable.material.get(TextureAttribute.class, PBRTextureAttribute.OcclusionTexture);
			if(attribute != null){
				prefix += "#define v_occlusionUV v_texCoord" + attribute.uvIndex + "\n";
				maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex);
			}
		}
		
		if(maxUVIndex == 1){
			prefix += "#define textureCoord1Flag\n";
		}else if(maxUVIndex > 1){
			throw new GdxRuntimeException("multi UVs > 1 not supported");
		}
		
		return new PBRShader(renderable, config, prefix);
	};
}
