package net.mgsx.gltf.loaders.gltf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.data.GLTFBuffer;
import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.loaders.shared.data.DataFileResolver;
import net.mgsx.gltf.loaders.shared.texture.PixmapBinaryLoaderHack;

public class SeparatedDataFileResolver implements DataFileResolver
{
	private ObjectMap<Integer, ByteBuffer> bufferMap = new ObjectMap<Integer, ByteBuffer>();
	private GLTF glModel;
	private FileHandle path;
	
	@Override
	public void load(FileHandle file) {
		glModel = new Json().fromJson(GLTF.class, file);
		path = file.parent();
		loadBuffers(path);
	}

	@Override
	public GLTF getRoot() {
		return glModel;
	}
	
	private ObjectMap<Integer, ByteBuffer> loadBuffers(FileHandle path) {
		
		if(glModel.buffers != null){
			for(int i=0 ; i<glModel.buffers.size ; i++){
				GLTFBuffer glBuffer = glModel.buffers.get(i);
				ByteBuffer buffer = ByteBuffer.allocate(glBuffer.byteLength);
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				if(glBuffer.uri.startsWith("data:")){
					// data:application/octet-stream;base64,
					String [] headerBody = glBuffer.uri.split(",", 2);
					String header = headerBody[0];
					// System.out.println(header);
					String body = headerBody[1];
					byte [] data = Base64Coder.decode(body);
					buffer.put(data);
				}else{
					FileHandle file = path.child(glBuffer.uri);
					buffer.put(file.readBytes());
				}
				bufferMap.put(i, buffer);
			}
		}
		return bufferMap;
	}

	@Override
	public ByteBuffer getBuffer(int buffer) {
		return bufferMap.get(buffer);
	}
	
	@Override
	public Pixmap load(GLTFImage glImage) {
		if(glImage.uri == null){
			throw new GdxRuntimeException("GLTF image URI cannot be null");
		}else if(glImage.uri.startsWith("data:")){
			// data:application/octet-stream;base64,
			String [] headerBody = glImage.uri.split(",", 2);
			String header = headerBody[0];
			System.out.println(header);
			String body = headerBody[1];
			byte [] data = Base64Coder.decode(body);
			return PixmapBinaryLoaderHack.load(data, 0, data.length);
		}else{
			return new Pixmap(path.child(glImage.uri));
		}
	}

	public FileHandle getImageFile(GLTFImage glImage) {
		if(glImage.uri != null && !glImage.uri.startsWith("data:")){
			return path.child(glImage.uri);
		}
		return null;
	}
	
}