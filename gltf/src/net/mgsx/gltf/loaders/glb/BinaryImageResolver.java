package net.mgsx.gltf.loaders.glb;

import java.nio.ByteBuffer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gltf.data.data.GLTFBufferView;
import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.loaders.shared.data.DataResolver;
import net.mgsx.gltf.loaders.shared.texture.ImageResolver;
import net.mgsx.gltf.loaders.shared.texture.PixmapBinaryLoaderHack;

public class BinaryImageResolver extends ImageResolver
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