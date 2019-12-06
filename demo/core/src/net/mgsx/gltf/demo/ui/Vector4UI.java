package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class Vector4UI extends Table
{
	private Array<Slider> sliders = new Array<Slider>();
	public Color value;
	public Vector4UI(Skin skin, final Color value) {
		super(skin);
		this.value = value;
		for(int i=0 ; i<4 ; i++){
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
			sliders.add(slider);
		}
	}
	private String name(int i) {
		switch(i){
		case 0: return "x";
		case 1: return "y";
		case 2: return "z";
		case 3: return "w";
		}
		return null;
	}
	private void set(int i, float value) {
		switch(i){
		case 0: this.value.r = value; break;
		case 1: this.value.g = value; break;
		case 2: this.value.b = value; break;
		case 3: this.value.a = value; break;
		}
	}
	public float get(int i) {
		switch(i){
		case 0: return value.r;
		case 1: return value.g;
		case 2: return value.b;
		case 3: return value.a;
		}
		return 0;
	}
	
	public Color getValue() {
		return value;
	}
	
	public void set(Color value) {
		this.value.set(value);
		sliders.get(0).setValue(value.r);
		sliders.get(1).setValue(value.g);
		sliders.get(2).setValue(value.b);
		sliders.get(3).setValue(value.a);
	}
	
	public void set(float r, float g, float b, float a) {
		this.value.set(r,g,b,a);
		sliders.get(0).setValue(r);
		sliders.get(1).setValue(g);
		sliders.get(2).setValue(b);
		sliders.get(3).setValue(a);
	}
}
