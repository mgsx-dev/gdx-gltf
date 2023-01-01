package net.mgsx.gltf.exporters;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;

public class ExportOBJTest extends Game {

	public static void main(String[] args) {
		new LwjglApplication(new ExportOBJTest());
	}

	@Override
	public void create() {
		Model model = new ObjLoader().loadModel(Gdx.files.classpath("nominal/obj/textured-cube.obj"));
		new GLTFExporter().export(model, Gdx.files.absolute("/tmp/ExportOBJTest.gltf"));
		Gdx.app.exit();
	}
}
