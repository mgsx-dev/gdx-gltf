package net.mgsx.gltf.loaders;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Texture;

import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class SharedTextureTest extends Game {

	public static void main(String[] args) {
		new LwjglApplication(new SharedTextureTest());
	}
	
	@Override
	public void create() {
		AssetManager manager = new AssetManager();
		manager.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader());
		
		manager.load("sharing/cube.gltf", SceneAsset.class);
		manager.load("sharing/sphere.gltf", SceneAsset.class);
		
		manager.finishLoading();

		Texture texture = manager.get("sharing/shared-texture.png", Texture.class);
		
		Gdx.app.log("SharedTextureTest", "texture reference count (initial): " + manager.getReferenceCount("sharing/shared-texture.png"));
		Gdx.app.log("SharedTextureTest", "texture handle: " + texture.getTextureObjectHandle());
		
		manager.unload("sharing/cube.gltf");
		
		
		Gdx.app.log("SharedTextureTest", "texture reference count (after unload 1): " + manager.getReferenceCount("sharing/shared-texture.png"));
		Gdx.app.log("SharedTextureTest", "texture handle: " + texture.getTextureObjectHandle());
	
		manager.unload("sharing/sphere.gltf");
		
		Gdx.app.log("SharedTextureTest", "texture still in asset manager (after unload 2): " + manager.contains("sharing/shared-texture.png", Texture.class));
		Gdx.app.log("SharedTextureTest", "texture handle: " + texture.getTextureObjectHandle());
	
		Gdx.app.log("SharedTextureTest", "done");
		Gdx.app.exit();
	}
}
