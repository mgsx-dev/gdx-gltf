package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class Vector3UI extends Table
{
	private Array<Slider> sliders = new Array<Slider>();
	
	public Vector3 value;
	public Vector3UI(Skin skin, final Vector3 value) {
		super(skin);
		this.value = value;
		for(int i=0 ; i<3 ; i++){
			final Slider slider = new Slider(-1, 1, .01f, false, skin);
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
			sliders.add(slider);
		}
	}
	private String name(int i) {
		switch(i){
		case 0: return "x";
		case 1: return "y";
		case 2: return "z";
		}
		return null;
	}
	private void set(int i, float value) {
		switch(i){
		case 0: this.value.x = value; break;
		case 1: this.value.y = value; break;
		case 2: this.value.z = value; break;
		}
	}
	private float get(int i) {
		switch(i){
		case 0: return value.x;
		case 1: return value.y;
		case 2: return value.z;
		}
		return 0;
	}
	public void set(Vector3 value) {
		this.value.set(value);
		sliders.get(0).setValue(value.x);
		sliders.get(1).setValue(value.y);
		sliders.get(2).setValue(value.z);
	}
	
}
