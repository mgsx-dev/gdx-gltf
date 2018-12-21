package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

public class ColorAttributeUI extends Table
{
	private Array<Slider> sliders = new Array<Slider>();
	private ColorAttribute attribute;
	public ColorAttributeUI(Skin skin, final ColorAttribute attribute) {
		super(skin);
		if(attribute == null) return;
		this.attribute = attribute;
		add(ColorAttribute.getAttributeAlias(attribute.type));
		for(int i=0 ; i<4 ; i++){
			if(i != 0) add();
			final Slider slider = new Slider(0, 1, .01f, false, skin);
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
	private void set(int i, float value) {
		switch(i){
		case 0: attribute.color.r = value; break;
		case 1: attribute.color.g = value; break;
		case 2: attribute.color.b = value; break;
		case 3: attribute.color.a = value; break;
		}
	}
	public float get(int i) {
		switch(i){
		case 0: return attribute.color.r;
		case 1: return attribute.color.g;
		case 2: return attribute.color.b;
		case 3: return attribute.color.a;
		}
		return 0;
	}
	
	public void set(Color value) {
		this.attribute.color.set(value);
		sliders.get(0).setValue(value.r);
		sliders.get(1).setValue(value.g);
		sliders.get(2).setValue(value.b);
		sliders.get(3).setValue(value.a);
	}
	
	public void set(float r, float g, float b, float a) {
		this.attribute.color.set(r,g,b,a);
		sliders.get(0).setValue(r);
		sliders.get(1).setValue(g);
		sliders.get(2).setValue(b);
		sliders.get(3).setValue(a);
	}
}
