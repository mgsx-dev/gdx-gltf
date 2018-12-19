package net.mgsx.gltf.scene3d;

import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.utils.Array;

public class NodeAnimationPlus extends NodeAnimation
{
	public Array<NodeKeyframe<WeightVector>> weights = null;

	public NodeAnimationPlus set(NodeAnimation other) 
	{
		node = other.node;
		translation = other.translation;
		scaling = other.scaling;
		if(other instanceof NodeAnimationPlus){
			weights = ((NodeAnimationPlus)other).weights;
		}
		return this;
	}
}
