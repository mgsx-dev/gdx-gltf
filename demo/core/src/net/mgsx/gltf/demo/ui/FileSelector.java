package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.files.FileHandle;

abstract public class FileSelector {

	public FileHandle lastFile;

	/** open a file */
	abstract public void open(Runnable runnable);
	
	/** open a file */
	abstract public void save(Runnable runnable);
	
	/** select a folder */
	abstract public void selectFolder(Runnable runnable);

}
