package net.mgsx.gltf.scene3d.model;

import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.utils.Array;

/**
 * {@link Node} hack to store morph targets weights 
 */
public class NodePlus extends Node
{
	/**
	 * null if no morph targets
	 */
	public WeightVector weights;
	
	/** 
	 * optionnal morph target names (eg. exported from Blender with custom properties enabled). 
	 * shared with others nodes with same mesh.
	 */
	public Array<String> morphTargetNames;
	
	@Override
	public Node copy() {
		return new NodePlus().set(this);
	}
	
	@Override
	protected Node set(Node other) 
	{
		if(other instanceof NodePlus){
			if(((NodePlus)other).weights != null){
				weights = ((NodePlus)other).weights.cpy();
				morphTargetNames = ((NodePlus)other).morphTargetNames;
			}
		}
		return super.set(other);
	}
}
