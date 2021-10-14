package net.mgsx.gltf.loaders.shared.scene;

import java.nio.FloatBuffer;

import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

import net.mgsx.gltf.data.scene.GLTFNode;
import net.mgsx.gltf.data.scene.GLTFSkin;
import net.mgsx.gltf.loaders.exceptions.GLTFIllegalException;
import net.mgsx.gltf.loaders.shared.data.DataResolver;

public class SkinLoader {
	
	public void load(Array<GLTFSkin> glSkins, Array<GLTFNode> glNodes, NodeResolver nodeResolver, DataResolver dataResolver) {
		if(glNodes != null){
			for(int i=0 ; i<glNodes.size ; i++){
				GLTFNode glNode = glNodes.get(i);
				if(glNode.skin != null){
					GLTFSkin glSkin = glSkins.get(glNode.skin);
					load(glSkin, glNode, nodeResolver.get(i), nodeResolver, dataResolver);
				}
			}
		}
	}

	private void load(GLTFSkin glSkin, GLTFNode glNode, Node node, NodeResolver nodeResolver, DataResolver dataResolver){
		
		Array<Matrix4> ibms = new Array<Matrix4>();
		Array<Integer> joints = new Array<Integer>();
		
		int bonesCount = glSkin.joints.size;
		
		FloatBuffer floatBuffer = dataResolver.getBufferFloat(glSkin.inverseBindMatrices);
		
		for(int i=0 ; i<bonesCount ; i++){
			float [] matrixData = new float[16];
			floatBuffer.get(matrixData);
			ibms.add(new Matrix4(matrixData));
		}
		joints.addAll(glSkin.joints);
		
		if(ibms.size > 0){
			for(NodePart nodePart : node.parts){
				nodePart.bones = new Matrix4[ibms.size];
				nodePart.invBoneBindTransforms = new ArrayMap<Node, Matrix4>();
				for(int n=0 ; n<joints.size ; n++){
					nodePart.bones[n] = new Matrix4().idt();
					int nodeIndex = joints.get(n);
					Node key = nodeResolver.get(nodeIndex);
					if(key == null) throw new GLTFIllegalException("node not found for bone: " + nodeIndex);
					nodePart.invBoneBindTransforms.put(key, ibms.get(n));
				}
			}
		}
	}

	
}
