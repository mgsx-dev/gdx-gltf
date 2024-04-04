package net.mgsx.gltf.demo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import com.github.xpenatan.gdx.backends.teavm.TeaWindowListener;

public class TeaVMLauncher {
    public static void main(String[] args) {
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("canvas");
        config.width = 0;
        config.height = 0;
        GLTFDemo models = new GLTFDemo("models");
        config.windowListener = new TeaWindowListener() {
            @Override
            public void filesDropped(String[] files) {
                try {
                    String path = files[0];
                    FileHandle model = Gdx.files.internal(path);
                    models.load(model);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new TeaApplication(models, config);
    }
}