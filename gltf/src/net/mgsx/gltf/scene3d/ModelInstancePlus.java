package net.mgsx.gltf.scene3d;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;

public class ModelInstancePlus extends ModelInstance
{

	public ModelInstancePlus(Model model) {
		super(model);
	}
	
	@Override
	public Renderable getRenderable (final Renderable out, final Node node, final NodePart nodePart) {
		super.getRenderable(out, node, nodePart);
		if(nodePart instanceof NodePartPlus){
			out.userData = ((NodePartPlus) nodePart).morphTargets;
		}
		return out;
	}
	
	
	
}
