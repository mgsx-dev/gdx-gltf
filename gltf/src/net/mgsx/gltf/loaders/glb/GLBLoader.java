package net.mgsx.gltf.loaders.glb;

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
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.data.GLTFBufferView;
import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.loaders.shared.GLTFLoaderBase;
import net.mgsx.gltf.loaders.shared.data.DataResolver;
import net.mgsx.gltf.loaders.shared.texture.ImageResolver;
import net.mgsx.gltf.loaders.shared.texture.PixmapBinaryLoaderHack;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GLBLoader extends GLTFLoaderBase {

	private static class BinaryImageResolver extends ImageResolver
	{
		private DataResolver dataResolver;
		
		public BinaryImageResolver(DataResolver dataResolver) {
			super();
			this.dataResolver = dataResolver;
		}

		@Override
		protected Pixmap load(GLTFImage glImage) {
			if(glImage.bufferView != null){
				GLTFBufferView bufferView = dataResolver.getBufferView(glImage.bufferView);
				ByteBuffer buffer = dataResolver.getBufferByte(bufferView);
				byte [] data = new byte[bufferView.byteLength];
				buffer.get(data);
				return PixmapBinaryLoaderHack.load(data, 0, data.length);
			}else{
				throw new GdxRuntimeException("GLB image should have bufferView");
			}
		}
	}
	
	public SceneAsset load(FileHandle file){
		return load(file.read());
	}
	
	public SceneAsset load(InputStream stream) {
		return load(new LittleEndianInputStream(stream));
	}
	
	public SceneAsset load(LittleEndianInputStream stream) {
		try {
			ObjectMap<Integer, ByteBuffer> bufferMap = new ObjectMap<Integer, ByteBuffer>();
			glModel = loadInternal(stream, bufferMap);
			dataResolver = new DataResolver(glModel, bufferMap);
			imageResolver = new BinaryImageResolver(dataResolver);
			return loadInternal();
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		}
	}
	
	public SceneAsset load(byte[] bytes) {
		return load(new ByteArrayInputStream(bytes));
	}
	
	private GLTF loadInternal(LittleEndianInputStream stream, ObjectMap<Integer, ByteBuffer> bufferMap) throws IOException {
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

}
