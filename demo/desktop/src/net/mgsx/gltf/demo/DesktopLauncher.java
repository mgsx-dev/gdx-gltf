package net.mgsx.gltf.demo;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import net.mgsx.gltf.demo.ui.GLTFDemoUI;

public class DesktopLauncher {
	public static void main (String[] arg) {
		
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		DisplayMode display = Lwjgl3ApplicationConfiguration.getDisplayMode();
		if(display.height >= 1440){
			config.setWindowedMode(1920, 1080);
			GLTFDemo.defaultUIScale = 2;
		}else{
			config.setWindowedMode(1024, 768);
			GLTFDemo.defaultUIScale = 1;
		}
		
		GLTFDemo.AUTOLOAD_ENTRY = arg.length > 1 ? arg[1] : null;
		GLTFDemo.AUTOLOAD_VARIANT = arg.length > 2 ? arg[2] : null;
		GLTFDemo.alternateMaps = arg.length > 3 ? arg[3] : null;
		
		GLTFDemoUI.fileSelector = new AWTFileSelector();
		
		new Lwjgl3Application(arg.length > 0 ? new GLTFDemo(arg[0]) : new GLTFDemo(), config);
	}
}
