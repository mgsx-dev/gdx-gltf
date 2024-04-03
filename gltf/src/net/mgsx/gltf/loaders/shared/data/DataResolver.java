package net.mgsx.gltf.loaders.shared.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.data.GLTFAccessor;
import net.mgsx.gltf.data.data.GLTFBufferView;
import net.mgsx.gltf.loaders.exceptions.GLTFUnsupportedException;
import net.mgsx.gltf.loaders.shared.GLTFTypes;

public class DataResolver {
	
	private GLTF glModel;
	private DataFileResolver dataFileResolver;
	
	public DataResolver(GLTF glModel, DataFileResolver dataFileResolver) {
		super();
		this.glModel = glModel;
		this.dataFileResolver = dataFileResolver;
	}
	
	public GLTFAccessor getAccessor(int accessorID) {
		return glModel.accessors.get(accessorID);
	}

	public float[] readBufferFloat(int accessorID) {
		GLTFAccessor accessor = glModel.accessors.get(accessorID);
		AccessorBuffer accessorBuffer = getAccessorBuffer(accessor);
		ByteBuffer bytes = accessorBuffer.prepareForReading();
		float [] data = new float[GLTFTypes.accessorSize(accessor)/4];

		int nbFloatsPerVertex = GLTFTypes.accessorTypeSize(accessor);
		int nbBytesToSkip = accessorBuffer.getByteStride() - nbFloatsPerVertex * 4;
		if(nbBytesToSkip == 0){
			bytes.asFloatBuffer().get(data);
		}else{
			for(int i=0 ; i<accessor.count ; i++){
				for(int j=0 ; j<nbFloatsPerVertex ; j++){
					data[i*nbFloatsPerVertex+j] = bytes.getFloat();
				}
				// skip remaining bytes
				bytes.position(bytes.position() + nbBytesToSkip);
			}
		}
		return data;
	}
	
	public int[] readBufferUByte(int accessorID) {
		GLTFAccessor accessor = glModel.accessors.get(accessorID);
		AccessorBuffer accessorBuffer = getAccessorBuffer(accessor);
		ByteBuffer bytes = accessorBuffer.prepareForReading();
		int [] data = new int[GLTFTypes.accessorSize(accessor)];
		
		int nbBytesPerVertex = GLTFTypes.accessorTypeSize(accessor);
		int nbBytesToSkip = accessorBuffer.getByteStride() - nbBytesPerVertex;
		if(nbBytesToSkip == 0){
			for(int i=0 ; i<data.length ; i++){
				data[i] = bytes.get() & 0xFF;
			}
		}else{
			for(int i=0 ; i<accessor.count ; i++){
				for(int j=0 ; j<nbBytesPerVertex ; j++){
					data[i*nbBytesPerVertex+j] = bytes.get() & 0xFF;
				}
				// skip remaining bytes
				bytes.position(bytes.position() + nbBytesToSkip);
			}
		}
		return data;
	}
	
	public int[] readBufferUShort(int accessorID) {
		GLTFAccessor accessor = glModel.accessors.get(accessorID);
		AccessorBuffer accessorBuffer = getAccessorBuffer(accessor);
		ByteBuffer bytes = accessorBuffer.prepareForReading();
		int [] data = new int[GLTFTypes.accessorSize(accessor)/2];
		
		int nbShortsPerVertex = GLTFTypes.accessorTypeSize(accessor);
		int nbBytesToSkip = accessorBuffer.getByteStride() - nbShortsPerVertex * 2;
		if(nbBytesToSkip == 0){
			ShortBuffer shorts = bytes.asShortBuffer();
			for(int i=0 ; i<data.length ; i++){
				data[i] = shorts.get() & 0xFFFF;
			}
		}else{
			for(int i=0 ; i<accessor.count ; i++){
				for(int j=0 ; j<nbShortsPerVertex ; j++){
					data[i*nbShortsPerVertex+j] = bytes.getShort() & 0xFFFF;
				}
				// skip remaining bytes
				bytes.position(bytes.position() + nbBytesToSkip);
			}
		}
		return data;
	}
	
