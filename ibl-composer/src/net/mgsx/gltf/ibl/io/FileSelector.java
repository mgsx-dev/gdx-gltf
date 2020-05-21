package net.mgsx.gltf.ibl.io;

import java.util.function.Consumer;

import com.badlogic.gdx.files.FileHandle;

abstract public class FileSelector {

	public static FileSelector instance;
	
	public FileHandle lastFile;

	/** open a file */
	abstract public void open(Consumer<FileHandle> handler);
	
	/** open a file */
	abstract public void save(Consumer<FileHandle> handler);
	
	/** select a folder */
	abstract public void selectFolder(Consumer<FileHandle> handler);

}
