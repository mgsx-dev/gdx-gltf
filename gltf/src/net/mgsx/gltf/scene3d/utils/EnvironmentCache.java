package net.mgsx.gltf.scene3d.utils;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Environment;

public class EnvironmentCache extends Environment {

	/**
	 * fast way to copy only references
	 * @param env
	 */
	public void setCache(Environment env){
		this.mask = env.getMask();
		this.attributes.clear();
		for(Attribute a : env) this.attributes.add(a);
		this.shadowMap  = env.shadowMap;
		this.sorted = true;
	}

	/**
	 * fast way to replace an attribute without sorting
	 * @param attribute
	 */
	public void replaceCache(Attribute attribute) {
		final int idx = indexOf(attribute.type);
		this.attributes.set(idx, attribute);
	}
}
