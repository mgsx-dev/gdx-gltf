package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class BooleanUI extends Table
{
	private TextButton bt;

	public BooleanUI(Skin skin, boolean defaultValue) {
		super(skin);
		bt = new TextButton("enabled", skin, "toggle");
		bt.setChecked(defaultValue);
		add(bt);
	}
	
	public boolean isOn(){
		return bt.isChecked();
	}
	

}
