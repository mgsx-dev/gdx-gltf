package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class Vector2UI extends Table
{
	// private Array<Slider> sliders = new Array<Slider>();
	private Vector2 value;
	public Vector2UI(Skin skin, final Vector2 value) {
		super(skin);
		this.value = value;
		for(int i=0 ; i<2 ; i++){
			final Slider slider = new Slider(0, 1, .01f, false, skin);
			add(name(i));
			add(slider).row();
			slider.setValue(get(i));
			final int index = i;
			slider.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					set(index, slider.getValue());
				}
			});
		}
	}
	private String name(int i) {
		switch(i){
		case 0: return "x";
		case 1: return "y";
		}
		return null;
	}
	private void set(int i, float value) {
		switch(i){
		case 0: this.value.x = value; break;
		case 1: this.value.y = value; break;
		}
	}
	private float get(int i) {
		switch(i){
		case 0: return value.x;
		case 1: return value.y;
		}
		return 0;
	}
	
}
