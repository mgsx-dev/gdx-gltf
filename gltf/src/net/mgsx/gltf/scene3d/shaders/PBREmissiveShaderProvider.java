package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

public class PBREmissiveShaderProvider extends PBRShaderProvider
{
	
	public PBREmissiveShaderProvider(PBRShaderConfig config) {
		super(config);
	}

	@Override
	protected Shader createShader(Renderable renderable) {
		
		PBRShaderConfig config = (PBRShaderConfig)this.config;
		
		// if material has some alpha settings, emissive is impacted and albedo is required.
		boolean hasAlpha = renderable.material.has(BlendingAttribute.Type) || renderable.material.has(FloatAttribute.AlphaTest);
		
		String prefix = createPrefixBase(renderable, config);
		
		prefix += morphTargetsPrefix(renderable);
		
		prefix += createPrefixSRGB(renderable, config);
		
		// optional base color factor
		if(renderable.material.has(PBRColorAttribute.BaseColorFactor)){
			prefix += "#define baseColorFactorFlag\n";
		}
		
		int maxUVIndex = 0;
		{
			TextureAttribute attribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Emissive);
			if(attribute != null){
				prefix += "#define v_emissiveUV v_texCoord" + attribute.uvIndex + "\n";
				maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex);
			}
		}
		if(hasAlpha){
			TextureAttribute attribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Diffuse);
			if(attribute != null){
				prefix += "#define v_diffuseUV v_texCoord" + attribute.uvIndex + "\n";
				maxUVIndex = Math.max(maxUVIndex, attribute.uvIndex);
			}
		}
		
		
		if(maxUVIndex >= 0){
			prefix += "#define textureFlag\n";
		}
		if(maxUVIndex == 1){
			prefix += "#define textureCoord1Flag\n";
		}else if(maxUVIndex > 1){
			throw new GdxRuntimeException("more than 2 texture coordinates attribute not supported");
		}
		
		PBRShader shader = new PBRShader(renderable, config, prefix);
		checkShaderCompilation(shader.program);
		
		// prevent infinite loop
		if(!shader.canRender(renderable)){
			throw new GdxRuntimeException("cannot render with this shader");
		}
		
		return shader;
	}

	public static PBRShaderConfig createConfig(int maxBones) {
		PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
		config.numBones = maxBones;
		config.vertexShader = Gdx.files.classpath("net/mgsx/gltf/shaders/gdx-pbr.vs.glsl").readString();
		config.fragmentShader = Gdx.files.classpath("net/mgsx/gltf/shaders/emissive-only.fs.glsl").readString();
		return config;
	}
}
