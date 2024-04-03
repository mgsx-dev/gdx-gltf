package net.mgsx.gltf.loaders.shared.texture;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

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
				Pixmap pixmap = dataFileResolver.load(glImage);
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

	public void clear() {
		pixmaps.clear();
	}

	public Array<Pixmap> getPixmaps(Array<Pixmap> array) {
		array.addAll(pixmaps);
		return array;
	}
}
