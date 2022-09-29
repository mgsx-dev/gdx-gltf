
package net.mgsx.gltf.scene3d.animation;

import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.model.*;
import com.badlogic.gdx.graphics.g3d.utils.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.ObjectMap.*;
import com.badlogic.gdx.utils.Pool.*;

import net.mgsx.gltf.loaders.shared.animation.Interpolation;
import net.mgsx.gltf.scene3d.model.*;

/** {@link AnimationController} hack to run morph targets animations */
public class AnimationControllerHack extends AnimationController {
	public class Transform implements Poolable {
		public final Vector3 translation = new Vector3();
		public final Quaternion rotation = new Quaternion();
		public final Vector3 scale = new Vector3(1, 1, 1);
		public final WeightVector weights = new WeightVector();

		public Transform() {
		}

		public Transform idt() {
			translation.set(0, 0, 0);
			rotation.idt();
			scale.set(1, 1, 1);
			weights.set();
			return this;
		}

		public Transform set(final Vector3 t, final Quaternion r, final Vector3 s, final WeightVector w) {
			translation.set(t);
			rotation.set(r);
			scale.set(s);
			if (w != null)
				weights.set(w);
			else
				weights.set();
			return this;
		}

		public Transform set(final Transform other) {
			return set(other.translation, other.rotation, other.scale, other.weights);
		}

		public Transform lerp(final Transform target, final float alpha) {
			return lerp(target.translation, target.rotation, target.scale, target.weights, alpha);
		}

		public Transform lerp(final Vector3 targetT, final Quaternion targetR, final Vector3 targetS, final WeightVector targetW, final float alpha) {
			translation.lerp(targetT, alpha);
			rotation.slerp(targetR, alpha);
			scale.lerp(targetS, alpha);
			if (targetW != null) weights.lerp(targetW, alpha);
			return this;
		}

		public Matrix4 toMatrix4(final Matrix4 out) {
			return out.set(translation, rotation, scale);
		}

		@Override
		public void reset() {
			idt();
		}

		@Override
		public String toString() {
			return translation.toString() + " - " + rotation.toString() + " - " + scale.toString() + " - " + weights.toString();
		}
	}

	public AnimationControllerHack(ModelInstance target) {
		super(target);
	}

	private final Pool<Transform> transformPool = new Pool<Transform>() {
		@Override
		protected Transform newObject() {
			return new Transform();
		}
	};
	private final ObjectMap<Node, Transform> transforms = new ObjectMap<Node, Transform>();
	private boolean applying = false;
	public boolean calculateTransforms = true;

	/** Begin applying multiple animations to the instance, must followed by one or more calls to { {@link #apply(Animation, float, float)} and finally {{@link #end()}. */

	@Override
	protected void begin() {
		if (applying) throw new GdxRuntimeException("You must call end() after each call to being()");
		applying = true;
	}

	/** Apply an animation, must be called between {{@link #begin()} and {{@link #end()}.
	 * 
	 * @param weight The blend weight of this animation relative to the previous applied animations. */
	@Override
	protected void apply(final Animation animation, final float time, final float weight) {
		if (!applying) throw new GdxRuntimeException("You must call begin() before adding an animation");
		applyAnimationPlus(transforms, transformPool, weight, animation, time);
	}

	/** End applying multiple animations to the instance and update it to reflect the changes. */
	@Override
	protected void end() {
		if (!applying) throw new GdxRuntimeException("You must call begin() first");
		for (Entry<Node, Transform> entry : transforms.entries()) {
			entry.value.toMatrix4(entry.key.localTransform);
			transformPool.free(entry.value);
		}
		transforms.clear();
		if (calculateTransforms) target.calculateTransforms();
		applying = false;
	}

	/** Apply a single animation to the {@link ModelInstance} and update the it to reflect the changes. */
	@Override
	protected void applyAnimation(final Animation animation, final float time) {
		if (applying) throw new GdxRuntimeException("Call end() first");
		applyAnimationPlus(null, (Pool<Transform>)null, 1.f, animation, time);
		if (calculateTransforms) target.calculateTransforms();
	}

