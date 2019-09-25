package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class FloatUI extends Table
{
	private Slider slider;
	public FloatUI(Skin skin, final float value) {
		super(skin);
		slider = new Slider(0, 1, .01f, false, skin);
		add(slider).row();
		slider.setValue(value);
	}
	
	public float getValue(){
		return slider.getValue();
	}
}
