package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;

import net.mgsx.gltf.scene3d.attributes.PBRVertexAttributes;

public class PBRDepthShaderProvider extends DepthShaderProvider
{
	
	public PBRDepthShaderProvider(Config config) {
		super(config);
	}

	protected String morphTargetsPrefix(Renderable renderable){
		String prefix = "";
		for(VertexAttribute att : renderable.meshPart.mesh.getVertexAttributes()){
			for(int i=0 ; i<PBRCommon.MAX_MORPH_TARGETS ; i++){
				if(att.usage == PBRVertexAttributes.Usage.PositionTarget && att.unit == i){
					prefix += "#define " + "position" + i + "Flag\n";
				}
			}
		}
		return prefix;
	}
	
	@Override
	protected Shader createShader(Renderable renderable) {
		
		// TODO only count used attributes, depth shader only require a few of them.
		PBRCommon.checkVertexAttributes(renderable);
		
		return new PBRDepthShader(renderable, config, DepthShader.createPrefix(renderable, config) + morphTargetsPrefix(renderable));
	}
}
