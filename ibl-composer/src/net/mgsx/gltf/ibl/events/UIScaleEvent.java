package net.mgsx.gltf.ibl.events;

import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class UIScaleEvent extends ChangeEvent {
	public float newScale;

	public UIScaleEvent(float newScale) {
		super();
		this.newScale = newScale;
	}
}
