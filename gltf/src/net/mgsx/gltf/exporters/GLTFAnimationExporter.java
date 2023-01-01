package net.mgsx.gltf.exporters;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import net.mgsx.gltf.data.animation.GLTFAnimation;
import net.mgsx.gltf.data.animation.GLTFAnimationChannel;
import net.mgsx.gltf.data.animation.GLTFAnimationSampler;
import net.mgsx.gltf.data.animation.GLTFAnimationTarget;
import net.mgsx.gltf.data.data.GLTFAccessor;
import net.mgsx.gltf.loaders.exceptions.GLTFIllegalException;
import net.mgsx.gltf.loaders.shared.GLTFTypes;
import net.mgsx.gltf.loaders.shared.animation.Interpolation;
import net.mgsx.gltf.scene3d.animation.NodeAnimationHack;
import net.mgsx.gltf.scene3d.model.CubicQuaternion;
import net.mgsx.gltf.scene3d.model.CubicVector3;
import net.mgsx.gltf.scene3d.model.CubicWeightVector;
import net.mgsx.gltf.scene3d.model.WeightVector;

import java.nio.FloatBuffer;

class GLTFAnimationExporter {
  private final GLTFExporter base;

  abstract static class ChannelExporter<T> {
    private final int numComponents;
    private final String outputType;
    private final int numElements;

    public ChannelExporter(int numComponents, String outputType) {
      this(numComponents, outputType, 1);
    }

    public ChannelExporter(int numComponents, String outputType, int numElements) {
      super();
      this.numComponents = numComponents;
      this.outputType = outputType;
      this.numElements = numElements;
    }

    private void export(GLTFExporter base, GLTFAnimation a, int nodeID, Array<NodeKeyframe<T>> keyFrames, String chanName, Interpolation interpolation) {
      if (a.channels == null) {
        a.channels = new Array<>();
      }
      GLTFAnimationChannel chan = new GLTFAnimationChannel();
      a.channels.add(chan);

      if (a.samplers == null) {
        a.samplers = new Array<>();
      }
      GLTFAnimationSampler sampler = new GLTFAnimationSampler();
      a.samplers.add(sampler);

      chan.sampler = a.samplers.size - 1;
      sampler.interpolation = mapInterpolation(interpolation);

      chan.target = new GLTFAnimationTarget();
      chan.target.node = nodeID;
      chan.target.path = chanName;
      int numKeyframes = keyFrames.size;
      float[] inputs = new float[numKeyframes];
      boolean cubic = interpolation == Interpolation.CUBICSPLINE;
      int interpolationFactor = cubic ? 3 : 1;
      int outputCount = numKeyframes * interpolationFactor * numElements;
      int outputFloats = outputCount * numComponents;
      FloatBuffer outputs = base.binManager.beginFloats(outputFloats);
      for (int i = 0; i < numKeyframes; i++) {
        NodeKeyframe<T> kf = keyFrames.get(i);
        inputs[i] = kf.keytime;
        getOutput(outputs, kf.value);
      }
      GLTFAccessor outputAccessor = base.obtainAccessor();
      outputAccessor.bufferView = base.binManager.end();
      outputAccessor.componentType = GLTFTypes.C_FLOAT;
      outputAccessor.count = outputCount;
      outputAccessor.type = outputType;
      sampler.output = base.root.accessors.size - 1;

      FloatBuffer inputBuffer = base.binManager.beginFloats(numKeyframes);
      inputBuffer.put(inputs);

      GLTFAccessor inputAccessor = base.obtainAccessor();
      inputAccessor.componentType = GLTFTypes.C_FLOAT;
      inputAccessor.count = numKeyframes;
      inputAccessor.type = GLTFTypes.TYPE_SCALAR;
      inputAccessor.bufferView = base.binManager.end();
      // min max are mandatory for sampler inputs
      inputAccessor.min = new float[]{inputs[0]};
      inputAccessor.max = new float[]{inputs[inputs.length - 1]};
      sampler.input = base.root.accessors.size - 1;
    }

    protected abstract void getOutput(FloatBuffer outputs, T value);
  }

  private static final ChannelExporter<Vector3> channelExporterVector3 = new ChannelExporter<Vector3>(3, GLTFTypes.TYPE_VEC3) {
    @Override
    protected void getOutput(FloatBuffer outputs, Vector3 value) {
      if (value instanceof CubicVector3) {
        CubicVector3 cubic = (CubicVector3) value;
        outputs.put(cubic.tangentIn.x);
        outputs.put(cubic.tangentIn.y);
        outputs.put(cubic.tangentIn.z);
        outputs.put(value.x);
        outputs.put(value.y);
        outputs.put(value.z);
        outputs.put(cubic.tangentOut.x);
        outputs.put(cubic.tangentOut.y);
        outputs.put(cubic.tangentOut.z);
      } else {
        outputs.put(value.x);
        outputs.put(value.y);
        outputs.put(value.z);
      }
    }
  };

