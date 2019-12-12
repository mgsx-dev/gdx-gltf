package net.mgsx.gltf.demo.shaders;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;

import net.mgsx.gltf.scene3d.attributes.PBRVertexAttributes;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShader;

public class OutlineShader extends PBRDepthShader {


	// XXX should be per material ?
	public static final Color extrusionColor = new Color(Color.BLACK);
	public static float extrusionRate = 1f; 
	
	private int u_extrusion, u_outlineColor;

	public OutlineShader(Renderable renderable, Config config, String prefix) {
		super(renderable, config, prefix);
	}
	
	@Override
	protected long computeMorphTargetsMask(Renderable renderable) {
		int morphTargetsFlag = 0;
		VertexAttributes vertexAttributes = renderable.meshPart.mesh.getVertexAttributes();
		final int n = vertexAttributes.size();
		for (int i = 0; i < n; i++) {
			final VertexAttribute attr = vertexAttributes.get(i);
			if (attr.usage == PBRVertexAttributes.Usage.PositionTarget) morphTargetsFlag |= (1 << attr.unit);
			if (attr.usage == PBRVertexAttributes.Usage.NormalTarget) morphTargetsFlag |= (1 << (attr.unit + 8));
		}
		return morphTargetsFlag;
	}
	
	@Override
	public void init() {
		super.init();
		
		u_extrusion = program.fetchUniformLocation("u_extrusion", false);
		u_outlineColor = program.fetchUniformLocation("u_outlineColor", false);
	}
	
	@Override
	public void render(Renderable renderable, Attributes combinedAttributes) {
		
		if(u_extrusion >= 0){
			program.setUniformf(u_extrusion, extrusionRate);
		}
		if(u_outlineColor >= 0){
			program.setUniformf(u_outlineColor, extrusionColor);
		}
		
		super.render(renderable, combinedAttributes);
	}
	
	@Override
	protected void bindMaterial(Attributes attributes) {
		super.bindMaterial(attributes);
		context.setDepthMask(true);
		context.setCullFace(GL20.GL_FRONT);
		context.setDepthTest(GL20.GL_LESS);
	}
	

}
