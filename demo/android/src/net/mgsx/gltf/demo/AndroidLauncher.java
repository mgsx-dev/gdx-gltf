package net.mgsx.gltf.demo;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import android.os.Bundle;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GLTFDemo.AUTOLOAD_ENTRY = "BoomBox";
		GLTFDemo.AUTOLOAD_VARIANT = "glTF";
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new GLTFDemo(), config);
	}
}
