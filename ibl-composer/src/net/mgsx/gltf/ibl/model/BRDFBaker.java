package net.mgsx.gltf.ibl.model;

import org.lwjgl.opengl.GL30;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

public class BRDFBaker implements Disposable {

	private ShaderProgram brdfShader;
	private ShapeRenderer shapes;
	private SpriteBatch batch;

	public BRDFBaker() {
		brdfShader = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/shaders/brdf.vs.glsl"), 
				Gdx.files.classpath("net/mgsx/gltf/shaders/brdf.fs.glsl"));
		if(!brdfShader.isCompiled()) throw new GdxRuntimeException(brdfShader.getLog());
		
		shapes = new ShapeRenderer(20, brdfShader);
		shapes.getProjectionMatrix().setToOrtho2D(0, 1, 1, -1);
		
		batch = new SpriteBatch();
	}
	
	@Override
	public void dispose() {
		brdfShader.dispose();
		shapes.dispose();
		batch.dispose();
	}
	
	public Texture createBRDF(int size, boolean RG16){
		FrameBuffer fbo;
		if(RG16){
			FrameBufferBuilder fbb = new FrameBufferBuilder(size, size);
			fbb.addColorTextureAttachment(GL30.GL_RG16F, GL30.GL_RG, GL20.GL_FLOAT);
			fbo = fbb.build();
		}else{
			fbo = new FrameBuffer(Format.RGB888, size, size, false){
				@Override
				protected void disposeColorTexture(Texture colorTexture) {
				}
			};
		}
		
		fbo.begin();
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		shapes.begin(ShapeType.Filled);
		shapes.rect(0, 0, 1, 1);
		shapes.end();
		
		fbo.end();
		Texture map = fbo.getColorBufferTexture();
		if(RG16){
			fbo.getTextureAttachments().clear();
			fbo.dispose();
		}else{
			fbo.dispose();
		}
		
		return map;
	}
	
	public Pixmap createBRDFPixmap(int size, boolean RG16) {
		Texture brdf = createBRDF(size, RG16);
		Pixmap pixmap = RG16 ? createBRDFPacked(brdf) : createBRDF(brdf);
		brdf.dispose();
		return pixmap;
	}
	
	private Pixmap createBRDF(Texture brdf) {
		int size = brdf.getWidth();
		FrameBuffer fbo = new FrameBuffer(Format.RGB888, size, size, false);
		fbo.begin();
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		brdf.bind();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.begin();
		batch.draw(brdf, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f);
		batch.end();
		Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, size, size);
		fbo.end();
		fbo.dispose();
		
		return pixmap;
	}

	private Pixmap createBRDFPacked(Texture brdf){
		ShaderProgram shader = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/shaders/brdf-pack.vs.glsl"), 
				Gdx.files.classpath("net/mgsx/gltf/shaders/brdf-pack.fs.glsl"));
		if(!shader.isCompiled()) throw new GdxRuntimeException(shader.getLog());
		SpriteBatch batch = new SpriteBatch(1, shader);
		FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, brdf.getWidth(), brdf.getHeight(), false);
		fbo.begin();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ZERO);
		batch.begin();
		batch.draw(brdf, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f);
		batch.end();
		batch.dispose();
		shader.dispose();
		Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, brdf.getWidth(), brdf.getHeight());
		fbo.end();
		fbo.dispose();
		return pixmap;
	}
	
	public Texture createBRDFUnacked(Pixmap pixmap){
		ShaderProgram shader = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/shaders/brdf-pack.vs.glsl"), 
				Gdx.files.classpath("net/mgsx/gltf/shaders/brdf-unpack.fs.glsl"));
		if(!shader.isCompiled()) throw new GdxRuntimeException(shader.getLog());
		SpriteBatch batch = new SpriteBatch(1, shader);
		FrameBufferBuilder fbb = new FrameBufferBuilder(pixmap.getWidth(), pixmap.getHeight());
		fbb.addColorTextureAttachment(GL30.GL_RG16F, GL30.GL_RG, GL20.GL_FLOAT);
		FrameBuffer fbo = fbb.build();
		Texture brdf = new Texture(pixmap);
		fbo.begin();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ZERO);
		batch.begin();
		batch.draw(brdf, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f);
		batch.end();
		batch.dispose();
		shader.dispose();
		brdf.dispose();
		Texture texture = fbo.getColorBufferTexture();
		fbo.getTextureAttachments().clear();
		fbo.end();
		fbo.dispose();
		return texture;
	}

	
}
