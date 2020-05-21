package net.mgsx.gltf.ibl.events;

import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class ExportIrradianceMapEvent extends ChangeEvent {
	public String path;

	public ExportIrradianceMapEvent(String path) {
		super();
		this.path = path;
	}
	
}
