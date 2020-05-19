package net.mgsx.gltf.ibl;

import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

public class CubemapUI extends Table
{

	public CubemapUI(Array<Texture> textures) {
		defaults().width(100).height(100);
		
		add();
		add();
		add(image(textures.get(2), CubemapSide.PositiveY));
		add();
		row();
		
		add(image(textures.get(4), CubemapSide.NegativeZ));
		add(image(textures.get(0), CubemapSide.NegativeX));
		add(image(textures.get(5), CubemapSide.PositiveZ));
		add(image(textures.get(1), CubemapSide.PositiveX));
		row();
		
		add();
		add();
		add(image(textures.get(3), CubemapSide.NegativeY));
		add();
		row();
	}

	private Actor image(Texture map, CubemapSide side) {
		Image img = new Image(map);
		img.setScaling(Scaling.fit);
		if(side == CubemapSide.PositiveY || side == CubemapSide.NegativeY){
			img.setOrigin(50, 50);
			img.setScale(-1, -1);
		}
		return img;
	}
	
}
