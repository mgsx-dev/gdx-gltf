package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

public class SceneModel implements Disposable
{
	public String name;
	public Model model;
	public ObjectMap<Node, Camera> cameras = new ObjectMap<Node, Camera>();
	public ObjectMap<Node, BaseLight> lights = new ObjectMap<Node, BaseLight>();
	
	@Override
	public void dispose() {
		model.dispose();
	}
}
