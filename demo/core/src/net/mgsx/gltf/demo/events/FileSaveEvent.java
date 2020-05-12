package net.mgsx.gltf.demo.events;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class FileSaveEvent extends ChangeEvent {
	public FileHandle file;
	public FileSaveEvent(FileHandle file) {
		this.file = file;
	}
}
