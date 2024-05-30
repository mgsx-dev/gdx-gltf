package net.mgsx.gltf.demo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;

import net.mgsx.gltf.scene3d.utils.ShaderParser;

public class ShaderGen {
	public static void main(String[] args) {
		Gdx.files = new Lwjgl3Files();
		String vs = ShaderParser.parse(Gdx.files.classpath("net/mgsx/gltf/shaders/pbr/pbr.vs.glsl"));
		Gdx.files.local("pbr-all.vs.glsl").writeString(vs, false);
		String fs = ShaderParser.parse(Gdx.files.classpath("net/mgsx/gltf/shaders/pbr/pbr.fs.glsl"));
		Gdx.files.local("pbr-all.fs.glsl").writeString(fs, false);
	}
}
