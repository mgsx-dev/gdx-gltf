package net.mgsx.gltf.demo.data;

import com.badlogic.gdx.utils.ObjectMap;

public class ModelEntry {
	public String name, screenshot;
	public ObjectMap<String, String> variants;
	@Override
	public String toString() {
		return name;
	}
}
