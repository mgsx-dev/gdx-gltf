package net.mgsx.gltf.ibl.events;

import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class ExportRadianceMapEvent extends ChangeEvent {
	public String path;

	public ExportRadianceMapEvent(String path) {
		super();
		this.path = path;
	}
	
}
