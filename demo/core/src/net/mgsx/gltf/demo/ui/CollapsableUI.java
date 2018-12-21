package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class CollapsableUI  extends Table
{
	private Cell<Table> optCell;
	protected Table optTable;
	public TextButton toggle;

	public CollapsableUI(Skin skin, String name, boolean visible) {
		toggle = new TextButton(name, skin, "toggle");
		toggle.setChecked(visible);
		add(toggle).row();
		optTable = new Table(skin);
		optCell = add();
		row();
		show(visible);
		toggle.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				show(toggle.isChecked());
			}
		});
	}
	
	public void show(boolean state){
		if(state){
			optCell.setActor(optTable);
		}else{
			optCell.setActor(null);
		}
	}
}