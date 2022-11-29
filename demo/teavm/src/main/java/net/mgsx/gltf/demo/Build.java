package net.mgsx.gltf.demo;

import com.github.xpenatan.gdx.backends.teavm.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.TeaBuilder;
import com.github.xpenatan.gdx.backends.teavm.plugins.TeaReflectionSupplier;
import java.io.File;
import java.io.IOException;

public class Build {

    public static void main(String[] args) throws IOException {
        TeaReflectionSupplier.addReflectionClass("net.mgsx.gltf.demo");

        TeaBuildConfiguration teaBuildConfiguration = new TeaBuildConfiguration();
        teaBuildConfiguration.assetsPath.add(new File("../android/assets"));
        teaBuildConfiguration.webappPath = new File(".").getCanonicalPath();
        teaBuildConfiguration.obfuscate = true;
        teaBuildConfiguration.logClasses = false;
        teaBuildConfiguration.setApplicationListener(TeaVMGLTFDemo.class);

        teaBuildConfiguration.additionalAssetsClasspathFiles.add("net/mgsx/gltf/demo/shaders/outline.fs.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("net/mgsx/gltf/demo/shaders/outline.vs.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("net/mgsx/gltf/demo/shaders/gltf-ceil-shading.fs.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("net/mgsx/gltf/demo/shaders/gltf-ceil-shading.vs.glsl");

        TeaBuilder.build(teaBuildConfiguration);
    }
}