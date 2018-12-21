package net.mgsx.gltf.scene3d.animation;

import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.scene3d.model.WeightVector;

/**
 *  {@link NodeAnimation} hack to store morph targets weights 
 */
public class NodeAnimationHack extends NodeAnimation
{
	public Array<NodeKeyframe<WeightVector>> weights = null;
}
