package net.mgsx.gltf.demo;

import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import com.github.xpenatan.gdx.backends.web.WebApplication;
import com.github.xpenatan.gdx.backends.web.WebApplicationConfiguration;

public class TeaVMLauncher {
    public static void main(String[] args) {
        WebApplicationConfiguration config = new TeaApplicationConfiguration("canvas");
        config.width = 0;
        config.height = 0;
        new WebApplication(new GLTFDemo("models"), config);
    }
}
