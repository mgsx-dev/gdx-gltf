package net.mgsx.gltf.demo.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader.Config;

import net.mgsx.gltf.scene3d.attributes.PBRVertexAttributes;
import net.mgsx.gltf.scene3d.shaders.PBRCommon;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;

public class OutlineShaderProvider extends PBRDepthShaderProvider
{

	public OutlineShaderProvider(int numBones) {
		super(createConfig(numBones));
	}

	private static Config createConfig(int numBones) {
		Config config = new Config();
		config.defaultCullFace = GL20.GL_BACK; // TODO back or front ?
		config.numBones = numBones;
		config.vertexShader = Gdx.files.classpath("net/mgsx/gltf/demo/shaders/gltf-outline-ext.vs.glsl").readString();
		config.fragmentShader = Gdx.files.classpath("net/mgsx/gltf/demo/shaders/gltf-outline-ext.fs.glsl").readString();
		return config;
	}
	
	@Override
	protected Shader createShader(Renderable renderable) {
		
		// TODO only count used attributes, depth shader only require a few of them.
		PBRCommon.checkVertexAttributes(renderable);
		
		return new OutlineShader(renderable, config, DepthShader.createPrefix(renderable, config) + morphTargetsPrefix(renderable));
	}
	
	@Override
	protected String morphTargetsPrefix(Renderable renderable){
		String prefix = "";
		// TODO optimize double loop
		for(VertexAttribute att : renderable.meshPart.mesh.getVertexAttributes()){
			for(int i=0 ; i<PBRCommon.MAX_MORPH_TARGETS ; i++){
				if(att.usage == PBRVertexAttributes.Usage.PositionTarget && att.unit == i){
					prefix += "#define " + "position" + i + "Flag\n";
				}else if(att.usage == PBRVertexAttributes.Usage.NormalTarget && att.unit == i){
					prefix += "#define " + "normal" + i + "Flag\n";
				}
			}
		}
		return prefix;
	}
}
