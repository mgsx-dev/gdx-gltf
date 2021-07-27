package net.mgsx.gltf.loaders;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;

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
		if(file.extension().equals("gltf")){
			new GLTFLoader().load(file);
		}else if(file.extension().equals("glb")){
			new GLBLoader().load(file);
		}else{
			throw new GdxRuntimeException("extension not supported: " + file.extension());
		}
		Gdx.app.log("ImportGLTFTest", "done");
		Gdx.app.exit();
	}
}
