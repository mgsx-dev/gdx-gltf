package net.mgsx.gltf.ibl.util;

import com.badlogic.gdx.Gdx;

public class ChunkedRender<T> {
	
	public static interface ChunkRenderer<T> {
		public T begin();
		public void render(int x, int y, int width, int height);
		public T end();
		public T cancel();
	}
	
	int width, height, tileWidth, tileHeight, currentX, currentY;
	private ChunkRenderer<T> renderer;
	
	public ChunkedRender(int width, int height, int tileWidth, int tileHeight) {
		super();
		this.width = width;
		this.height = height;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}
	
	public T begin(ChunkRenderer<T> renderer){
		this.renderer = renderer;
		return this.renderer.begin();
	}
	
	public boolean update(){
		boolean completed = true;
		for( ; completed && currentY<height ; ){
			for( ; completed && currentX<width ; currentX+=tileWidth){
				int w = Math.min(width - currentX, tileWidth);
				int h = Math.min(height - currentY, tileHeight);
				Gdx.gl.glViewport(currentX, currentY, w, h);
				renderer.render(currentX, currentY, w, h);
				completed = false;
			}
			if(completed) currentY+=tileHeight;
		}
		
		return completed;
	}
	
	public float getProgress(){
		return (float)(currentY * width + currentX) / (float)((height-1) * width + width-1);
	}
	
	public T end(){
		T result = this.renderer.end();
		this.renderer = null;
		return result;
	}
	
	public T cancel(){
		T result = this.renderer.cancel();
		this.renderer = null;
		return result;
	}
}
