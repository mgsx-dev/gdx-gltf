package net.mgsx.gltf.loaders.shared.scene;

import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.utils.ObjectMap;

public class NodeResolver {

  ObjectMap<Integer, Node> nodeMap = new ObjectMap<>();

  public Node get(int index) {
    return nodeMap.get(index);
  }

  public void put(int index, Node node) {
    nodeMap.put(index, node);
  }
}
