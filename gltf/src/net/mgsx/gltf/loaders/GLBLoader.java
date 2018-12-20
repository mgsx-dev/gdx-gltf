package net.mgsx.gltf.loaders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.LittleEndianInputStream;

import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.data.GLTFBufferView;
import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.scene3d.SceneAsset;

public class GLBLoader extends GLTFLoaderBase {

	public SceneAsset load(FileHandle file){
		return load(file.read());
	}
	
	public SceneAsset load(InputStream stream) {
		return load(new LittleEndianInputStream(stream));
	}
	
	public SceneAsset load(LittleEndianInputStream stream) {
		try {
			glModel = loadInternal(stream);
			return loadInternal();
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	public SceneAsset load(byte[] bytes) {
		return load(new ByteArrayInputStream(bytes));
	}
	
	private GLTF loadInternal(LittleEndianInputStream stream) throws IOException {
		long magic = stream.readInt(); // & 0xFFFFFFFFL;
		if(magic != 0x46546C67) throw new GdxRuntimeException("bad magic");
		int version = stream.readInt();
		if(version != 2) throw new GdxRuntimeException("bad version");
		long length = stream.readInt();// & 0xFFFFFFFFL;
		
		String jsonData = null;
		for(int i=12 ; i<length ; ){
			int chunkLen = stream.readInt();
			int chunkType = stream.readInt();
			i += 8;			// chunkLen % 4;
			if(chunkType == 0x4E4F534A){
				byte[] data = new byte[(int)chunkLen];
				stream.read(data, 0, chunkLen);
				jsonData = new String(data);
			}else if(chunkType == 0x004E4942){
				ByteBuffer bufferData = ByteBuffer.allocate(chunkLen);
				bufferData.order(ByteOrder.LITTLE_ENDIAN);
				for(int j=0 ; j<chunkLen ; j++) bufferData.put(stream.readByte()); // TODO optimize with stream copy utils ?
				//StreamUtils.copyStream(stream, bufferData, chunkLen);
				bufferData.flip();
				bufferMap.put(bufferMap.size, bufferData);
			}else{
				System.out.println("skip buffer type " + chunkType);
				if(chunkLen > 0){
					stream.skip(chunkLen);
				}
			}
			i += chunkLen;
		}
		
		return new Json().fromJson(GLTF.class, jsonData);
	}

	@Override
	protected Pixmap loadPixmap(GLTFImage glImage) {
		if(glImage.bufferView != null){
			GLTFBufferView bufferView = glModel.bufferViews.get(glImage.bufferView);
			ByteBuffer buffer = bufferMap.get(bufferView.buffer);
			byte [] data = new byte[bufferView.byteLength];
			buffer.position(bufferView.byteOffset);
			buffer.get(data);
			return new Pixmap(data, 0, data.length);
		}else{
			throw new GdxRuntimeException("GLB image should have bufferView");
		}
	}

	
}
