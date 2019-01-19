package net.mgsx.gltf.loaders.gltf;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.loaders.shared.texture.ImageResolver;
import net.mgsx.gltf.loaders.shared.texture.PixmapBinaryLoaderHack;

public class SeparatedImageResolver extends ImageResolver
{
	private FileHandle path;
	
	public SeparatedImageResolver(FileHandle path) {
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