package net.mgsx.gltf.data.scene;

import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.data.GLTFEntity;

public class GLTFSkin extends GLTFEntity{
	public int inverseBindMatrices;
	public Array<Integer> joints;
	public int skeleton;
}
