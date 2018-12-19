package net.mgsx.gltf.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.CubemapData;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class FacedMultiCubemapData implements CubemapData
{

	final protected TextureData[] data;
	private int levels;

	public FacedMultiCubemapData(FileHandleResolver resolver, String baseName, String midName, String extension, int levels)
	{
		this.levels = levels;
		data = new TextureData[6 * levels];
		String[] names = {"right", "left", "top", "bottom", "front", "back"};
		for(int level = 0 ; level<levels ; level++){
			for(int face = 0 ; face < 6 ; face++){
				// eg. specular_right_8.jpg
				FileHandle file = resolver.resolve(baseName + names[face] + midName + level + extension);
				data[level*6+face] = new PixmapTextureData(new Pixmap(file), null, false, true);
			}
		}
	}

	@Override
	public boolean isManaged () {
		for (TextureData data : this.data)
			if (!data.isManaged()) return false;
		return true;
	}

	/** @return True if all sides of this cubemap are set, false otherwise. */
	public boolean isComplete () {
		for (int i = 0; i < data.length; i++)
			if (data[i] == null) return false;
		return true;
	}

	/** @return The {@link TextureData} for the specified side, can be null if the cubemap is incomplete. */
	public TextureData getTextureData (CubemapSide side) {
		return data[side.index];
	}

	@Override
	public int getWidth () {
		int tmp, width = 0;
		if (data[CubemapSide.PositiveZ.index] != null && (tmp = data[CubemapSide.PositiveZ.index].getWidth()) > width) width = tmp;
		if (data[CubemapSide.NegativeZ.index] != null && (tmp = data[CubemapSide.NegativeZ.index].getWidth()) > width) width = tmp;
		if (data[CubemapSide.PositiveY.index] != null && (tmp = data[CubemapSide.PositiveY.index].getWidth()) > width) width = tmp;
		if (data[CubemapSide.NegativeY.index] != null && (tmp = data[CubemapSide.NegativeY.index].getWidth()) > width) width = tmp;
		return width;
	}

	@Override
	public int getHeight () {
		int tmp, height = 0;
		if (data[CubemapSide.PositiveZ.index] != null && (tmp = data[CubemapSide.PositiveZ.index].getHeight()) > height)
			height = tmp;
		if (data[CubemapSide.NegativeZ.index] != null && (tmp = data[CubemapSide.NegativeZ.index].getHeight()) > height)
			height = tmp;
		if (data[CubemapSide.PositiveX.index] != null && (tmp = data[CubemapSide.PositiveX.index].getHeight()) > height)
			height = tmp;
		if (data[CubemapSide.NegativeX.index] != null && (tmp = data[CubemapSide.NegativeX.index].getHeight()) > height)
			height = tmp;
		return height;
	}

	@Override
	public boolean isPrepared () {
		return false;
	}

	@Override
	public void prepare () {
		if (!isComplete()) throw new GdxRuntimeException("You need to complete your cubemap data before using it");
		for (int i = 0; i < data.length; i++)
			if (!data[i].isPrepared()) data[i].prepare();
	}

	@Override
	public void consumeCubemapData () {
		for(int level = 0 ; level<levels ; level++){
			for (int i = 0; i < 6; i++) {
				int index = level * 6 + i;
				if (data[index].getType() == TextureData.TextureDataType.Custom) {
					data[index].consumeCustomData(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i);
				} else {
					Pixmap pixmap = data[index].consumePixmap();
					boolean disposePixmap = data[index].disposePixmap();
					if (data[index].getFormat() != pixmap.getFormat()) {
						Pixmap tmp = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), data[index].getFormat());
						tmp.setBlending(Blending.None);
						tmp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
						if (data[index].disposePixmap()) pixmap.dispose();
						pixmap = tmp;
						disposePixmap = true;
					}
					Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
					Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, level, pixmap.getGLInternalFormat(), pixmap.getWidth(),
						pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
					if (disposePixmap) pixmap.dispose();
				}
			}
		}
	}

}
