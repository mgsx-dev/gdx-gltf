package net.mgsx.gltf.data;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;

import net.mgsx.gltf.data.extensions.KHRMaterialsPBRSpecularGlossiness;
import net.mgsx.gltf.data.extensions.KHRTextureTransform;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public class GLTFExtensions implements Serializable{

	public JsonValue value;
	
	@Override
	public void write(Json json) {
		
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		
		// TODO map to known extensions (official)
		for(int i=0 ; i<jsonData.size ; i++){
			JsonValue child = jsonData.get(i);
			String name = child.name;
			if(KHRMaterialsPBRSpecularGlossiness.EXT.equals(name)){
				extentions.put(name, json.readValue(KHRMaterialsPBRSpecularGlossiness.class, child));
			}else if(KHRTextureTransform.EXT.equals(name)){
				extentions.put(name, json.readValue(KHRTextureTransform.class, child));
			}else{
				System.out.println("unknown extension : " + name);
			}
		}
		
		value = jsonData;
	}
	
	private ObjectMap<String, Object> extentions = new ObjectMap<String, Object>();

	public <T> T get(Class<T> type, String ext) 
	{
		return (T)extentions.get(ext);
	}
	

}
