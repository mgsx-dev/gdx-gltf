package net.mgsx.gltf.scene3d.animation;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

import net.mgsx.gltf.scene3d.model.NodePartPlus;
import net.mgsx.gltf.scene3d.model.NodePlus;
import net.mgsx.gltf.scene3d.model.WeightVector;

public class AnimationControllerPlus extends AnimationController
{
	public static class Transform implements Poolable {
		public final Vector3 translation = new Vector3();
		public final Quaternion rotation = new Quaternion();
		public final Vector3 scale = new Vector3(1, 1, 1);
		public final WeightVector weights = new WeightVector();
		
		public Transform () {
		}

		public Transform idt () {
			translation.set(0, 0, 0);
			rotation.idt();
			scale.set(1, 1, 1);
			weights.set();
			return this;
		}

		public Transform set (final Vector3 t, final Quaternion r, final Vector3 s, final WeightVector w) {
			translation.set(t);
			rotation.set(r);
			scale.set(s);
			weights.set(w);
			return this;
		}

		public Transform set (final Transform other) {
			return set(other.translation, other.rotation, other.scale, other.weights);
		}

		public Transform lerp (final Transform target, final float alpha) {
			return lerp(target.translation, target.rotation, target.scale, target.weights, alpha);
		}

		public Transform lerp (final Vector3 targetT, final Quaternion targetR, final Vector3 targetS, final WeightVector targetW, final float alpha) {
			translation.lerp(targetT, alpha);
			rotation.slerp(targetR, alpha);
			scale.lerp(targetS, alpha);
			weights.lerp(targetW, alpha);
			return this;
		}

		public Matrix4 toMatrix4 (final Matrix4 out) {
			return out.set(translation, rotation, scale);
		}

		@Override
		public void reset () {
			idt();
		}

		@Override
		public String toString () {
			return translation.toString() + " - " + rotation.toString() + " - " + scale.toString() + " - " + weights.toString();
		}
	}
	
	public AnimationControllerPlus(ModelInstance target) {
		super(target);
	}

	private final Pool<Transform> transformPool = new Pool<Transform>() {
		@Override
		protected Transform newObject () {
			return new Transform();
		}
	};
	private final static ObjectMap<Node, Transform> transforms = new ObjectMap<Node, Transform>();
	private boolean applying = false;

	/** Begin applying multiple animations to the instance, must followed by one or more calls to {
	 * {@link #apply(Animation, float, float)} and finally {{@link #end()}. */
	
	protected void begin () {
		if (applying) throw new GdxRuntimeException("You must call end() after each call to being()");
		applying = true;
	}

	/** Apply an animation, must be called between {{@link #begin()} and {{@link #end()}.
	 * @param weight The blend weight of this animation relative to the previous applied animations. */
	@Override
	protected void apply (final Animation animation, final float time, final float weight) {
		if (!applying) throw new GdxRuntimeException("You must call begin() before adding an animation");
		applyAnimationPlus(transforms, transformPool, weight, animation, time);
	}

	/** End applying multiple animations to the instance and update it to reflect the changes. */
	@Override
	protected void end () {
		if (!applying) throw new GdxRuntimeException("You must call begin() first");
		for (Entry<Node, Transform> entry : transforms.entries()) {
			entry.value.toMatrix4(entry.key.localTransform);
			transformPool.free(entry.value);
		}
		transforms.clear();
		target.calculateTransforms();
		applying = false;
	}

	/** Apply a single animation to the {@link ModelInstance} and update the it to reflect the changes. */
	@Override
	protected void applyAnimation (final Animation animation, final float time) {
		if (applying) throw new GdxRuntimeException("Call end() first");
		applyAnimationPlus(null, (Pool<Transform>)null, 1.f, animation, time);
		target.calculateTransforms();
	}

	/** Apply two animations, blending the second onto to first using weight. */
	@Override
	protected void applyAnimations (final Animation anim1, final float time1, final Animation anim2, final float time2,
		final float weight) {
		if (anim2 == null || weight == 0.f)
			applyAnimation(anim1, time1);
		else if (anim1 == null || weight == 1.f)
			applyAnimation(anim2, time2);
		else if (applying)
			throw new GdxRuntimeException("Call end() first");
		else {
			begin();
			apply(anim1, time1, 1.f);
			apply(anim2, time2, weight);
			end();
		}
	}
	
	
	private final static Transform tmpT = new Transform();

	private final static <T> int getFirstKeyframeIndexAtTime (final Array<NodeKeyframe<T>> arr, final float time) {
		final int n = arr.size - 1;
		for (int i = 0; i < n; i++) {
			if (time >= arr.get(i).keytime && time <= arr.get(i + 1).keytime) {
				return i;
			}
		}
		return 0;
	}

	private final static Vector3 getTranslationAtTime (final NodeAnimation nodeAnim, final float time, final Vector3 out) {
		if (nodeAnim.translation == null) return out.set(nodeAnim.node.translation);
		if (nodeAnim.translation.size == 1) return out.set(nodeAnim.translation.get(0).value);

		int index = getFirstKeyframeIndexAtTime(nodeAnim.translation, time);
		final NodeKeyframe firstKeyframe = nodeAnim.translation.get(index);
		out.set((Vector3)firstKeyframe.value);

		if (++index < nodeAnim.translation.size) {
			final NodeKeyframe<Vector3> secondKeyframe = nodeAnim.translation.get(index);
			final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
			out.lerp(secondKeyframe.value, t);
		}
		return out;
	}

