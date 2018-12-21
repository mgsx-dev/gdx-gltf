package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * gdx view of an asset file : Model, Camera (as template), lights (as template), textures
 */
public class SceneAsset implements Disposable 
{
	public Array<Model> scenes = new Array<Model>();
	public Model scene;

	public final Array<Camera> cameras = new Array<Camera>();
	
	/** node name to camera index */
	public ObjectMap<String, Integer> cameraMap = new ObjectMap<String, Integer>();
	
	public Array<Animation> animations = new Array<Animation>();
	public int maxBones;
	
	public final Array<Texture> textures = new Array<Texture>();
	
	public Camera createCamera(String name) 
	{
		return createCamera(cameraMap.get(name));
	}
	public Camera createCamera(int index) 
	{
		Camera cameraModel = cameras.get(index);
		// TODO instanciate by recopy
		return cameraModel;
	}

	@Override
	public void dispose() {
		for(Model scene : scenes){
			scene.dispose();
		}
		for(Texture texture : textures){
			texture.dispose();
		}
	}
	
}
