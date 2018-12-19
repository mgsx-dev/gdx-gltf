package net.mgsx.gltf.data;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;

public class GLTFExtras implements Serializable
{
	public JsonValue value;
	
	@Override
	public void write(Json json) {
		
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		value = jsonData;
	}
}
