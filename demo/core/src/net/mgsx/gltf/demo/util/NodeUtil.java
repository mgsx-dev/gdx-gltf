package net.mgsx.gltf.demo.util;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.utils.Array;

public class NodeUtil {

	public static Array<Node> getAllNodes(Array<Node> result, ModelInstance modelInstance){
		return getAllNodes(result, modelInstance.nodes);
	}

	public static Array<Node> getAllNodes(Array<Node> result, Array<Node> nodes) {
		for(Node node : nodes){
			getAllNodes(result, node);
		}
		return result;
	}

	public static Array<Node> getAllNodes(Array<Node> result, Node node) {
		result.add(node);
		for(int i=0 ; i<node.getChildCount() ; i++){
			getAllNodes(result, node.getChild(i));
		}
		return result;
	}

	public static Array<Node> getAllNodes(Array<Node> result, Model model) {
		return getAllNodes(result, model.nodes);
	}
}
