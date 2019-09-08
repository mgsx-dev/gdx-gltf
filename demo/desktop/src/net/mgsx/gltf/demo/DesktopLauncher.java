package net.mgsx.gltf.demo;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import net.mgsx.gltf.demo.ui.GLTFDemoUI;

public class DesktopLauncher {
	public static void main (String[] arg) {
		
		// required for HTTPS requests
		System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1024;
		config.height = 768;
		
		GLTFDemo.AUTOLOAD_ENTRY = arg.length > 1 ? arg[1] : "BoomBox";
		GLTFDemo.AUTOLOAD_VARIANT = arg.length > 2 ? arg[2] : "glTF-Binary";
		GLTFDemo.alternateMaps = arg.length > 3 ? arg[3] : null;
		
		GLTFDemoUI.fileSelector = new AWTFileSelector();
		
		new LwjglApplication(arg.length > 0 ? new GLTFDemo(arg[0]) : new GLTFDemo(), config);
	}
}
