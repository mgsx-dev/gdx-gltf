package net.mgsx.gltf.scene3d.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Implementation of SpotLight with extra features.
 * <b>WARNING</b> SpotLight fields {@link SpotLight#cutoffAngle} and {@link SpotLight#exponent} shouldn't be set manually.
 * use {@link #setConeRad(float, float)} or {@link #setConeDeg(float, float)} instead.
 */
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

	/** @deprecated use {@link #setRad(Color, Vector3, Vector3, float, float, float, Float)} or {@link #setDeg(Color, Vector3, Vector3, float, float, float, Float)} instead. */
	@Deprecated
	public SpotLightEx set(Color color, Vector3 position, Vector3 direction, float intensity, float cutoffAngle, float exponent, Float range) {
		super.set(color, position, direction, intensity, cutoffAngle, exponent);
		this.range = range;
		return this;
	}
	
	public SpotLightEx setRad(Color color, Vector3 position, Vector3 direction, float intensity, float outerConeAngleRad, float innerConeAngleRad, Float range) {
		if (color != null) this.color.set(color);
		if (position != null) this.position.set(position);
		if (direction != null) this.direction.set(direction).nor();
		this.intensity = intensity;
		setConeRad(outerConeAngleRad, innerConeAngleRad);
		this.range = range;
		return this;
	}
	
	public SpotLightEx setDeg(Color color, Vector3 position, Vector3 direction, float intensity, float outerConeAngleDeg, float innerConeAngleDeg, Float range) {
		return setRad(color, position, direction, intensity, outerConeAngleDeg * MathUtils.degreesToRadians, innerConeAngleDeg * MathUtils.degreesToRadians, range);
	}
	
	public SpotLightEx setConeRad(float outerConeAngleRad, float innerConeAngleRad)
	{
		// from https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_lights_punctual/README.md#inner-and-outer-cone-angles
		float cosOuterAngle = (float)Math.cos(outerConeAngleRad);
		float cosInnerAngle = (float)Math.cos(innerConeAngleRad);
		float lightAngleScale = 1.0f / Math.max(0.001f, cosInnerAngle - cosOuterAngle);
		float lightAngleOffset = -cosOuterAngle * lightAngleScale;
		
		// XXX we hack libgdx cutoffAngle and exponent variables to store cached scale/offset values.
		// it's not an issue since libgdx default shader doesn't implement spot lights.
		
		cutoffAngle = lightAngleOffset;
		exponent = lightAngleScale;
		
		return this;
	}
	
	public SpotLightEx setConeDeg(float outerConeAngleDeg, float innerConeAngleDeg)
	{
		return setConeRad(outerConeAngleDeg * MathUtils.degreesToRadians, innerConeAngleDeg * MathUtils.degreesToRadians);
	}
}
