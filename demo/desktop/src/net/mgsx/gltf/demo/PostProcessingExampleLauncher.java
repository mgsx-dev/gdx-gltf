package net.mgsx.gltf.demo;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import net.mgsx.gltf.examples.GLTFPostProcessingExample;

public class PostProcessingExampleLauncher {
  public static void main(String[] arg) {
    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    new LwjglApplication(new GLTFPostProcessingExample(), config);
  }
}
