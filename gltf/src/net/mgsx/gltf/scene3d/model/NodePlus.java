package net.mgsx.gltf.scene3d.model;

import com.badlogic.gdx.graphics.g3d.model.Node;

public class NodePlus extends Node
{
	public WeightVector weights;
	
	@Override
	public Node copy() {
		return new NodePlus().set(this);
	}
	
	@Override
	protected Node set(Node other) 
	{
		if(other instanceof NodePlus){
			if(((NodePlus)other).weights != null){
				weights = new WeightVector().set(((NodePlus)other).weights);
			}
		}
		return super.set(other);
	}
}
