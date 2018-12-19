package net.mgsx.gltf.loaders;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;

import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.data.GLTFBuffer;
import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.scene3d.SceneAsset;

public class GLTFLoader extends GLTFLoaderBase 
{
	private FileHandle path;
	
	public SceneAsset load(FileHandle glFile, FileHandle path){
		this.path = path;
		this.glModel = new Json().fromJson(GLTF.class, glFile);;
		loadBuffers();
		return loadInternal();
	}
	
	private void loadBuffers() {
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
	}

	@Override
	protected Pixmap loadPixmap(GLTFImage glImage) {
		if(glImage.uri == null){
			throw new GdxRuntimeException("GLTF image URI cannot be null");
		}else if(glImage.uri.startsWith("data:")){
			// data:application/octet-stream;base64,
			String [] headerBody = glImage.uri.split(",", 2);
			String header = headerBody[0];
			System.out.println(header);
			String body = headerBody[1];
			byte [] data = Base64Coder.decode(body);
			return new Pixmap(data, 0, data.length);
		}else{
			return new Pixmap(path.child(glImage.uri));
		}
	}
}
