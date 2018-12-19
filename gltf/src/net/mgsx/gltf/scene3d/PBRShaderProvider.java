package net.mgsx.gltf.scene3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class PBRShaderProvider extends DefaultShaderProvider
{
	public static PBRShaderProvider createDefault(int maxBones){
		String mode = "gdx-pbr";
		Config config = new Config(
				Gdx.files.classpath("net/mgsx/gltf/shaders/" + mode + ".vs").readString(), 
				Gdx.files.classpath("net/mgsx/gltf/shaders/" + mode + ".fs").readString());
		
		config.numBones = maxBones;
		
		return new PBRShaderProvider(config);
	}
	
	// TODO move to environnement variable
	private Texture brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/assets/brdfLUT.png"));
	
	public PBRShaderProvider(Config config) {
		super(config);
	}
	
	protected Shader createShader(Renderable renderable) {
		
		// TODO use extended config for all options ... 
		
		String prefix = DefaultShader.createPrefix(renderable, config);
		
		boolean hasMorphTargets = false;
		for(VertexAttribute att : renderable.meshPart.mesh.getVertexAttributes()){
			for(int i=0 ; i<8 ; i++){
				if(att.alias.equals(ShaderProgram.POSITION_ATTRIBUTE + i)){
					prefix += "#define " + "position" + i + "Flag\n";
					hasMorphTargets = true;
				}else if(att.alias.equals(ShaderProgram.NORMAL_ATTRIBUTE + i)){
					prefix += "#define " + "normal" + i + "Flag\n";
					hasMorphTargets = true;
				}else if(att.alias.equals(ShaderProgram.TANGENT_ATTRIBUTE + i)){
					prefix += "#define " + "tangent" + i + "Flag\n";
					hasMorphTargets = true;
				}
			}
		}
		if(hasMorphTargets){
			// TODO should be automatic in shader ?
			prefix += "#define morphTargetsFlag\n";
		}
		
		// XXX force camera position required for the shader
		prefix += "#define cameraPositionFlag\n";
		
		if(renderable.material.has(PBRTextureAttribute.MetallicRoughnessTexture)){
			prefix += "#define metallicRoughnessTextureFlag\n";
		}
		if(renderable.material.has(PBRTextureAttribute.OcclusionTexture)){
			prefix += "#define occlusionTextureFlag\n";
		}
		
		// TODO option !
		prefix += "#define USE_IBL\n";
		
		// TODO option !
		prefix += "#define USE_TEX_LOD\n";
		
		// TODO option
		prefix += "#define MANUAL_SRGB\n";
		// prefix += "#define SRGB_FAST_APPROXIMATION\n";
		
		if(renderable.environment.has(PBRCubemapAttribute.SpecularEnv)){
			prefix += "#define diffuseSpecularEnvSeparateFlag\n";
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
			throw new GdxRuntimeException("multi UVs > 1 not supported"); // TODO maybe just ignored ?
		}
		
		return new PBRShader(renderable, config, prefix, brdfLUT);
	};
}