  private static final ChannelExporter<Quaternion> channelExporterQuaternion = new ChannelExporter<Quaternion>(4, GLTFTypes.TYPE_VEC4) {
    @Override
    protected void getOutput(FloatBuffer outputs, Quaternion value) {
      if (value instanceof CubicQuaternion) {
        CubicQuaternion cubic = (CubicQuaternion) value;
        outputs.put(cubic.tangentIn.x);
        outputs.put(cubic.tangentIn.y);
        outputs.put(cubic.tangentIn.z);
        outputs.put(cubic.tangentIn.w);
        outputs.put(value.x);
        outputs.put(value.y);
        outputs.put(value.z);
        outputs.put(value.w);
        outputs.put(cubic.tangentOut.x);
        outputs.put(cubic.tangentOut.y);
        outputs.put(cubic.tangentOut.z);
        outputs.put(cubic.tangentOut.w);
      } else {
        outputs.put(value.x);
        outputs.put(value.y);
        outputs.put(value.z);
        outputs.put(value.w);
      }
    }
  };

  private static ChannelExporter<WeightVector> channelExporterWeights(int count) {
    return new ChannelExporter<WeightVector>(1, GLTFTypes.TYPE_SCALAR, count) {
      @Override
      protected void getOutput(FloatBuffer outputs, WeightVector value) {
        if (value instanceof CubicWeightVector) {
          /** https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#animations end of chapter :
           * When used with CUBICSPLINE interpolation, tangents (ak, bk) and values (vk) are grouped within keyframes:
           * a1,a2,...an,v1,v2,...vn,b1,b2,...bn
           *  */
          CubicWeightVector cubic = (CubicWeightVector) value;
          outputs.put(cubic.tangentIn.values, 0, value.count);
          outputs.put(value.values, 0, value.count);
          outputs.put(cubic.tangentOut.values, 0, value.count);
        } else {
          outputs.put(value.values, 0, value.count);
        }
      }
    };
  }

  public GLTFAnimationExporter(GLTFExporter base) {
    super();
    this.base = base;
  }

  public void export(Array<Animation> animations) {
    for (Animation animation : animations) {
      export(animation);
    }
  }

  private void export(Animation animation) {
    GLTFAnimation a = new GLTFAnimation();
    a.name = animation.id;
    if (base.root.animations == null) {
      base.root.animations = new Array<>();
    }
    base.root.animations.add(a);

    for (NodeAnimation nodeAnim : animation.nodeAnimations) {
      int nodeID = base.nodeMapping.indexOf(nodeAnim.node, true);

      if (nodeAnim.translation != null) {
        channelExporterVector3.export(base, a, nodeID, nodeAnim.translation, "translation", translationInterpolation(nodeAnim));
      }
      if (nodeAnim.rotation != null) {
        channelExporterQuaternion.export(base, a, nodeID, nodeAnim.rotation, "rotation", rotationInterpolation(nodeAnim));
      }
      if (nodeAnim.scaling != null) {
        channelExporterVector3.export(base, a, nodeID, nodeAnim.scaling, "scale", scaleInterpolation(nodeAnim));
      }
      if (nodeAnim instanceof NodeAnimationHack) {
        NodeAnimationHack nodeAnimMorph = (NodeAnimationHack) nodeAnim;
        if (nodeAnimMorph.weights != null) {
          int count = nodeAnimMorph.weights.first().value.count;
          channelExporterWeights(count).export(base, a, nodeID, nodeAnimMorph.weights, "weights", nodeAnimMorph.weightsMode);
        }
      }
    }
  }

  private Interpolation translationInterpolation(NodeAnimation nodeAnim) {
    if (nodeAnim instanceof NodeAnimationHack) {
      return ((NodeAnimationHack) nodeAnim).translationMode;
    }
    return null;
  }

  private Interpolation rotationInterpolation(NodeAnimation nodeAnim) {
    if (nodeAnim instanceof NodeAnimationHack) {
      return ((NodeAnimationHack) nodeAnim).rotationMode;
    }
    return null;
  }

  private Interpolation scaleInterpolation(NodeAnimation nodeAnim) {
    if (nodeAnim instanceof NodeAnimationHack) {
      return ((NodeAnimationHack) nodeAnim).scalingMode;
    }
    return null;
  }

  public static String mapInterpolation(Interpolation type) {
    if (type == null || type == Interpolation.LINEAR) return null; // default "LINEAR";
    if (type == Interpolation.STEP) return "STEP";
    if (type == Interpolation.CUBICSPLINE) return "CUBICSPLINE";
    throw new GLTFIllegalException("unexpected interpolation type " + type);
  }
}
