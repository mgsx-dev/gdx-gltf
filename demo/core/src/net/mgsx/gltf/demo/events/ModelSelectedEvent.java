package net.mgsx.gltf.demo.events;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class ModelSelectedEvent extends ChangeEvent {

	public FileHandle glFile;

	public ModelSelectedEvent(FileHandle glFile) {
		this.glFile = glFile;
	}

}
