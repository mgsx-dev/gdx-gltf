package net.mgsx.gltf.loaders.shared.texture;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.loaders.shared.data.DataFileResolver;

public class ImageResolver implements Disposable {
	
	private Array<Pixmap> pixmaps = new Array<Pixmap>();	
	
	private DataFileResolver dataFileResolver;
	
	public ImageResolver(DataFileResolver dataFileResolver) {
		super();
		this.dataFileResolver = dataFileResolver;
	}

	public void load(Array<GLTFImage> glImages) {
		if(glImages != null){
			for(int i=0 ; i<glImages.size ; i++){
				
				GLTFImage glImage = glImages.get(i);
				
				// XXX should throw exception (usefull to test models with incompatible bitmaps)
				Pixmap pixmap;
				try{
					pixmap = dataFileResolver.load(glImage);
				}catch(GdxRuntimeException e){
					pixmap = null;
					System.err.println("cannot load pixmap " + glImage.uri);
				}
				pixmaps.add(pixmap);
			}
		}
	}
	
	public Pixmap get(int index) {
		return pixmaps.get(index);
	}
	
	@Override
	public void dispose() {
		for(Pixmap pixmap : pixmaps){
			pixmap.dispose();
		}
		pixmaps.clear();
	}
}
