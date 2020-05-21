package net.mgsx.gltf.ibl.events;

import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class ExportBRDFMapEvent extends ChangeEvent {
	public String path;

	public ExportBRDFMapEvent(String path) {
		super();
		this.path = path;
	}
	
}
