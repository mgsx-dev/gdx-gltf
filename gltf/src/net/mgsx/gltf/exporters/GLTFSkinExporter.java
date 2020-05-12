package net.mgsx.gltf.exporters;

import java.nio.FloatBuffer;

import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gltf.data.data.GLTFAccessor;
import net.mgsx.gltf.data.scene.GLTFNode;
import net.mgsx.gltf.data.scene.GLTFSkin;
import net.mgsx.gltf.loaders.shared.GLTFTypes;

class GLTFSkinExporter {
	private final GLTFExporter base;

	public GLTFSkinExporter(GLTFExporter base) {
		super();
		this.base = base;
	}
	
	public void export() 
	{
		// note that node.skeleton is not mandatory, it's set on root node of armature
		
		for(int i=0 ; i<base.nodeMapping.size ; i++){
			Node node = base.nodeMapping.get(i);
			GLTFNode glNode = base.root.nodes.get(i);
			
			// skip already exported skins (in case of multiple scene)
			if(glNode.skin != null) continue;
			
			if(node.parts != null){
				for(NodePart part : node.parts){
					if(part.invBoneBindTransforms != null){
						// here we can create a new skin
						GLTFSkin skin = new GLTFSkin();
						if(base.root.skins == null) base.root.skins = new Array<GLTFSkin>();
						base.root.skins.add(skin);
						glNode.skin = base.root.skins.size-1;
						
						skin.joints = new Array<Integer>();
						
						FloatBuffer matrixBuffer = base.binManager.beginFloats(part.invBoneBindTransforms.size * 16);
						
						for(Entry<Node, Matrix4> e : part.invBoneBindTransforms){
							int boneID = base.nodeMapping.indexOf(e.key, true);
							skin.joints.add(boneID);
							matrixBuffer.put(e.value.val);
						}
						GLTFAccessor accessor = base.obtainAccessor();
						accessor.bufferView = base.binManager.end();
						accessor.type = GLTFTypes.TYPE_MAT4;
						accessor.componentType = GLTFTypes.C_FLOAT;
						accessor.count = part.invBoneBindTransforms.size;
						
						skin.inverseBindMatrices = base.root.accessors.size-1;
					}
				}
			}
			
		}
	}
}
