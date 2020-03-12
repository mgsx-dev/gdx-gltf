package net.mgsx.gltf.scene3d.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.math.Vector3;

public class SpotLightEx extends SpotLight {

	/** Optional range in meters.
	 * see {@link net.mgsx.gltf.data.extensions.KHRLightsPunctual.GLTFLight#range} */
	public Float range;

	@Override
	public SpotLight set (final SpotLight copyFrom) {
		if(copyFrom instanceof SpotLightEx){
			return set(copyFrom.color, copyFrom.position, copyFrom.direction, copyFrom.intensity, copyFrom.cutoffAngle, copyFrom.exponent, ((SpotLightEx)copyFrom).range);
		}else{
			return set(copyFrom.color, copyFrom.position, copyFrom.direction, copyFrom.intensity, copyFrom.cutoffAngle, copyFrom.exponent);
		}
	}

	public SpotLightEx set(Color color, Vector3 position, Vector3 direction, float intensity, float cutoffAngle, float exponent, Float range) {
		super.set(color, position, direction, intensity, cutoffAngle, exponent);
		this.range = range;
		return this;
	}
}
