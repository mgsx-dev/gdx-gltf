package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * gdx view of an asset file : Model, Camera (as template), lights (as template), textures
 */
public class SceneAsset implements Disposable 
{
	public Array<SceneModel> scenes = new Array<SceneModel>();
	public SceneModel scene;

	public Array<Animation> animations = new Array<Animation>();
	public int maxBones;
	
	public final Array<Texture> textures = new Array<Texture>();
	
	@Override
	public void dispose() {
		for(SceneModel scene : scenes){
			scene.dispose();
		}
		for(Texture texture : textures){
			texture.dispose();
		}
	}
}
