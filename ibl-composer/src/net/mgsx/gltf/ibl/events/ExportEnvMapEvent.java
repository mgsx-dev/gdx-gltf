package net.mgsx.gltf.ibl.events;

import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class ExportEnvMapEvent extends ChangeEvent {
	public String path;

	public ExportEnvMapEvent(String path) {
		super();
		this.path = path;
	}
	
}
