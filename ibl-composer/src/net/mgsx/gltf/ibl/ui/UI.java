package net.mgsx.gltf.ibl.ui;

import java.io.IOException;
import java.util.function.Consumer;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class UI {
	public static <T extends Actor> T change(T actor, Consumer<ChangeEvent> handler){
		actor.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				handler.accept(event);
			}
		});
		return actor;
	}
	public static <T extends Slider> T changeCompleted(T slider, Consumer<ChangeEvent> handler){
		slider.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(!slider.isDragging()) handler.accept(event);
			}
		});
		return slider;
	}
	public static TextButton toggle(Skin skin, String text, boolean checked, Consumer<Boolean> handler){
		TextButton bt = new TextButton(text, skin, "toggle");
		bt.setChecked(checked);
		change(bt, event->handler.accept(bt.isChecked()));
		return bt;
	}

	public static void dialog(Stage stage, Skin skin, String title, String message, IOException e) {
		dialog(stage, skin, title, message + "\n" + e.getClass().getSimpleName() + ": " + e.getMessage());
	}
	public static void dialog(Stage stage, Skin skin, String title, String message) {
		Dialog d = new Dialog(title, skin);
		d.text(message);
		d.button("OK");
		d.pack();
		d.show(stage);
	}
}
