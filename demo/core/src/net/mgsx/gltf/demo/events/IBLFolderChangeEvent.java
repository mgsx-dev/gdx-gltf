package net.mgsx.gltf.demo.events;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class IBLFolderChangeEvent extends ChangeEvent {
	public FileHandle file;
	public IBLFolderChangeEvent(FileHandle file) {
		this.file = file;
	}
}
