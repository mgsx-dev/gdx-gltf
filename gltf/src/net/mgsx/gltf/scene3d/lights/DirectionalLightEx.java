package net.mgsx.gltf.scene3d.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;

public class DirectionalLightEx extends DirectionalLight
{
	/** base color clamped */
	public final Color baseColor = new Color(Color.WHITE);
	
	/** light intensity in lux (lm/m2) */
	public float intensity = 1f;
	
	@Override
	public DirectionalLight set (final DirectionalLight copyFrom) {
		if(copyFrom instanceof DirectionalLightEx){
			return set(((DirectionalLightEx) copyFrom).baseColor, copyFrom.direction, ((DirectionalLightEx)copyFrom).intensity);
		}else{
			return set(copyFrom.color, copyFrom.direction, 1f);
		}
	}

	public DirectionalLightEx set(Color baseColor, Vector3 direction, float intensity) {
		this.intensity = intensity;
		this.baseColor.set(baseColor);
		this.direction.set(direction);
		updateColor();
		return this;
	}
	
	public void updateColor(){
		this.color.r = baseColor.r * intensity;
		this.color.g = baseColor.g * intensity;
		this.color.b = baseColor.b * intensity;
	}
	
	@Override
	public boolean equals(DirectionalLight other) {
		return (other instanceof DirectionalLightEx) ? equals((DirectionalLightEx)other) : false;
	}
	
	public boolean equals(DirectionalLightEx other) {
		return (other != null) && ((other == this) || ((baseColor.equals(other.baseColor) && Float.compare(intensity, other.intensity) == 0 && direction.equals(other.direction))));
	}
}