	/** Apply two animations, blending the second onto to first using weight. */
	@Override
	protected void applyAnimations(final Animation anim1, final float time1, final Animation anim2, final float time2, final float weight) {
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

	private final Transform tmpT = new Transform();

	private final <T> int getFirstKeyframeIndexAtTime(final Array<NodeKeyframe<T>> arr, final float time) {
		final int n = arr.size - 1;
		for (int i = 0; i < n; i++) {
			if (time >= arr.get(i).keytime && time <= arr.get(i + 1).keytime) {
				return i;
			}
		}
		return n;
	}

	private final Vector3 getTranslationAtTime(final NodeAnimation nodeAnim, final float time, final Vector3 out) {
		if (nodeAnim.translation == null) return out.set(nodeAnim.node.translation);
		if (nodeAnim.translation.size == 1) return out.set(nodeAnim.translation.get(0).value);

		int index = getFirstKeyframeIndexAtTime(nodeAnim.translation, time);

		Interpolation interpolation = null;
		if (nodeAnim instanceof NodeAnimationHack) {
			interpolation = ((NodeAnimationHack)nodeAnim).translationMode;
		}

		if (interpolation == Interpolation.STEP) {
			final NodeKeyframe<Vector3> firstKeyframe = nodeAnim.translation.get(index);
			out.set(firstKeyframe.value);
		} else if (interpolation == Interpolation.LINEAR) {
			final NodeKeyframe<Vector3> firstKeyframe = nodeAnim.translation.get(index);
			out.set(firstKeyframe.value);
			if (++index < nodeAnim.translation.size) {
				final NodeKeyframe<Vector3> secondKeyframe = nodeAnim.translation.get(index);
				final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
				out.lerp(secondKeyframe.value, t);
			} else {
				out.set(firstKeyframe.value);
			}
		} else if (interpolation == Interpolation.CUBICSPLINE) {
			final NodeKeyframe<Vector3> firstKeyframe = nodeAnim.translation.get(index);
			if (++index < nodeAnim.translation.size) {
				final NodeKeyframe<Vector3> secondKeyframe = nodeAnim.translation.get(index);
				final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);

				CubicVector3 firstCV = (CubicVector3)firstKeyframe.value;
				CubicVector3 secondCV = (CubicVector3)secondKeyframe.value;

				cubic(out, t, firstCV, firstCV.tangentOut, secondCV, secondCV.tangentIn);
			} else {
				out.set(firstKeyframe.value);
			}
		}

		return out;
	}

	/** https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#appendix-c-spline-interpolation */
	private void cubic(Vector3 out, float t, Vector3 p0, Vector3 m0, Vector3 p1, Vector3 m1) {
		// p(t) = (2t3 - 3t2 + 1)p0 + (t3 - 2t2 + t)m0 + (-2t3 + 3t2)p1 + (t3 - t2)m1
		float t2 = t * t;
		float t3 = t2 * t;
		out.set(p0).scl(2 * t3 - 3 * t2 + 1).mulAdd(m0, t3 - 2 * t2 + t).mulAdd(p1, -2 * t3 + 3 * t2).mulAdd(m1, t3 - t2);
	}

	private final Quaternion q1 = new Quaternion();
	private final Quaternion q2 = new Quaternion();
	private final Quaternion q3 = new Quaternion();
	private final Quaternion q4 = new Quaternion();

	/** https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#appendix-c-spline-interpolation
	 * 
	 * https://github.com/KhronosGroup/glTF-Sample-Viewer/blob/6a862d2607fb47ac48f54786b04e40be2ad866a4/src/interpolator.js */
	private void cubic(Quaternion out, float t, float delta, Quaternion p0, Quaternion m0, Quaternion p1, Quaternion m1) {

		// XXX not good, see https://github.com/KhronosGroup/glTF-Sample-Viewer/blob/master/src/interpolator.js#L42
		delta = -delta;

		// p(t) = (2t3 - 3t2 + 1)p0 + (t3 - 2t2 + t)m0 + (-2t3 + 3t2)p1 + (t3 - t2)m1
		float t2 = t * t;
		float t3 = t2 * t;
		q1.set(p0).mul(2 * t3 - 3 * t2 + 1);
		q2.set(m0).mul(delta).mul(t3 - 2 * t2 + t);
		q3.set(p1).mul(-2 * t3 + 3 * t2);
		q4.set(m1).mul(delta).mul(t3 - t2);

		out.set(q1).add(q2).add(q3).add(q4).nor();
	}

