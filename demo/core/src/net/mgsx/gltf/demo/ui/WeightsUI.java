package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.scene3d.model.WeightVector;

public class WeightsUI extends Table
{
	private Array<Slider> sliders = new Array<Slider>();
	public WeightVector value;
	public WeightsUI(Skin skin, final WeightVector value, final Array<String> names) {
		super(skin);
		this.value = value;
		for(int i=0 ; i<value.count ; i++){
			final Slider slider = new Slider(0, 1, .01f, false, skin);
			final int index = i;
			slider.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					set(index, slider.getValue());
				}
			});
			String name = names != null && i < names.size ? names.get(i) : "#" + (i+1);
			add(name);
			add(slider).row();
			slider.setValue(get(i));
			sliders.add(slider);
		}
	}
	private void set(int i, float value) {
		this.value.values[i] = value;
	}
	public float get(int i) {
		return this.value.values[i];
	}
	
}