	private final static Quaternion getRotationAtTime (final NodeAnimation nodeAnim, final float time, final Quaternion out) {
		if (nodeAnim.rotation == null) return out.set(nodeAnim.node.rotation);
		if (nodeAnim.rotation.size == 1) return out.set(nodeAnim.rotation.get(0).value);

		int index = getFirstKeyframeIndexAtTime(nodeAnim.rotation, time);
		final NodeKeyframe firstKeyframe = nodeAnim.rotation.get(index);
		out.set((Quaternion)firstKeyframe.value);

		if (++index < nodeAnim.rotation.size) {
			final NodeKeyframe<Quaternion> secondKeyframe = nodeAnim.rotation.get(index);
			final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
			out.slerp(secondKeyframe.value, t);
		}
		return out;
	}

	private final static Vector3 getScalingAtTime (final NodeAnimation nodeAnim, final float time, final Vector3 out) {
		if (nodeAnim.scaling == null) return out.set(nodeAnim.node.scale);
		if (nodeAnim.scaling.size == 1) return out.set(nodeAnim.scaling.get(0).value);

		int index = getFirstKeyframeIndexAtTime(nodeAnim.scaling, time);
		final NodeKeyframe firstKeyframe = nodeAnim.scaling.get(index);
		out.set((Vector3)firstKeyframe.value);

		if (++index < nodeAnim.scaling.size) {
			final NodeKeyframe<Vector3> secondKeyframe = nodeAnim.scaling.get(index);
			final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
			out.lerp(secondKeyframe.value, t);
		}
		return out;
	}
	
	private final static WeightVector getMorphTargetAtTime (final NodeAnimationPlus nodeAnim, final float time, final WeightVector out) {
		if (nodeAnim.weights == null) return out.set();
		if (nodeAnim.weights.size == 1) return out.set(nodeAnim.weights.get(0).value);

		int index = getFirstKeyframeIndexAtTime(nodeAnim.weights, time);
		final NodeKeyframe firstKeyframe = nodeAnim.weights.get(index);
		out.set((WeightVector)firstKeyframe.value);

		if (++index < nodeAnim.weights.size) {
			final NodeKeyframe<WeightVector> secondKeyframe = nodeAnim.weights.get(index);
			final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
			out.lerp(secondKeyframe.value, t);
		}
		return out;
	}

	private final static Transform getNodeAnimationTransform (final NodeAnimation nodeAnim, final float time) {
		final Transform transform = tmpT;
		getTranslationAtTime(nodeAnim, time, transform.translation);
		getRotationAtTime(nodeAnim, time, transform.rotation);
		getScalingAtTime(nodeAnim, time, transform.scale);
		if(nodeAnim instanceof NodeAnimationPlus)
			getMorphTargetAtTime((NodeAnimationPlus)nodeAnim, time, transform.weights);
		return transform;
	}

	private final static void applyNodeAnimationDirectly (final NodeAnimation nodeAnim, final float time) {
		final Node node = nodeAnim.node;
		node.isAnimated = true;
		final Transform transform = getNodeAnimationTransform(nodeAnim, time);
		transform.toMatrix4(node.localTransform);
		if(node instanceof NodePlus){
			if(((NodePlus)node).weights != null){
				((NodePlus)node).weights.set(transform.weights);
				for(NodePart part : node.parts){
					((NodePartPlus)part).morphTargets.set(transform.weights);
				}
			}
		}
	}

	private final static void applyNodeAnimationBlending (final NodeAnimation nodeAnim, final ObjectMap<Node, Transform> out,
		final Pool<Transform> pool, final float alpha, final float time) {

		final Node node = nodeAnim.node;
		node.isAnimated = true;
		final Transform transform = getNodeAnimationTransform(nodeAnim, time);

		Transform t = out.get(node, null);
		if (t != null) {
			if (alpha > 0.999999f)
				t.set(transform);
			else
				t.lerp(transform, alpha);
		} else {
			if (alpha > 0.999999f)
				out.put(node, pool.obtain().set(transform));
			else
				out.put(node, pool.obtain().set(node.translation, node.rotation, node.scale, ((NodePlus)node).weights).lerp(transform, alpha));
		}
	}

	/** Helper method to apply one animation to either an objectmap for blending or directly to the bones. */
	protected static void applyAnimationPlus (final ObjectMap<Node, Transform> out, final Pool<Transform> pool, final float alpha,
		final Animation animation, final float time) {

		if (out == null) {
			for (final NodeAnimation nodeAnim : animation.nodeAnimations)
				applyNodeAnimationDirectly(nodeAnim, time);
		} else {
			for (final Node node : out.keys())
				node.isAnimated = false;
			for (final NodeAnimation nodeAnim : animation.nodeAnimations)
				applyNodeAnimationBlending(nodeAnim, out, pool, alpha, time);
			for (final ObjectMap.Entry<Node, Transform> e : out.entries()) {
				if (!e.key.isAnimated) {
					e.key.isAnimated = true;
					e.value.lerp(e.key.translation, e.key.rotation, e.key.scale, ((NodePlus)e.key).weights, alpha);
				}
			}
		}
	}
}
