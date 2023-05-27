package net.mgsx.gltf.loaders;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class ImportGLTFTest extends Game {

	public static void main(String[] args) {
		if(args.length < 1) throw new GdxRuntimeException("program argument required");
		if(args.length > 1) throw new GdxRuntimeException("too many program arguments");
		new LwjglApplication(new ImportGLTFTest(args[0]));
	}

	private final String path;
	
	public ImportGLTFTest(String path) {
		this.path = path;
	}

	@Override
	public void create() {
		FileHandle file = path.startsWith("/") ? Gdx.files.absolute(path) : Gdx.files.classpath(path);
		if(!file.exists()) throw new GdxRuntimeException("file not found: " + path);
		
		SceneAsset asset;
		if(file.extension().equals("gltf")){
			asset = new GLTFLoader().load(file);
		}else if(file.extension().equals("glb")){
			asset = new GLBLoader().load(file);
		}else{
			throw new GdxRuntimeException("extension not supported: " + file.extension());
		}
		Gdx.app.log("ImportGLTFTest", "done");
		
		// try to get the pixmap of a texture
		for(Material m : asset.scene.model.materials){
			PBRTextureAttribute a = m.get(PBRTextureAttribute.class, PBRTextureAttribute.BaseColorTexture);
			if(a != null){
				TextureData d = a.textureDescription.texture.getTextureData();
				if(!d.isPrepared()) d.prepare();
				Pixmap pixmap = d.consumePixmap();
				Gdx.app.log("ImportGLTFTest", "pixmap ok : " + pixmap.getWidth() + "x" + pixmap.getHeight() + " color at (0,0): " + pixmap.getPixel(0, 0));
				if(d.disposePixmap()){
					pixmap.dispose();
				}
			}
		}
		
		Gdx.app.exit();
	}
}
