package net.mgsx.gltf.scene3d.model;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.NodePart;

public class NodePartPlus extends NodePart{
	
	public WeightVector morphTargets;

	public Renderable setRenderable (final Renderable out) {
		out.material = material;
		out.meshPart.set(meshPart);
		out.bones = bones;
		out.userData = morphTargets;
		return out;
	}
	
	@Override
	public NodePart copy() {
		return new NodePartPlus().set(this);
	}
	
	@Override
	protected NodePart set(NodePart other) {
		super.set(other);
		if(other instanceof NodePartPlus && ((NodePartPlus) other).morphTargets != null){
			morphTargets = ((NodePartPlus) other).morphTargets; //.cpy(); 
			// XXX
		}
		return this;
	}
}
