package net.mgsx.gltf.data.animation;

import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.data.GLTFEntity;

public class GLTFAnimation extends GLTFEntity {
	public Array<GLTFAnimationChannel> channels;
	public Array<GLTFAnimationSampler> samplers;
}
