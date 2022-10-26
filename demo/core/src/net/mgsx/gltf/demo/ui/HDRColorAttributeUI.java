package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.scene3d.attributes.PBRHDRColorAttribute;

public class HDRColorAttributeUI extends Table
{
	private Array<Slider> sliders = new Array<Slider>();
	private PBRHDRColorAttribute attribute;
	public HDRColorAttributeUI(Skin skin, final PBRHDRColorAttribute attribute, float max) {
		super(skin);
		if(attribute == null) return;
		this.attribute = attribute;
		add(ColorAttribute.getAttributeAlias(attribute.type));
		for(int i=0 ; i<3 ; i++){
			if(i != 0) add();
			final Slider slider = new Slider(0, max, .01f, false, skin);
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
		case 0: attribute.r = value; break;
		case 1: attribute.g = value; break;
		case 2: attribute.b = value; break;
		}
	}
	public float get(int i) {
		switch(i){
		case 0: return attribute.r;
		case 1: return attribute.g;
		case 2: return attribute.b;
		}
		return 0;
	}
	
	public void set(float r, float g, float b) {
		this.attribute.set(r,g,b);
		sliders.get(0).setValue(r);
		sliders.get(1).setValue(g);
		sliders.get(2).setValue(b);
	}
}
