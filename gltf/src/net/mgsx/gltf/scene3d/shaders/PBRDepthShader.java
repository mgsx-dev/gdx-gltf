package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;

import net.mgsx.gltf.scene3d.attributes.PBRVertexAttributes;
import net.mgsx.gltf.scene3d.model.WeightVector;

public class PBRDepthShader extends DepthShader
{
	public final long morphTargetsMask;
	
	// morph targets
	private int u_morphTargets1;
	private int u_morphTargets2;
	
	public static long computeMorphTargetsMask(Renderable renderable){
		int morphTargetsFlag = 0;
		VertexAttributes vertexAttributes = renderable.meshPart.mesh.getVertexAttributes();
		final int n = vertexAttributes.size();
		for (int i = 0; i < n; i++) {
			final VertexAttribute attr = vertexAttributes.get(i);
			if (attr.usage == PBRVertexAttributes.Usage.PositionTarget) morphTargetsFlag |= (1 << attr.unit);
		}
		return morphTargetsFlag;
	}
	
	public PBRDepthShader(Renderable renderable, Config config, String prefix) {
		super(renderable, config, prefix);
		
		this.morphTargetsMask = computeMorphTargetsMask(renderable);
	}

	@Override
	public boolean canRender(Renderable renderable) {
		
		if(this.morphTargetsMask != computeMorphTargetsMask(renderable)) return false;
		
		return super.canRender(renderable);
	}
	
	@Override
	public void init() {
		super.init();
		
		u_morphTargets1 = program.fetchUniformLocation("u_morphTargets1", false);
		u_morphTargets2 = program.fetchUniformLocation("u_morphTargets2", false);

	}
	
	@Override
	public void render(Renderable renderable, Attributes combinedAttributes) {
		
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

}