	private void cubic(WeightVector out, float t, WeightVector p0, WeightVector m0, WeightVector p1, WeightVector m1) {
		// p(t) = (2t3 - 3t2 + 1)p0 + (t3 - 2t2 + t)m0 + (-2t3 + 3t2)p1 + (t3 - t2)m1
		float t2 = t * t;
		float t3 = t2 * t;
		out.set(p0).scl(2 * t3 - 3 * t2 + 1).mulAdd(m0, t3 - 2 * t2 + t).mulAdd(p1, -2 * t3 + 3 * t2).mulAdd(m1, t3 - t2);
	}

	private final Quaternion getRotationAtTime(final NodeAnimation nodeAnim, final float time, final Quaternion out) {
		if (nodeAnim.rotation == null) return out.set(nodeAnim.node.rotation);
		if (nodeAnim.rotation.size == 1) return out.set(nodeAnim.rotation.get(0).value);

		int index = getFirstKeyframeIndexAtTime(nodeAnim.rotation, time);

		Interpolation interpolation = null;
		if (nodeAnim instanceof NodeAnimationHack) {
			interpolation = ((NodeAnimationHack)nodeAnim).rotationMode;
		}

		if (interpolation == Interpolation.STEP) {
			final NodeKeyframe<Quaternion> firstKeyframe = nodeAnim.rotation.get(index);
			out.set(firstKeyframe.value);
		} else if (interpolation == Interpolation.LINEAR) {
			final NodeKeyframe<Quaternion> firstKeyframe = nodeAnim.rotation.get(index);
			out.set(firstKeyframe.value);
			if (++index < nodeAnim.rotation.size) {
				final NodeKeyframe<Quaternion> secondKeyframe = nodeAnim.rotation.get(index);
				final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
				out.slerp(secondKeyframe.value, t);
			} else {
				out.set(firstKeyframe.value);
			}
		} else if (interpolation == Interpolation.CUBICSPLINE) {
			final NodeKeyframe<Quaternion> firstKeyframe = nodeAnim.rotation.get(index);
			if (++index < nodeAnim.rotation.size) {
				final NodeKeyframe<Quaternion> secondKeyframe = nodeAnim.rotation.get(index);
				final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);

				CubicQuaternion firstCV = (CubicQuaternion)firstKeyframe.value;
				CubicQuaternion secondCV = (CubicQuaternion)secondKeyframe.value;

				cubic(out, t, secondKeyframe.keytime - firstKeyframe.keytime, firstCV, firstCV.tangentOut, secondCV, secondCV.tangentIn);
			} else {
				out.set(firstKeyframe.value);
			}
		}

