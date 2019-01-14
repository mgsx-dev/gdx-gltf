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
	public Array<SceneModel> scenes;
	public SceneModel scene;

	public Array<Animation> animations;
	public int maxBones;
	
	public Array<Texture> textures;
	
	@Override
	public void dispose() {
		if(scenes != null){
			for(SceneModel scene : scenes){
				scene.dispose();
			}
		}
		if(textures != null){
			for(Texture texture : textures){
				texture.dispose();
			}
		}
	}
}
