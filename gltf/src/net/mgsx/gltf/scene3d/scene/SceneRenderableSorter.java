package net.mgsx.gltf.scene3d.scene;

import java.util.Comparator;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Advanced RenderableSorter providing hints and limit shader, material and mesh switches.
 * Usefull for Skybox : should be rendererd before transparent renderables but after all opaque renderables. 
 */
public class SceneRenderableSorter implements RenderableSorter, Comparator<Renderable> {
	
	public static enum Hints {
		OPAQUE_LAST
	}
	
	private Camera camera;
	private final Vector3 tmpV1 = new Vector3();
	private final Vector3 tmpV2 = new Vector3();

	@Override
	public void sort (final Camera camera, final Array<Renderable> renderables) {
		this.camera = camera;
		renderables.sort(this);
	}

	private Vector3 getTranslation (Matrix4 worldTransform, Vector3 center, Vector3 output) {
		if (center.isZero())
			worldTransform.getTranslation(output);
		else if (!worldTransform.hasRotationOrScaling())
			worldTransform.getTranslation(output).add(center);
		else
			output.set(center).mul(worldTransform);
		return output;
	}
	
	@Override
	public int compare(Renderable o1, Renderable o2) 
	{
		// original (blending and hints)
		final boolean b1 = o1.material.has(BlendingAttribute.Type) && ((BlendingAttribute)o1.material.get(BlendingAttribute.Type)).blended;
		final boolean b2 = o2.material.has(BlendingAttribute.Type) && ((BlendingAttribute)o2.material.get(BlendingAttribute.Type)).blended;

		final Hints h1 = o1.userData instanceof Hints ? (Hints)o1.userData : null;
		final Hints h2 = o2.userData instanceof Hints ? (Hints)o2.userData : null;
		
		if(h1 != h2){
			if(h1 == Hints.OPAQUE_LAST){
				return b2 ? -1 : 1;
			}
			if(h2 == Hints.OPAQUE_LAST){
				return b1 ? 1 : -1;
			}
		}
		
		// solid models are grouped by context (same shader, env, material, mesh) to minimize switches.
		if(!b1 && !b2){
			
			int shaderCompare = compareIdentity(o1.shader, o2.shader);
			if(shaderCompare != 0) return shaderCompare;
			
			int envCompare = compareIdentityNullable(o1.environment, o2.environment);
			if(envCompare != 0) return envCompare;
			
			int materialCompare = compareIdentity(o1.material, o2.material);
			if(materialCompare != 0) return materialCompare;
			
			int meshCompare = compareIdentity(o1.meshPart.mesh, o2.meshPart.mesh);
			if(meshCompare != 0) return meshCompare;
		}
		// solid models should be rendered before transparent models
		else if(b1 && !b2){
			return 1;
		}else if(!b1 && b2){
			return -1;
		}
		
		// classic with distance : front to back for solid, back to front for transparent.
		getTranslation(o1.worldTransform, o1.meshPart.center, tmpV1);
		getTranslation(o2.worldTransform, o2.meshPart.center, tmpV2);
		final float dst = (int)(camera.position.dst2(tmpV1)) - (int)(camera.position.dst2(tmpV2));
		final int result = dst < 0 ? -1 : (dst > 0 ? 1 : 0);
		return b1 ? -result : result;
	}
	
	private int compareIdentityNullable(Object o1, Object o2){
		if(o1 == null && o2 == null) return 0;
		if(o1 == null) return -1;
		if(o2 == null) return 1;
		return compareIdentity(o1, o2);
	}
	
	private int compareIdentity(Object o1, Object o2){
		if(o1 == o2) return 0;
		return Integer.compare(o1.hashCode(), o2.hashCode());
	}
}
