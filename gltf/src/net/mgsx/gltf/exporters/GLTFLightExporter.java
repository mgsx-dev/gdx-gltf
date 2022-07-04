package net.mgsx.gltf.exporters;

import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gltf.data.GLTFExtensions;
import net.mgsx.gltf.data.extensions.KHRLightsPunctual;
import net.mgsx.gltf.data.extensions.KHRLightsPunctual.GLTFLight;
import net.mgsx.gltf.data.extensions.KHRLightsPunctual.GLTFLightNode;
import net.mgsx.gltf.data.extensions.KHRLightsPunctual.GLTFLights;
import net.mgsx.gltf.data.extensions.KHRLightsPunctual.GLTFSpotLight;
import net.mgsx.gltf.data.scene.GLTFNode;
import net.mgsx.gltf.loaders.exceptions.GLTFRuntimeException;
import net.mgsx.gltf.loaders.exceptions.GLTFUnsupportedException;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.PointLightEx;
import net.mgsx.gltf.scene3d.lights.SpotLightEx;

class GLTFLightExporter {

	private GLTFExporter base;

	public GLTFLightExporter(GLTFExporter base) {
		this.base = base;
	}

	public void export(ObjectMap<Node, BaseLight> lights) {
		if(base.root.extensionsUsed == null){
			base.root.extensionsUsed = new Array<String>();
		}
		if(!base.root.extensionsUsed.contains(KHRLightsPunctual.EXT, false)){
			base.root.extensionsUsed.add(KHRLightsPunctual.EXT);
		}
		if(base.root.extensionsRequired == null){
			base.root.extensionsRequired = new Array<String>();
		}
		if(!base.root.extensionsRequired.contains(KHRLightsPunctual.EXT, false)){
			base.root.extensionsRequired.add(KHRLightsPunctual.EXT);
		}
		
		for(Entry<Node, BaseLight> entry : lights){
			int nodeID = base.nodeMapping.indexOf(entry.key, true);
			if(nodeID < 0) throw new GLTFRuntimeException("node not found");
			GLTFNode glNode = base.root.nodes.get(nodeID);
			
			if(base.root.extensions == null){
				base.root.extensions = new GLTFExtensions();
			}
			GLTFLights extLights = base.root.extensions.get(KHRLightsPunctual.GLTFLights.class, KHRLightsPunctual.EXT);
			if(extLights == null){
				base.root.extensions.set(KHRLightsPunctual.EXT, extLights = new GLTFLights());
			}
			if(extLights.lights == null){
				extLights.lights = new Array<GLTFLight>();
			}
			GLTFLight glLight = map(new GLTFLight(), entry.value);
			glLight.name = glNode.name;
			extLights.lights.add(glLight);
			
			if(glNode.extensions == null){
				glNode.extensions = new GLTFExtensions();
			}
			GLTFLightNode nodeLight = glNode.extensions.get(GLTFLightNode.class, KHRLightsPunctual.EXT);
			if(nodeLight == null){
				glNode.extensions.set(KHRLightsPunctual.EXT, nodeLight = new GLTFLightNode());
			}
			nodeLight.light = extLights.lights.size - 1;
		}
	}
	
	public static GLTFLight map(GLTFLight glLight, BaseLight light) {
		float intensityScale;
		if(light instanceof DirectionalLight){
			glLight.type = GLTFLight.TYPE_DIRECTIONAL;
			if(light instanceof DirectionalLightEx){
				glLight.intensity = ((DirectionalLightEx) light).intensity;
			}else{
				glLight.intensity = 1;
			}
			intensityScale = 1;
		}
		else if(light instanceof PointLight){
			glLight.type = GLTFLight.TYPE_POINT;
			if(light instanceof PointLightEx){
				glLight.intensity = ((PointLightEx) light).intensity;
				glLight.range = ((PointLightEx) light).range;
			}else{
				glLight.intensity = 1;
			}
			intensityScale = 10;
		}
		else if(light instanceof SpotLight){
			glLight.type = GLTFLight.TYPE_SPOT;
			glLight.spot = new GLTFSpotLight();
			if(light instanceof SpotLightEx){
				glLight.intensity = ((SpotLightEx) light).intensity;
				glLight.range = ((SpotLightEx) light).range;
			}else{
				glLight.intensity = 1;
			}
			intensityScale = 10;
			// https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_lights_punctual/README.md#inner-and-outer-cone-angles
			// inverse formula
			float cosDeltaAngle = 1f / ((SpotLight)light).exponent;
			float cosOuterAngle = -((SpotLight)light).cutoffAngle / ((SpotLight)light).exponent;
			glLight.spot.outerConeAngle = (float)Math.acos(cosOuterAngle);
			glLight.spot.innerConeAngle = (float)Math.acos(cosOuterAngle + cosDeltaAngle);
		}
		else{
			throw new GLTFUnsupportedException("unsupported light type " + light.getClass());
		}
		
		// rescale color based on intensity
		glLight.color = GLTFExportTypes.rgb(light.color.cpy().mul(1f / glLight.intensity));
		glLight.intensity *= intensityScale;
		
		return glLight;
	}

}
