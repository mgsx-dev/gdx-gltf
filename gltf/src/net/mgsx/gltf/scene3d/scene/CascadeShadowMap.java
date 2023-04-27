package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import net.mgsx.gltf.scene3d.attributes.CascadeShadowMapAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;

/**
 * Cascade shadow map class basically contains several {@link DirectionalShadowLight} with different view boxes.
 * When rendering shadows, the first depth map is used (the closest). If fragment is outside of its view box, it fallback
 * to the next one, and so on until the default shadow map (the farest).
 * 
 * These additionals {@link DirectionalShadowLight} should be created before using it for rendering.
 * Either manually or by using the {@link #setCascade(DirectionalShadowLight, float)} convenient method.
 * 
 */
public class CascadeShadowMap implements Disposable {

	public final Array<DirectionalShadowLight> lights;
	protected final int cascadeCount;
	protected static final Vector3 v = new Vector3();

	public final CascadeShadowMapAttribute attribute;
	
	/**
	 * @param cascadeCount how many cascade (at least 1)
	 */
	public CascadeShadowMap(int cascadeCount) {
		this.cascadeCount = cascadeCount;
		attribute = new CascadeShadowMapAttribute(this);
		lights = new Array<DirectionalShadowLight>(cascadeCount);
	}
	
	@Override
	public void dispose() {
		for(DirectionalShadowLight light : lights){
			light.dispose();
		}
		lights.clear();
	}
	
	/**
	 * Convenient method to create and configure lights.
	 * Required lights are only created if necessary. It's then safe to call this method every frame.
	 * @param base default {@link DirectionalShadowLight} used. Shadow box should be set before calling this method. 
	 * @param downscale viewport factor between cascades (typically 4.0)
	 */
	public void setCascade(DirectionalShadowLight base, float downscale){
		int w = base.getFrameBuffer().getWidth();
		int h = base.getFrameBuffer().getHeight();
		for(int i=0 ; i<cascadeCount ; i++){
			if(i < lights.size){
				DirectionalShadowLight light = lights.get(i);
				if(light.getFrameBuffer().getWidth() != w ||
						light.getFrameBuffer().getHeight() != h){
					light.dispose();
					lights.set(i, new DirectionalShadowLight(w,h));
				}
			}else{
				lights.add(new DirectionalShadowLight(w,h));
			}
		}
		float scale = downscale;
		Camera baseCam = base.getCamera();
		float baseWidth = baseCam.viewportWidth;
		float baseHeight = baseCam.viewportHeight;
		base.getCenter(v);
		// reverse order : first is the max LOD.
		for(int i=lights.size-1 ; i>=0 ; i--){
			DirectionalShadowLight light = lights.get(i);
			light.baseColor.set(base.baseColor);
			light.color.set(base.baseColor);
			light.direction.set(base.direction);
			light.setCenter(v);
			Camera cam = light.getCamera();
			cam.viewportWidth = baseWidth / scale;
			cam.viewportHeight = baseHeight / scale;
			cam.near = baseCam.near;
			cam.far = baseCam.far;
			cam.up.set(baseCam.up);
			cam.direction.set(base.direction);
			cam.update();
			scale *= scale;
		}
	}
}
