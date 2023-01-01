package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;

import static net.mgsx.gltf.constant.CommonConstants.EOL;

public class PBREmissiveShaderProvider extends PBRShaderProvider {

  public PBREmissiveShaderProvider(PBRShaderConfig config) {
    super(config);
  }

  @Override
  protected Shader createShader(Renderable renderable) {
    PBRShaderConfig config = (PBRShaderConfig) this.config;

    // if material has some alpha settings, emissive is impacted and albedo is required.
    boolean hasAlpha = renderable.material.has(BlendingAttribute.Type) || renderable.material.has(FloatAttribute.AlphaTest);

    StringBuilder prefix = new StringBuilder(createPrefixBase(renderable, config));

    prefix.append(morphTargetsPrefix(renderable));

    prefix.append(createPrefixSRGB(renderable, config));

    // optional base color factor
    if (renderable.material.has(PBRColorAttribute.BaseColorFactor)) {
      prefix.append("#define baseColorFactorFlag").append(EOL);
    }

    int maxUVIndex = 0;

    TextureAttribute emissiveAttribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Emissive);
    if (emissiveAttribute != null) {
      prefix.append("#define v_emissiveUV v_texCoord").append(emissiveAttribute.uvIndex).append(EOL);
      maxUVIndex = Math.max(maxUVIndex, emissiveAttribute.uvIndex);
    }

    if (hasAlpha) {
      TextureAttribute diffuseAttribute = renderable.material.get(TextureAttribute.class, TextureAttribute.Diffuse);
      if (diffuseAttribute != null) {
        prefix.append("#define v_diffuseUV v_texCoord").append(diffuseAttribute.uvIndex).append(EOL);
        maxUVIndex = Math.max(maxUVIndex, diffuseAttribute.uvIndex);
      }
    }

    if (maxUVIndex >= 0) {
      prefix.append("#define textureFlag").append(EOL);
    }
    if (maxUVIndex == 1) {
      prefix.append("#define textureCoord1Flag").append(EOL);
    } else if (maxUVIndex > 1) {
      throw new GdxRuntimeException("more than 2 texture coordinates attribute not supported");
    }

    PBRShader shader = new PBRShader(renderable, config, prefix.toString());
    checkShaderCompilation(shader.program);

    // prevent infinite loop
    if (!shader.canRender(renderable)) {
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
