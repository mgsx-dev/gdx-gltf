package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class FloatUI extends Table
{
	private Slider slider;
	public FloatUI(Skin skin, final float value){
		this(skin, value, null);
	}
	public FloatUI(Skin skin, final float value, String name) {
		super(skin);
		slider = new Slider(0, 1, .01f, false, skin);
		if(name != null) add(name);
		add(slider).row();
		slider.setValue(value);
		slider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				onChange(slider.getValue());
			}
		});
	}
	
	protected void onChange(float value){
		
	}
	
	public float getValue(){
		return slider.getValue();
	}
}
