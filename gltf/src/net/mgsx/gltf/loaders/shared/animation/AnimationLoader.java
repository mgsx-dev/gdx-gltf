package net.mgsx.gltf.loaders.shared.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gltf.data.animation.GLTFAnimation;
import net.mgsx.gltf.data.animation.GLTFAnimationChannel;
import net.mgsx.gltf.data.animation.GLTFAnimationSampler;
import net.mgsx.gltf.data.data.GLTFAccessor;
import net.mgsx.gltf.loaders.shared.GLTFTypes;
import net.mgsx.gltf.loaders.shared.data.DataResolver;
import net.mgsx.gltf.loaders.shared.scene.NodeResolver;
import net.mgsx.gltf.scene3d.animation.NodeAnimationHack;
import net.mgsx.gltf.scene3d.model.CubicQuaternion;
import net.mgsx.gltf.scene3d.model.CubicVector3;
import net.mgsx.gltf.scene3d.model.CubicWeightVector;
import net.mgsx.gltf.scene3d.model.NodePlus;
import net.mgsx.gltf.scene3d.model.WeightVector;

public class AnimationLoader {
	
	public static boolean useCubicIntepolation = false;
	public static boolean useStepIntepolation = false;
	
	public final Array<Animation> animations = new Array<Animation>();
	
	public void load(Array<GLTFAnimation> glAnimations, NodeResolver nodeResolver, DataResolver dataResolver) {
		
		if(glAnimations != null){
			for(int i=0 ; i<glAnimations.size ; i++){
				GLTFAnimation glAnimation = glAnimations.get(i);
				
				Animation animation = load(glAnimation, nodeResolver, dataResolver);
				animation.id = glAnimation.name == null ? "animation" + i : glAnimation.name;
				
				animations.add(animation);
			}
		}
	}
	
