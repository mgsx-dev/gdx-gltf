package net.mgsx.gltf.ibl.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gltf.ibl.exceptions.FrameBufferError;

public class EnvironmentBaker implements Disposable {

	private FrameBufferCubemap fboEnv;
	private ShaderProgram rectToCubeShader;
	private ShapeRenderer rectToCubeRenderer;
	
	public EnvironmentBaker() {
		rectToCubeShader = loadShader("cubemap-make");
		rectToCubeRenderer = new ShapeRenderer(20, rectToCubeShader);
		rectToCubeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
	}
	
	@Override
	public void dispose() {
		rectToCubeShader.dispose();
		rectToCubeRenderer.dispose();
		if(fboEnv != null) fboEnv.dispose();
	}
	
	private static ShaderProgram loadShader(String name){
		ShaderProgram shader = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/shaders/" + name + ".vs.glsl"), 
				Gdx.files.classpath("net/mgsx/gltf/shaders/" + name + ".fs.glsl"));
		if(!shader.isCompiled()) throw new GdxRuntimeException(shader.getLog());
		if(shader.getLog().length() > 0) Gdx.app.error(EnvironmentBaker.class.getSimpleName(), shader.getLog());
		return shader;
	}
	
	public Cubemap getEnvMap(Texture hdrTexture, int size, float exposure){
		if(fboEnv != null && fboEnv.getWidth() != size){
			fboEnv.dispose();
			fboEnv = null;
		}
		if(fboEnv == null){
			try{
				fboEnv = new FrameBufferCubemap(Format.RGB888, size, size, false);
			}catch(IllegalStateException e){
				fboEnv = new FrameBufferCubemap(Format.RGB888, 1, 1, false);
				throw new FrameBufferError(e);
			}
		}
		fboEnv.begin();
		while(fboEnv.nextSide()){
			hdrTexture.bind();
			rectToCubeShader.bind();
			rectToCubeShader.setUniformi("u_hdr", 0);
			rectToCubeShader.setUniformf("u_exposure", exposure);
			
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			CubemapSide side = fboEnv.getSide();
			renderSide(side);
		}
		fboEnv.end();
		return fboEnv.getColorBufferTexture();
	}
	
	private void renderSide(CubemapSide side) {
		
		Matrix4 matrix = new Matrix4().setToLookAt(side.direction, side.up).tra();
		
		ShaderProgram shader = rectToCubeShader;
		
		shader.bind();
		shader.setUniformMatrix("u_mat", matrix);

		ShapeRenderer shapes = rectToCubeRenderer;
		shapes.begin(ShapeType.Filled);
		shapes.rect(0, 0, 1, 1);
		shapes.end();
		
	}

	public Array<Pixmap> createEnvMapPixmaps(Texture hdrTexture, int size, float exposure) {
		Array<Pixmap> pixmaps = new Array<Pixmap>();
		FrameBuffer fbo = new FrameBuffer(Format.RGB888, size, size, false);
		fbo.begin();
		for(CubemapSide side : CubemapSide.values()){
			hdrTexture.bind();
			rectToCubeShader.bind();
			rectToCubeShader.setUniformi("u_hdr", 0);
			rectToCubeShader.setUniformf("u_exposure", exposure);
			
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			renderSide(side);
			
			Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, size, size);
			pixmaps.add(pixmap);
		}
		
		fbo.end();
		fbo.dispose();
		return pixmaps;
	}

	public Cubemap getLastMap() {
		return fboEnv.getColorBufferTexture();
	}
}
