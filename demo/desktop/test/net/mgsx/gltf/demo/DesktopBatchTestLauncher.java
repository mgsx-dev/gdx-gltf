package net.mgsx.gltf.demo;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopBatchTestLauncher {
  public static void main(String[] arg) {

    // required for HTTPS requests
    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

    LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.width = 1024;
    config.height = 768;
    new LwjglApplication(arg.length > 0 ? new GLTFTest(arg[0]) : new GLTFTest(), config);
  }
}