	private Animation load(GLTFAnimation glAnimation, NodeResolver nodeResolver, DataResolver dataResolver){
		
		ObjectMap<Node, NodeAnimation> animMap = new ObjectMap<Node, NodeAnimation>();
		
		Animation animation = new Animation();
		
		for(GLTFAnimationChannel glChannel : glAnimation.channels){
			GLTFAnimationSampler glSampler = glAnimation.samplers.get(glChannel.sampler);
			Node node = nodeResolver.get(glChannel.target.node);
			
			NodeAnimation nodeAnimation = animMap.get(node);
			if(nodeAnimation == null){
				nodeAnimation = new NodeAnimationHack();
				nodeAnimation.node = node;
				animMap.put(node, nodeAnimation);
				animation.nodeAnimations.add(nodeAnimation);
			}
			
			float[] inputData = dataResolver.readBufferFloat(glSampler.input);
			float[] outputData = dataResolver.readBufferFloat(glSampler.output);

			Interpolation originalInterpolation = GLTFTypes.mapInterpolation(glSampler.interpolation);
			
			// case of cubic spline, we skip anchor vectors if cubic is disabled.
			int dataOffset = 0;
			int dataStride = 1;
			if(originalInterpolation == Interpolation.CUBICSPLINE){
				dataOffset = 1;
				dataStride = 3;
			}
			
			// change interpolation depending on configuration
			Interpolation interpolation = originalInterpolation;
			if(originalInterpolation == Interpolation.CUBICSPLINE){
				if(!useCubicIntepolation){
					Gdx.app.log("GLTF", "interpolation " + originalInterpolation + ", processed as LINEAR");
					interpolation = Interpolation.LINEAR;
				}
			}else if(originalInterpolation == Interpolation.STEP){
				if(!useStepIntepolation){
					Gdx.app.log("GLTF", "interpolation " + originalInterpolation + ", processed as LINEAR");
					interpolation = Interpolation.LINEAR;
				}
			}
			
			GLTFAccessor inputAccessor = dataResolver.getAccessor(glSampler.input);
			animation.duration = Math.max(animation.duration, inputAccessor.max[0]);
			
			String property = glChannel.target.path;
			if("translation".equals(property)){
				
				((NodeAnimationHack)nodeAnimation).translationMode = interpolation;
				
				nodeAnimation.translation = new Array<NodeKeyframe<Vector3>>();
				if(interpolation == Interpolation.CUBICSPLINE){
					// copy first frame if not at zero time
					if(inputData[0] > 0){
						nodeAnimation.translation.add(new NodeKeyframe<Vector3>(0, GLTFTypes.map(new CubicVector3(), outputData, 0)));
					}
					for(int k=0 ; k<inputData.length ; k++){
						nodeAnimation.translation.add(new NodeKeyframe<Vector3>(inputData[k], GLTFTypes.map(new CubicVector3(), outputData, k*dataStride*3)));
					}
				}else{
					// copy first frame if not at zero time
					if(inputData[0] > 0){
						nodeAnimation.translation.add(new NodeKeyframe<Vector3>(0, GLTFTypes.map(new Vector3(), outputData, dataOffset * 3)));
					}
					for(int k=0 ; k<inputData.length ; k++){
						nodeAnimation.translation.add(new NodeKeyframe<Vector3>(inputData[k], GLTFTypes.map(new Vector3(), outputData, (dataOffset+(k*dataStride))*3)));
					}
				}
			}else if("rotation".equals(property)){
				
				((NodeAnimationHack)nodeAnimation).rotationMode = interpolation;
				
				nodeAnimation.rotation = new Array<NodeKeyframe<Quaternion>>();
				if(interpolation == Interpolation.CUBICSPLINE){
					// copy first frame if not at zero time
					if(inputData[0] > 0){
						nodeAnimation.rotation.add(new NodeKeyframe<Quaternion>(0, GLTFTypes.map(new CubicQuaternion(), outputData, 0)));
					}
					for(int k=0 ; k<inputData.length ; k++){
						nodeAnimation.rotation.add(new NodeKeyframe<Quaternion>(inputData[k], GLTFTypes.map(new CubicQuaternion(), outputData, k*dataStride*4)));
					}
				}else{
					// copy first frame if not at zero time
					if(inputData[0] > 0){
						nodeAnimation.rotation.add(new NodeKeyframe<Quaternion>(0, GLTFTypes.map(new Quaternion(), outputData, dataOffset * 4)));
					}
					for(int k=0 ; k<inputData.length ; k++){
						nodeAnimation.rotation.add(new NodeKeyframe<Quaternion>(inputData[k], GLTFTypes.map(new Quaternion(), outputData, (dataOffset+(k*dataStride))*4)));
					}
				}
			}else if("scale".equals(property)){
				
				((NodeAnimationHack)nodeAnimation).scalingMode = interpolation;
				
				nodeAnimation.scaling = new Array<NodeKeyframe<Vector3>>();
				if(interpolation == Interpolation.CUBICSPLINE){
					// copy first frame if not at zero time
					if(inputData[0] > 0){
						nodeAnimation.scaling.add(new NodeKeyframe<Vector3>(0, GLTFTypes.map(new CubicVector3(), outputData, 0)));
					}
					for(int k=0 ; k<inputData.length ; k++){
						nodeAnimation.scaling.add(new NodeKeyframe<Vector3>(inputData[k], GLTFTypes.map(new CubicVector3(), outputData, k*dataStride*3)));
					}
				}else{
					// copy first frame if not at zero time
					if(inputData[0] > 0){
						nodeAnimation.scaling.add(new NodeKeyframe<Vector3>(0, GLTFTypes.map(new Vector3(), outputData, dataOffset * 3)));
					}
					for(int k=0 ; k<inputData.length ; k++){
						nodeAnimation.scaling.add(new NodeKeyframe<Vector3>(inputData[k], GLTFTypes.map(new Vector3(), outputData, (dataOffset+(k*dataStride))*3)));
					}
				}
			}else if("weights".equals(property)){
				
				((NodeAnimationHack)nodeAnimation).weightsMode = interpolation;
				
				NodeAnimationHack np = (NodeAnimationHack)nodeAnimation;
				int nbWeights = ((NodePlus)node).weights.count;
				np.weights = new Array<NodeKeyframe<WeightVector>>();
				if(interpolation == Interpolation.CUBICSPLINE){
					// copy first frame if not at zero time
					if(inputData[0] > 0){
						np.weights.add(new NodeKeyframe<WeightVector>(0, GLTFTypes.map(new CubicWeightVector(nbWeights), outputData, 0)));
					}
					for(int k=0 ; k<inputData.length ; k++){
						np.weights.add(new NodeKeyframe<WeightVector>(inputData[k], GLTFTypes.map(new CubicWeightVector(nbWeights), outputData, k*dataStride*nbWeights)));
					}
				}else{
					// copy first frame if not at zero time
					if(inputData[0] > 0){
						np.weights.add(new NodeKeyframe<WeightVector>(0, GLTFTypes.map(new WeightVector(nbWeights), outputData, dataOffset * nbWeights)));
					}
					for(int k=0 ; k<inputData.length ; k++){
						np.weights.add(new NodeKeyframe<WeightVector>(inputData[k], GLTFTypes.map(new WeightVector(nbWeights), outputData, (dataOffset+(k*dataStride))*nbWeights)));
					}
				}
			}else{
				throw new GdxRuntimeException("unsupported " + property);
			}
		}
		
		return animation;
	}
	
}