		return out;
	}

	private final Vector3 getScalingAtTime(final NodeAnimation nodeAnim, final float time, final Vector3 out) {
		if (nodeAnim.scaling == null) return out.set(nodeAnim.node.scale);
		if (nodeAnim.scaling.size == 1) return out.set(nodeAnim.scaling.get(0).value);

		int index = getFirstKeyframeIndexAtTime(nodeAnim.scaling, time);

		Interpolation interpolation = null;
		if (nodeAnim instanceof NodeAnimationHack) {
			interpolation = ((NodeAnimationHack)nodeAnim).scalingMode;
		}

		if (interpolation == Interpolation.STEP) {
			final NodeKeyframe<Vector3> firstKeyframe = nodeAnim.scaling.get(index);
			out.set(firstKeyframe.value);
		} else if (interpolation == Interpolation.LINEAR) {
			final NodeKeyframe<Vector3> firstKeyframe = nodeAnim.scaling.get(index);
			out.set(firstKeyframe.value);
			if (++index < nodeAnim.scaling.size) {
				final NodeKeyframe<Vector3> secondKeyframe = nodeAnim.scaling.get(index);
				final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
				out.lerp(secondKeyframe.value, t);
			} else {
				out.set(firstKeyframe.value);
			}
		} else if (interpolation == Interpolation.CUBICSPLINE) {
			final NodeKeyframe<Vector3> firstKeyframe = nodeAnim.scaling.get(index);
			if (++index < nodeAnim.scaling.size) {
				final NodeKeyframe<Vector3> secondKeyframe = nodeAnim.scaling.get(index);
				final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);

				CubicVector3 firstCV = (CubicVector3)firstKeyframe.value;
				CubicVector3 secondCV = (CubicVector3)secondKeyframe.value;

				cubic(out, t, firstCV, firstCV.tangentOut, secondCV, secondCV.tangentIn);
			} else {
				out.set(firstKeyframe.value);
			}
		}
		return out;
	}

	private final WeightVector getMorphTargetAtTime(final NodeAnimationHack nodeAnim, final float time, final WeightVector out) {
		if (nodeAnim.weights == null) return out.set();
		if (nodeAnim.weights.size == 1) return out.set(nodeAnim.weights.get(0).value);

		int index = getFirstKeyframeIndexAtTime(nodeAnim.weights, time);

		Interpolation interpolation = null;
		if (nodeAnim instanceof NodeAnimationHack) {
			interpolation = nodeAnim.weightsMode;
		}

		if (interpolation == Interpolation.STEP) {
			final NodeKeyframe<WeightVector> firstKeyframe = nodeAnim.weights.get(index);
			out.set(firstKeyframe.value);
		} else if (interpolation == Interpolation.LINEAR) {
			final NodeKeyframe<WeightVector> firstKeyframe = nodeAnim.weights.get(index);
			out.set(firstKeyframe.value);
			if (++index < nodeAnim.weights.size) {
				final NodeKeyframe<WeightVector> secondKeyframe = nodeAnim.weights.get(index);
				final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);
				out.lerp(secondKeyframe.value, t);
			} else {
				out.set(firstKeyframe.value);
			}
		} else if (interpolation == Interpolation.CUBICSPLINE) {
			final NodeKeyframe<WeightVector> firstKeyframe = nodeAnim.weights.get(index);
			if (++index < nodeAnim.weights.size) {
				final NodeKeyframe<WeightVector> secondKeyframe = nodeAnim.weights.get(index);
				final float t = (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime);

				CubicWeightVector firstCV = (CubicWeightVector)firstKeyframe.value;
				CubicWeightVector secondCV = (CubicWeightVector)secondKeyframe.value;

				cubic(out, t, firstCV, firstCV.tangentOut, secondCV, secondCV.tangentIn);
			} else {
				out.set(firstKeyframe.value);
			}
		}

		return out;
	}

	private final Transform getNodeAnimationTransform(final NodeAnimation nodeAnim, final float time) {
		final Transform transform = tmpT;
		getTranslationAtTime(nodeAnim, time, transform.translation);
		getRotationAtTime(nodeAnim, time, transform.rotation);
		getScalingAtTime(nodeAnim, time, transform.scale);
		if (nodeAnim instanceof NodeAnimationHack) getMorphTargetAtTime((NodeAnimationHack)nodeAnim, time, transform.weights);
		return transform;
	}

	private final void applyNodeAnimationDirectly(final NodeAnimation nodeAnim, final float time) {
		final Node node = nodeAnim.node;
		node.isAnimated = true;
		final Transform transform = getNodeAnimationTransform(nodeAnim, time);
		transform.toMatrix4(node.localTransform);
		if (node instanceof NodePlus) {
			if (((NodePlus)node).weights != null) {
				((NodePlus)node).weights.set(transform.weights);
				for (NodePart part : node.parts) {
					((NodePartPlus)part).morphTargets.set(transform.weights);
				}
			}
		}
	}

	private final void applyNodeAnimationBlending(final NodeAnimation nodeAnim, final ObjectMap<Node, Transform> out, final Pool<Transform> pool, final float alpha,
		final float time) {

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
	protected void applyAnimationPlus(final ObjectMap<Node, Transform> out, final Pool<Transform> pool, final float alpha, final Animation animation, final float time) {

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

	public void setAnimationDesc(AnimationDesc anim) {
		setAnimation(anim.animation, anim.offset, anim.duration, anim.loopCount, anim.speed, anim.listener);
	}

	public void setAnimation(Animation animation) {
		setAnimation(animation, 1);
	}

	/** @param animation animation to play
	 * @param loopCount loop count : 0 paused, -1 infinite, n for n loops */
	public void setAnimation(Animation animation, int loopCount) {
		setAnimation(animation, 0f, animation.duration, loopCount, 1f, null); // loop count: 0 paused, -1 infinite
	}

}
