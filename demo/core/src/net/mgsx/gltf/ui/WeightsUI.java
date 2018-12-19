package net.mgsx.gltf.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.scene3d.WeightVector;

public class WeightsUI extends Table
{
	private Array<Slider> sliders = new Array<Slider>();
	public WeightVector value;
	public WeightsUI(Skin skin, final WeightVector value) {
		super(skin);
		this.value = value;
		for(int i=0 ; i<value.count ; i++){
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
		this.value.values[i] = value;
	}
	public float get(int i) {
		return this.value.values[i];
	}
	
}
