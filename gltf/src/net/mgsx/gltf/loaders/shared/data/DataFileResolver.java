package net.mgsx.gltf.loaders.shared.data;

import java.nio.ByteBuffer;

import com.badlogic.gdx.files.FileHandle;

import net.mgsx.gltf.data.GLTF;

public interface DataFileResolver {
	public void load(FileHandle file);
	public GLTF getRoot();
	public ByteBuffer getBuffer(int buffer);
}
