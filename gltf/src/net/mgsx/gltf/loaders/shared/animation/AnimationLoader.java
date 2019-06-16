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
import net.mgsx.gltf.loaders.shared.GLTFLoaderBase;
import net.mgsx.gltf.loaders.shared.GLTFTypes;
import net.mgsx.gltf.loaders.shared.data.DataResolver;
import net.mgsx.gltf.loaders.shared.scene.NodeResolver;
import net.mgsx.gltf.scene3d.animation.NodeAnimationHack;
import net.mgsx.gltf.scene3d.model.NodePlus;
import net.mgsx.gltf.scene3d.model.WeightVector;

public class AnimationLoader {
	
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
			
			Interpolation interpolation = GLTFTypes.mapInterpolation(glSampler.interpolation);
			// TODO store interpolation on AnimationNode (for individual channels) and use it in animations ...
			if(interpolation != Interpolation.LINEAR){
				if(GLTFLoaderBase.FAIL_NOT_IMPLEMENTED)
					throw new GdxRuntimeException("unsupported interpolation " + interpolation);
				else
					Gdx.app.error("GLTF", "unsupported interpolation " + interpolation + ", process as LINEAR");
			}
			
			// case of cubic spline, we skip anchor vectors
			int dataOffset = 0;
			int dataStride = 1;
			if(interpolation == Interpolation.CUBICSPLINE){
				dataOffset = 1;
				dataStride = 3;
			}
			
			GLTFAccessor inputAccessor = dataResolver.getAccessor(glSampler.input);
			animation.duration = Math.max(animation.duration, inputAccessor.max[0]);
			
			String property = glChannel.target.path;
			if("translation".equals(property)){
				nodeAnimation.translation = new Array<NodeKeyframe<Vector3>>();
				// copy first frame if not at zero time
				if(inputData[0] > 0){
					nodeAnimation.translation.add(new NodeKeyframe<Vector3>(0, GLTFTypes.map(new Vector3(), outputData, dataOffset * 3)));
				}
				for(int k=0 ; k<inputData.length ; k++){
					nodeAnimation.translation.add(new NodeKeyframe<Vector3>(inputData[k], GLTFTypes.map(new Vector3(), outputData, (dataOffset+(k*dataStride))*3)));
				}
			}else if("rotation".equals(property)){
				nodeAnimation.rotation = new Array<NodeKeyframe<Quaternion>>();
				// copy first frame if not at zero time
				if(inputData[0] > 0){
					nodeAnimation.rotation.add(new NodeKeyframe<Quaternion>(0, GLTFTypes.map(new Quaternion(), outputData, dataOffset * 4)));
				}
				for(int k=0 ; k<inputData.length ; k++){
					nodeAnimation.rotation.add(new NodeKeyframe<Quaternion>(inputData[k], GLTFTypes.map(new Quaternion(), outputData, (dataOffset+(k*dataStride))*4)));
				}
			}else if("scale".equals(property)){
				nodeAnimation.scaling = new Array<NodeKeyframe<Vector3>>();
				// copy first frame if not at zero time
				if(inputData[0] > 0){
					nodeAnimation.scaling.add(new NodeKeyframe<Vector3>(0, GLTFTypes.map(new Vector3(), outputData, dataOffset * 3)));
				}
				for(int k=0 ; k<inputData.length ; k++){
					nodeAnimation.scaling.add(new NodeKeyframe<Vector3>(inputData[k], GLTFTypes.map(new Vector3(), outputData, (dataOffset+(k*dataStride))*3)));
				}
			}else if("weights".equals(property)){
				NodeAnimationHack np = (NodeAnimationHack)nodeAnimation;
				int nbWeights = ((NodePlus)node).weights.count;
				np.weights = new Array<NodeKeyframe<WeightVector>>();
				// copy first frame if not at zero time
				if(inputData[0] > 0){
					np.weights.add(new NodeKeyframe<WeightVector>(0, GLTFTypes.map(new WeightVector(nbWeights), outputData, dataOffset * nbWeights)));
				}
				for(int k=0 ; k<inputData.length ; k++){
					np.weights.add(new NodeKeyframe<WeightVector>(inputData[k], GLTFTypes.map(new WeightVector(nbWeights), outputData, (dataOffset+(k*dataStride))*nbWeights)));
				}
			}else{
				throw new GdxRuntimeException("unsupported " + property);
			}
		}
		
		return animation;
	}
	
}
