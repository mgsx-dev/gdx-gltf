package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;

import net.mgsx.gltf.scene3d.attributes.CascadeShadowMapAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;

/**
 * Cascade shadow map class basically contains several {@link DirectionalShadowLight} with different view boxes.
 * When rendering shadows, the first depth map is used (the closest). If fragment is outside of its view box, it fallback
 * to the next one, and so on until the default shadow map (the farest).
 * 
 * These additionals {@link DirectionalShadowLight} could be created and configured manually or by using one of the setCascades methods.
 * And should be configured before rendering.
 * 
 */
public class CascadeShadowMap implements Disposable {

	public final Array<DirectionalShadowLight> lights;
	public final CascadeShadowMapAttribute attribute;
	
	protected final int cascadeCount;
	
	protected final FloatArray splitRates;
	private final Vector3 [] splitPoints;
	private final Matrix4 lightMatrix = new Matrix4();
	private final BoundingBox box = new BoundingBox();
	private final Vector3 center = new Vector3();
	
	/**
	 * @param cascadeCount how many extra cascades
	 */
	public CascadeShadowMap(int cascadeCount) {
		this.cascadeCount = cascadeCount;
		attribute = new CascadeShadowMapAttribute(this);
		lights = new Array<DirectionalShadowLight>(cascadeCount);
		splitRates = new FloatArray(cascadeCount+2);
		splitPoints = new Vector3[8];
		for(int i=0 ; i<splitPoints.length ; i++){
			splitPoints[i] = new Vector3();
		}
	}
	
	@Override
	public void dispose() {
		for(DirectionalShadowLight light : lights){
			light.dispose();
		}
		lights.clear();
	}
	
	/**
	 * Setup base light and extra cascades based on scene camera frustum. With automatic split rates.
	 * @param sceneCamera the camera used to render the scene (frustum should be up to date)
	 * @param base the default shadow light, used for far shadows
	 * @param minLlightDepth minimum shadow box depth, depends on the scene, big value means more objects casted but less precision.
	 * A zero value restricts shadow box depth to the frustrum (only visible objects by the scene camera).
	 * @param splitDivisor Describe how to split scene camera frustum. . With a value of 4, far cascade covers the
	 * range: 1/4 to 1/1, next cascade, the range 1/16 to 1/4, and so on. The closest one covers the remaining starting
	 * from 0. When used with 2 extra cascades (3 areas), split points are: 0.0, 1/16, 1/4, 1.0.
	 */
	public void setCascades(Camera sceneCamera, DirectionalShadowLight base, float minLlightDepth, float splitDivisor){
		splitRates.clear();
		float rate = 1f;
		for(int i=0 ; i<cascadeCount+1 ; i++){
			splitRates.add(rate);
			rate /= splitDivisor;
		}
		splitRates.add(0);
		splitRates.reverse();
		
		setCascades(sceneCamera, base, minLlightDepth, splitRates);
	}
	
	/**
	 * Setup base light and extra cascades based on scene camera frustum. With user defined split rates.
	 * @param sceneCamera the camera used to render the scene (frustum should be up to date)
	 * @param base the default shadow light, used for far shadows
	 * @param minLlightDepth minimum shadow box depth, depends on the scene, big value means more objects casted but less precision.
	 * A zero value restricts shadow box depth to the frustrum (only visible objects by the scene camera).
	 * @param splitRates Describe how to split scene camera frustum. The first 2 values define near and far rate for the closest cascade, 
	 * Second and third value define near and far rate for the second cascade, and so on.
	 * When used with 2 extra cascades (3 areas), 4 split rates are expected. Eg: [0.0, 0.1, 0.3, 1.0].
	 */
	public void setCascades(Camera sceneCamera, DirectionalShadowLight base, float minLlightDepth, FloatArray splitRates){
		if(splitRates.size != cascadeCount+2){
			throw new IllegalArgumentException("Invalid splitRates, expected " + cascadeCount+2 + " items.");
		}
		
		syncExtraCascades(base);
		
		for(int i=0 ; i<cascadeCount+1 ; i++){
			float splitNear = splitRates.get(i);
			float splitFar = splitRates.get(i+1);
			DirectionalShadowLight light = i < cascadeCount ? lights.get(i) : base;
			if(light != base) {
				light.direction.set(base.direction);
				light.getCamera().up.set(base.getCamera().up);
			}
			setCascades(light, sceneCamera, splitNear, splitFar, minLlightDepth);
		}
	}
	
	private void setCascades(DirectionalShadowLight shadowLight, Camera cam, float splitNear, float splitFar, float minLlightDepth){
		
		for(int i=0 ; i<4 ; i++){
			Vector3 a = cam.frustum.planePoints[i];
			Vector3 b = cam.frustum.planePoints[i+4];
			
			splitPoints[i].set(a).lerp(b, splitNear);
			splitPoints[i+4].set(a).lerp(b, splitFar);
		}
		
		lightMatrix.setToLookAt(shadowLight.direction, shadowLight.getCamera().up);
		box.inf();
		for(int i=0 ; i<splitPoints.length; i++){
			Vector3 v = splitPoints[i].mul(lightMatrix);
			box.ext(v);
		}
		float halfFrustumDepth = box.getDepth() / 2;
		
		float lightDepth = Math.max(minLlightDepth, box.getDepth());
		
		box.getCenter(center);
		center.mul(lightMatrix.tra());
		center.mulAdd(shadowLight.direction, halfFrustumDepth  - lightDepth/2);
		
		shadowLight.setCenter(center);
		shadowLight.setViewport(box.getWidth(), box.getHeight(), 0, lightDepth);
	}
	
	/**
	 * create or recreate, if necessary ,extra cascades with same resolution as the default shadow light.
	 * @param base the default shadow light
	 */
	protected void syncExtraCascades(DirectionalShadowLight base) {
		int w = base.getFrameBuffer().getWidth();
		int h = base.getFrameBuffer().getHeight();
		for(int i=0 ; i<cascadeCount ; i++){
			DirectionalShadowLight light;
			if(i < lights.size){
				light = lights.get(i);
				if(light.getFrameBuffer().getWidth() != w ||
						light.getFrameBuffer().getHeight() != h){
					light.dispose();
					lights.set(i, light = createLight(w, h));
				}
			}else{
				lights.add(light = createLight(w, h));
			}
			light.direction.set(base.direction);
			light.getCamera().up.set(base.getCamera().up);
		}
	}

	/**
	 * Allow subclass to use their own shadow light implementation.
	 * @param width
	 * @param height
	 * @return a new directional shadow light.
	 */
	protected DirectionalShadowLight createLight(int width, int height) {
		return new DirectionalShadowLight(width, height);
	}
}
