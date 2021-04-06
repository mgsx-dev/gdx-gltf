package net.mgsx.gltf.ibl.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;

public class TabPane extends Table
{
	private Table tabs;
	private Array<Actor> actors = new Array<Actor>();
	private Cell paneCell;
	private ButtonGroup<TextButton> buttonGroup;

	public TabPane(Skin skin) {
		super(skin);
		
		tabs = new Table(skin);
		
		add(tabs).expandX().left().row();
		
		Table paneTable = new Table(skin);
		paneTable.setBackground("default-pane");
		paneCell = paneTable.add().grow();
				
		add(paneTable).grow();
		row();
		buttonGroup = new ButtonGroup<TextButton>();
	}
	
	public void addPane(String title, Actor actor){
		TextButton bt = new TextButton(title, getSkin(), "toggle");
		tabs.add(bt);
		UI.change(bt, event->{if(bt.isChecked()) setPane(actor);});
		actors.add(actor);
		buttonGroup.add(bt);
	}

	private void setPane(Actor actor) {
		paneCell.setActor(actor);
		invalidateHierarchy();
	}

}
