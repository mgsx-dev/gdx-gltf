package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.files.FileHandle;

abstract public class FileSelector {

	public FileHandle lastFile;

	abstract public void open(Runnable runnable);

}
