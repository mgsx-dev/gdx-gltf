package net.mgsx.gltf.ibl;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import net.mgsx.gltf.ibl.io.AWTFileSelector;
import net.mgsx.gltf.ibl.io.FileSelector;

public class IBLComposerLauncher { 
	public static void main(String[] args) {
		FileSelector.instance = new AWTFileSelector();
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1024;
		config.height = 768;
		config.useHDPI = true;
		new LwjglApplication(new IBLComposerApp(args.length > 0 ? args[0] : null), config);
		
	}
}
