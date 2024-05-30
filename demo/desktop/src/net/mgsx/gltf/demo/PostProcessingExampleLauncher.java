package net.mgsx.gltf.demo;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import net.mgsx.gltf.examples.GLTFPostProcessingExample;

public class PostProcessingExampleLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		new Lwjgl3Application(new GLTFPostProcessingExample(), config);
	}
}
