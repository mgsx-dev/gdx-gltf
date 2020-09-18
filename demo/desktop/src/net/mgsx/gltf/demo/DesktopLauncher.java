package net.mgsx.gltf.demo;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import net.mgsx.gltf.demo.ui.GLTFDemoUI;

public class DesktopLauncher {
	public static void main (String[] arg) {
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		DisplayMode display = LwjglApplicationConfiguration.getDesktopDisplayMode();
		if(display.height >= 1440){
			config.width = 1920;
			config.height = 1080;
			GLTFDemo.defaultUIScale = 2;
		}else{
			config.width = 1024;
			config.height = 768;
			GLTFDemo.defaultUIScale = 1;
		}
		config.useHDPI = true;
		
		GLTFDemo.AUTOLOAD_ENTRY = arg.length > 1 ? arg[1] : null;
		GLTFDemo.AUTOLOAD_VARIANT = arg.length > 2 ? arg[2] : null;
		GLTFDemo.alternateMaps = arg.length > 3 ? arg[3] : null;
		
		GLTFDemoUI.fileSelector = new AWTFileSelector();
		
		new LwjglApplication(arg.length > 0 ? new GLTFDemo(arg[0]) : new GLTFDemo(), config);
	}
}