	public float[] readBufferUShortAsFloat(int accessorID) {
		int[] intBuffer = readBufferUShort(accessorID);
		float[] floatBuffer = new float[intBuffer.length];
		for (int i = 0; i < intBuffer.length; i++) {
			floatBuffer[i] = intBuffer[i] / 65535f;
		}
		return floatBuffer;
	}

	public float[] readBufferUByteAsFloat(int accessorID) {
		int[] intBuffer = readBufferUByte(accessorID);
		float[] floatBuffer = new float[intBuffer.length];
		for (int i = 0; i < intBuffer.length; i++) {
			floatBuffer[i] = intBuffer[i] / 255f;
		}
		return floatBuffer;
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
		AccessorBuffer buffer = getAccessorBuffer(glAccessor);
		return buffer.prepareForReading();
	}

	public AccessorBuffer getAccessorBuffer(GLTFAccessor glAccessor) {
		AccessorBuffer buffer;
		if (glAccessor.bufferView != null) {
			GLTFBufferView bufferView = glModel.bufferViews.get(glAccessor.bufferView);
			buffer = AccessorBuffer.fromBufferView(glAccessor, bufferView, dataFileResolver);
		} else {
			buffer = AccessorBuffer.fromZeros(glAccessor);
		}
		if (glAccessor.sparse != null) {
			buffer.prepareForWriting();
			patchSparseValues(glAccessor, buffer);
		}
		buffer.prepareForReading();
		return buffer;
	}

	private void patchSparseValues(GLTFAccessor glAccessor, AccessorBuffer outputBuffer) {
		GLTFBufferView indicesBufferView = getBufferView(glAccessor.sparse.indices.bufferView);
		ByteBuffer indicesBuffer = dataFileResolver.getBuffer(indicesBufferView.buffer).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
		indicesBuffer.position(glAccessor.sparse.indices.byteOffset + indicesBufferView.byteOffset);
		GLTFBufferView replacementValueBufferView = getBufferView(glAccessor.sparse.values.bufferView);
		ByteBuffer replacementValuesBuffer = dataFileResolver.getBuffer(replacementValueBufferView.buffer).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
		replacementValuesBuffer.position(glAccessor.sparse.values.byteOffset + replacementValueBufferView.byteOffset);
		int bytesPerValue = GLTFTypes.accessorStrideSize(glAccessor);
		byte[] replacementValueBytes = new byte[bytesPerValue];
		for (int i = 0; i < glAccessor.sparse.count; i++) {
			int indexToReplace;
			switch (glAccessor.sparse.indices.componentType) {
				case GLTFTypes.C_UBYTE:
					indexToReplace = ((int) indicesBuffer.get()) & 0xff;
					break;
				case GLTFTypes.C_USHORT:
					indexToReplace = ((int) indicesBuffer.getShort()) & 0xffff;
					break;
				case GLTFTypes.C_UINT: {
					// java does not have uint, so read as signed long
					long asLong = ((long) indicesBuffer.getInt()) & 0xffffffffL;
					if (asLong > Integer.MAX_VALUE) {
						throw new GLTFUnsupportedException("very large indices can not be parsed");
					}
					indexToReplace = (int) asLong;
					break;
				}
				default:
					throw new GLTFUnsupportedException("unsupported indices type");
			}
			replacementValuesBuffer.get(replacementValueBytes);
			ByteBuffer data = outputBuffer.getData();
			int elementOffset = indexToReplace * outputBuffer.getByteStride();
			data.position(outputBuffer.getByteOffset() + elementOffset);
			data.put(replacementValueBytes);
		}
	}

	public ByteBuffer getBufferByte(GLTFBufferView bufferView) {
		ByteBuffer bytes = dataFileResolver.getBuffer(bufferView.buffer);
		bytes.position(bufferView.byteOffset);
		return bytes;
	}
}
