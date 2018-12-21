package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class FloatAttributeUI extends Table
{
	private Slider slider;
	protected FloatAttribute attribute;
	public FloatAttributeUI(Skin skin, final FloatAttribute attribute) {
		super(skin);
		this.attribute = attribute;
		slider = new Slider(0, 1, .01f, false, skin);
		if(attribute != null){
			add(FloatAttribute.getAttributeAlias(attribute.type));
			add(slider).row();
			slider.setValue(attribute.value);
			slider.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					attribute.value = slider.getValue();
				}
			});
		}
	}
}
