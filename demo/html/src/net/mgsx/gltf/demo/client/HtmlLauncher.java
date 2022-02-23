package net.mgsx.gltf.demo.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

import net.mgsx.gltf.demo.GLTFDemo;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(1024, 768);
        }

        @Override
        public ApplicationListener createApplicationListener () {
        		GLTFDemo.AUTOLOAD_ENTRY = "BoomBox";
        		GLTFDemo.AUTOLOAD_VARIANT = "glTF";
                return new GLTFDemo("models");
        }
}