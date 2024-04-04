package net.mgsx.gltf.demo;

import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder;
import com.github.xpenatan.gdx.backends.teavm.config.plugins.TeaReflectionSupplier;
import com.github.xpenatan.gdx.backends.teavm.gen.SkipClass;
import java.io.File;
import java.io.IOException;
import org.teavm.tooling.TeaVMTool;

@SkipClass
public class Build {

    public static void main(String[] args) throws IOException {
        TeaReflectionSupplier.addReflectionClass("net.mgsx.gltf.demo");

        TeaBuildConfiguration teaBuildConfiguration = new TeaBuildConfiguration();
        teaBuildConfiguration.assetsPath.add(new File(".." + File.separatorChar + "android" + File.separatorChar + "assets"));
        teaBuildConfiguration.webappPath = new File("build" + File.separatorChar + "dist").getCanonicalPath();

        teaBuildConfiguration.additionalAssetsClasspathFiles.add("net/mgsx/gltf/demo/shaders/outline.fs.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("net/mgsx/gltf/demo/shaders/outline.vs.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("net/mgsx/gltf/demo/shaders/gltf-ceil-shading.fs.glsl");
        teaBuildConfiguration.additionalAssetsClasspathFiles.add("net/mgsx/gltf/demo/shaders/gltf-ceil-shading.vs.glsl");

        TeaVMTool tool = TeaBuilder.config(teaBuildConfiguration);
        tool.setMainClass(TeaVMLauncher.class.getName());
        tool.setObfuscated(true);
        TeaBuilder.build(tool, false);
    }
}