package net.mgsx.gltf.scene3d;

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

public class ModelInstancePlus extends ModelInstance
{

	public ModelInstancePlus(Model model) {
		super(model);
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
				NodeAnimationPlus nodeAnim = new NodeAnimationPlus();
				nodeAnim.node = node;
				if (shareKeyframes) {
					nodeAnim.translation = nanim.translation;
					nodeAnim.rotation = nanim.rotation;
					nodeAnim.scaling = nanim.scaling;
					nodeAnim.weights = ((NodeAnimationPlus)nanim).weights;
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
					if (((NodeAnimationPlus)nanim).weights != null) {
						((NodeAnimationPlus)nanim).weights = new Array<NodeKeyframe<WeightVector>>();
						for (final NodeKeyframe<WeightVector> kf : ((NodeAnimationPlus)nanim).weights)
							((NodeAnimationPlus)nanim).weights.add(new NodeKeyframe<WeightVector>(kf.keytime, kf.value));
					}
				}
				if (nodeAnim.translation != null || nodeAnim.rotation != null || nodeAnim.scaling != null || ((NodeAnimationPlus)nanim).weights != null)
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
