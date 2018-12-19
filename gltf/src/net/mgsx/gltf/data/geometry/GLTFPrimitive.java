package net.mgsx.gltf.data.geometry;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gltf.data.GLTFObject;

public class GLTFPrimitive extends GLTFObject {
	public ObjectMap<String, Integer> attributes;
	public Integer indices;
	public Integer mode;
	public Integer material;
	public Array<ObjectMap<String, Integer>> targets; // TODO mapping error : integer to floats ....
	
}
