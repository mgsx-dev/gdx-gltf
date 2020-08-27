package net.mgsx.gltf.ibl.ui;

import com.badlogic.gdx.utils.Array;

public class MapSize {
	
	public static Array<MapSize> createPOT(int min, int max){
		Array<MapSize> array = new Array<MapSize>();
		for(int i=min ; i<=max ; i++){
			array.add(new MapSize(1 << i));
		}
		return array;
	}
	
	public int size;

	public MapSize(int size) {
		this.size = size;
	}
	
	@Override
	public String toString() {
		return size + "x" + size;
	}
}
