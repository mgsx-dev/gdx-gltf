package net.mgsx.gltf.data;

import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.data.animation.GLTFAnimation;
import net.mgsx.gltf.data.camera.GLTFCamera;
import net.mgsx.gltf.data.data.GLTFAccessor;
import net.mgsx.gltf.data.data.GLTFBuffer;
import net.mgsx.gltf.data.data.GLTFBufferView;
import net.mgsx.gltf.data.geometry.GLTFMesh;
import net.mgsx.gltf.data.material.GLTFMaterial;
import net.mgsx.gltf.data.scene.GLTFNode;
import net.mgsx.gltf.data.scene.GLTFScene;
import net.mgsx.gltf.data.scene.GLTFSkin;
import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.data.texture.GLTFSampler;
import net.mgsx.gltf.data.texture.GLTFTexture;

public class GLTF extends GLTFObject {
	public GLTFAsset asset;
	public int scene;
	public Array<GLTFScene> scenes;
	public Array<GLTFNode> nodes;
	public Array<GLTFCamera> cameras;
	public Array<GLTFMesh> meshes;
	
	public Array<GLTFImage> images;
	public Array<GLTFSampler> samplers;
	public Array<GLTFTexture> textures;
	
	public Array<GLTFAnimation> animations;
	public Array<GLTFSkin> skins;

	public Array<GLTFAccessor> accessors;
	public Array<GLTFMaterial> materials;
	public Array<GLTFBufferView> bufferViews;
	public Array<GLTFBuffer> buffers;
	
	public Array<String> extensionsUsed;
	public Array<String> extensionsRequired;
}
