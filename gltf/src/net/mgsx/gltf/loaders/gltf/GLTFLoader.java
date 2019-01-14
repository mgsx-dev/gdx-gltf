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
import net.mgsx.gltf.loaders.shared.GLTFLoaderBase;
import net.mgsx.gltf.loaders.shared.data.DataResolver;
import net.mgsx.gltf.loaders.shared.texture.ImageResolver;
import net.mgsx.gltf.loaders.shared.texture.PixmapBinaryLoaderHack;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GLTFLoader extends GLTFLoaderBase 
{
	private static class DefaultImageResolver extends ImageResolver
	{
		private FileHandle path;
		
		public DefaultImageResolver(FileHandle path) {
			super();
			this.path = path;
		}

		@Override
		protected Pixmap load(GLTFImage glImage) {
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
	}
	
	public SceneAsset load(FileHandle glFile){
		this.glModel = new Json().fromJson(GLTF.class, glFile);
		FileHandle path = glFile.parent();
		dataResolver = new DataResolver(glModel, loadBuffers(path));
		imageResolver = new DefaultImageResolver(path);
		return loadInternal();
	}
	
	private ObjectMap<Integer, ByteBuffer> loadBuffers(FileHandle path) {
		ObjectMap<Integer, ByteBuffer> bufferMap = new ObjectMap<Integer, ByteBuffer>();
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

}
