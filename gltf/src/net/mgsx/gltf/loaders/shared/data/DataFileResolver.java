package net.mgsx.gltf.loaders.shared.data;

import java.nio.ByteBuffer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;

import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.texture.GLTFImage;

public interface DataFileResolver {
	public void load(FileHandle file);
	public GLTF getRoot();
	public ByteBuffer getBuffer(int buffer);
	public Pixmap load(GLTFImage glImage);
}
