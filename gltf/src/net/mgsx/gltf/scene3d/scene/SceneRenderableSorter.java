package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.DefaultRenderableSorter;

/**
 * Advnaced RenderableSorter providing hints.
 * Usefull for Skybox : should be rendererd before transparent renderables but after all opaque renderables. 
 */
public class SceneRenderableSorter extends DefaultRenderableSorter {
	
	public static enum Hints {
		OPAQUE_LAST
	}
	
	public static final Object o = null;
	
	@Override
	public int compare(Renderable o1, Renderable o2) 
	{
		final boolean b1 = o1.material.has(BlendingAttribute.Type) && ((BlendingAttribute)o1.material.get(BlendingAttribute.Type)).blended;
		final boolean b2 = o2.material.has(BlendingAttribute.Type) && ((BlendingAttribute)o2.material.get(BlendingAttribute.Type)).blended;

		final Hints h1 = o1.userData instanceof Hints ? (Hints)o1.userData : null;
		final Hints h2 = o2.userData instanceof Hints ? (Hints)o2.userData : null;
		
		if(h1 == h2) return super.compare(o1, o2);
		
		if(h1 == Hints.OPAQUE_LAST){
			return b2 ? -1 : 1;
		}
		if(h2 == Hints.OPAQUE_LAST){
			return b1 ? 1 : -1;
		}
		
		return super.compare(o1, o2);
	}
}
