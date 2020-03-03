package net.mgsx.gltf.scene3d.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;

public class PointLightEx extends PointLight
{
	/** see {@link net.mgsx.gltf.data.extensions.KHRLightsPunctual.GLTFLight#range} */
	public Float range;
	
	@Override
	public PointLight set (final PointLight copyFrom) {
		if(copyFrom instanceof PointLightEx){
			return set(copyFrom.color, copyFrom.position, copyFrom.intensity, ((PointLightEx)copyFrom).range);
		}else{
			return set(copyFrom.color, copyFrom.position, copyFrom.intensity);
		}
	}

	public PointLightEx set(Color color, Vector3 position, float intensity, Float range) {
		super.set(color, position, intensity);
		this.range = range;
		return this;
	}
	
}
