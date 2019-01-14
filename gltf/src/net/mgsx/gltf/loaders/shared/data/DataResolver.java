package net.mgsx.gltf.loaders.shared.data;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.data.GLTFAccessor;
import net.mgsx.gltf.data.data.GLTFBufferView;
import net.mgsx.gltf.loaders.shared.GLTFTypes;

public class DataResolver {
	
	private GLTF glModel;
	private ObjectMap<Integer, ByteBuffer> bufferMap;
	
	public DataResolver(GLTF glModel, ObjectMap<Integer, ByteBuffer> bufferMap) {
		super();
		this.glModel = glModel;
		this.bufferMap = bufferMap;
	}
	
	public GLTFAccessor getAccessor(int accessorID) {
		return glModel.accessors.get(accessorID);
	}

	public float[] readBufferFloat(int accessorID) {
		GLTFAccessor accessor = glModel.accessors.get(accessorID);
		FloatBuffer floatBuffer = getBufferFloat(accessorID);
		float [] data = new float[GLTFTypes.accessorSize(accessor) / 4];
		floatBuffer.get(data);
		return data;
	}
	
	public int[] readBufferUByte(int accessorID) {
		GLTFAccessor accessor = glModel.accessors.get(accessorID);
		GLTFBufferView bufferView = glModel.bufferViews.get(accessor.bufferView);
		ByteBuffer bytes = bufferMap.get(bufferView.buffer);
		bytes.position(bufferView.byteOffset + accessor.byteOffset);
		int [] data = new int[GLTFTypes.accessorSize(accessor)];
		for(int i=0 ; i<data.length ; i++){
			data[i] = bytes.get() & 0xFF;
		}
		return data;
	}
	
	public int[] readBufferUShort(int accessorID) {
		GLTFAccessor accessor = glModel.accessors.get(accessorID);
		GLTFBufferView bufferView = glModel.bufferViews.get(accessor.bufferView);
		ByteBuffer bytes = bufferMap.get(bufferView.buffer);
		bytes.position(bufferView.byteOffset + accessor.byteOffset);
		ShortBuffer shorts = bytes.asShortBuffer();
		int [] data = new int[GLTFTypes.accessorSize(accessor)/2];
		for(int i=0 ; i<data.length ; i++){
			data[i] = shorts.get() & 0xFFFF;
		}
		return data;
	}

	public FloatBuffer getBufferFloat(int accessorID) {
		return getBufferFloat(glModel.accessors.get(accessorID));
	}

	public GLTFBufferView getBufferView(int bufferViewID) {
		return glModel.bufferViews.get(bufferViewID);
	}

	public FloatBuffer getBufferFloat(GLTFAccessor glAccessor) {
		return getBufferByte(glAccessor).asFloatBuffer();
	}

	public IntBuffer getBufferInt(GLTFAccessor glAccessor) {
		return getBufferByte(glAccessor).asIntBuffer();
	}

	public ShortBuffer getBufferShort(GLTFAccessor glAccessor) {
		return getBufferByte(glAccessor).asShortBuffer();
	}

	public ByteBuffer getBufferByte(GLTFAccessor glAccessor) {
		GLTFBufferView bufferView = glModel.bufferViews.get(glAccessor.bufferView);
		ByteBuffer bytes = bufferMap.get(bufferView.buffer);
		bytes.position(bufferView.byteOffset + glAccessor.byteOffset);
		return bytes;
	}

	public ByteBuffer getBufferByte(GLTFBufferView bufferView) {
		ByteBuffer bytes = bufferMap.get(bufferView.buffer);
		bytes.position(bufferView.byteOffset);
		return bytes;
	}
}
