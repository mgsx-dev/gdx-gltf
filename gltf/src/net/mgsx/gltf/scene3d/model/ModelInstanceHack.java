package net.mgsx.gltf.scene3d.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.scene3d.animation.NodeAnimationHack;

/**
 * {@link ModelInstance} hack for morph targets :
 * - copy animations with {@link NodeAnimationHack}
 * - pass morph targets to shader via Renderable userData 
 */
public class ModelInstanceHack extends ModelInstance
{

	public ModelInstanceHack(Model model) {
		super(model);
		// patch animation copy because of private method
		animations.clear();
		copyAnimations(model.animations, defaultShareKeyframes);
	}
	
	public ModelInstanceHack(Model model, final String... rootNodeIds){
		super(model, rootNodeIds);
		// patch animation copy because of private method
		animations.clear();
		copyAnimations(model.animations, defaultShareKeyframes);
	}
	
	private void copyAnimations (final Iterable<Animation> source, boolean shareKeyframes) {
		for (final Animation anim : source) {
			Animation animation = new Animation();
			animation.id = anim.id;
			animation.duration = anim.duration;
			for (final NodeAnimation nanim : anim.nodeAnimations) {
				final Node node = getNode(nanim.node.id);
				if (node == null) continue;
				NodeAnimationHack nodeAnim = new NodeAnimationHack();
				nodeAnim.node = node;
				
				nodeAnim.translationMode = ((NodeAnimationHack)nanim).translationMode;
				nodeAnim.rotationMode = ((NodeAnimationHack)nanim).rotationMode;
				nodeAnim.scalingMode = ((NodeAnimationHack)nanim).scalingMode;
				nodeAnim.weightsMode = ((NodeAnimationHack)nanim).weightsMode;
				
				if (shareKeyframes) {
					nodeAnim.translation = nanim.translation;
					nodeAnim.rotation = nanim.rotation;
					nodeAnim.scaling = nanim.scaling;
					nodeAnim.weights = ((NodeAnimationHack)nanim).weights;
				} else {
					if (nanim.translation != null) {
						nodeAnim.translation = new Array<NodeKeyframe<Vector3>>();
						for (final NodeKeyframe<Vector3> kf : nanim.translation)
							nodeAnim.translation.add(new NodeKeyframe<Vector3>(kf.keytime, kf.value));
					}
					if (nanim.rotation != null) {
						nodeAnim.rotation = new Array<NodeKeyframe<Quaternion>>();
						for (final NodeKeyframe<Quaternion> kf : nanim.rotation)
							nodeAnim.rotation.add(new NodeKeyframe<Quaternion>(kf.keytime, kf.value));
					}
					if (nanim.scaling != null) {
						nodeAnim.scaling = new Array<NodeKeyframe<Vector3>>();
						for (final NodeKeyframe<Vector3> kf : nanim.scaling)
							nodeAnim.scaling.add(new NodeKeyframe<Vector3>(kf.keytime, kf.value));
					}
					if (((NodeAnimationHack)nanim).weights != null) {
						((NodeAnimationHack)nanim).weights = new Array<NodeKeyframe<WeightVector>>();
						for (final NodeKeyframe<WeightVector> kf : ((NodeAnimationHack)nanim).weights)
							((NodeAnimationHack)nanim).weights.add(new NodeKeyframe<WeightVector>(kf.keytime, kf.value));
					}
				}
				if (nodeAnim.translation != null || nodeAnim.rotation != null || nodeAnim.scaling != null || ((NodeAnimationHack)nanim).weights != null)
					animation.nodeAnimations.add(nodeAnim);
			}
			if (animation.nodeAnimations.size > 0) animations.add(animation);
		}
	}
	
	@Override
	public Renderable getRenderable (final Renderable out, final Node node, final NodePart nodePart) {
		super.getRenderable(out, node, nodePart);
		if(nodePart instanceof NodePartPlus){
			out.userData = ((NodePartPlus) nodePart).morphTargets;
		}
		return out;
	}
	
	
	
}
