package net.mgsx.gltf.demo;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopBatchTestLauncher {
	public static void main (String[] arg) {
		
		// required for HTTPS requests
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(1024, 768);
		new Lwjgl3Application(arg.length > 0 ? new GLTFTest(arg[0]) : new GLTFTest(), config);
	}
}
