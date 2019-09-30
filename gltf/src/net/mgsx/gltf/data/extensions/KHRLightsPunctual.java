package net.mgsx.gltf.data.extensions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gltf.loaders.exceptions.GLTFIllegalException;
import net.mgsx.gltf.loaders.shared.GLTFTypes;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;

/**
 * {@link net.mgsx.gltf.data.scene.GLTFNode} and {@link net.mgsx.gltf.data.GLTF} (root) extension
 * @see https://github.com/KhronosGroup/glTF/tree/master/extensions/2.0/Khronos/KHR_lights_punctual
 */
abstract public class KHRLightsPunctual {
	
	public static final String EXT = "KHR_lights_punctual";
	
	
	public static class GLTFSpotLight {
		public float innerConeAngle = 0;
		public float outerConeAngle = MathUtils.PI / 4f;
	}

	public static class GLTFLight {
		public static final String TYPE_DIRECTIONAL = "directional";
		public static final String TYPE_POINT = "point";
		public static final String TYPE_SPOT = "spot";
		
		public String name = "";
		public float [] color = {1f, 1f, 1f};
		
		/** 
		 * in Candela for point/spot lights : Ev(lx) = Iv(cd) / (d(m))2 
		 * in Lux for directional lights : Ev(lx)
		 */
		public float intensity = 1f;
		public String type;
		public float range;
		public GLTFSpotLight spot;
	}
	public static class GLTFLights {
		public Array<GLTFLight> lights;
	}
	public static class GLTFLightNode {
		public int light;
	}
	
	
	public static BaseLight map(GLTFLight light) {
		if(GLTFLight.TYPE_DIRECTIONAL.equals(light.type)){
			DirectionalLightEx dl = new DirectionalLightEx();
			dl.baseColor.set(GLTFTypes.mapColor(light.color, Color.WHITE));
			dl.intensity = light.intensity;
			return dl;
		}else if(GLTFLight.TYPE_POINT.equals(light.type)){
			PointLight pl = new PointLight();
			pl.color.set(GLTFTypes.mapColor(light.color, Color.WHITE));
			// Blender exported intensity is the raw value in Watts
			// GLTF spec. states it's in Candela which is lumens per square radian (lm/sr).
			// adjustement is made empirically here (comparing with Blender rendering)
			// TODO find if it's a GLTF Blender exporter issue and find the right conversion.
			pl.intensity = light.intensity / 10f;
			return pl;
		}else if(GLTFLight.TYPE_SPOT.equals(light.type)){
			SpotLight sl = new SpotLight();
			if(light.spot == null) throw new GLTFIllegalException("spot property required for spot light type");
			sl.color.set(GLTFTypes.mapColor(light.color, Color.WHITE));
			
			// same hack as point lights (see point light above)
			sl.intensity = light.intensity / 10f;
			
			// from https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_lights_punctual/README.md#inner-and-outer-cone-angles
			float cosOuterAngle = (float)Math.cos(light.spot.outerConeAngle);
			float cosInnerAngle = (float)Math.cos(light.spot.innerConeAngle);
			float lightAngleScale = 1.0f / Math.max(0.001f, cosInnerAngle - cosOuterAngle);
			float lightAngleOffset = -cosOuterAngle * lightAngleScale;
			
			// XXX we hack libgdx cutoffAngle and exponent variables to store cached scale/offset values.
			// it's not an issue since libgdx default shader doesn't implement spot lights.
			
			sl.cutoffAngle = lightAngleOffset;
			sl.exponent = lightAngleScale;
			return sl;
		} else{
			throw new GdxRuntimeException("unsupported light type " + light.type);
		}
	}
}
