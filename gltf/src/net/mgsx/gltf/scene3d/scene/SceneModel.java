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
	public ObjectMap<Camera, Node> cameras = new ObjectMap<Camera, Node>();
	public ObjectMap<BaseLight, Node> lights = new ObjectMap<BaseLight, Node>();
	
	@Override
	public void dispose() {
		model.dispose();
	}
}
