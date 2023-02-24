package net.mgsx.gltf.ibl.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

public class IrradianceBaker implements Disposable {

	private final Matrix4 matrix = new Matrix4();
	
	private final ShaderProgram irrandianceShader;
	private final Mesh boxMesh;
	
	public IrradianceBaker() {
		irrandianceShader = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-irradiance.vs.glsl"), 
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-irradiance.fs.glsl"));
		if(!irrandianceShader.isCompiled()) throw new GdxRuntimeException(irrandianceShader.getLog());
		
		MeshBuilder mb = new MeshBuilder();
		mb.begin(Usage.Position, GL20.GL_TRIANGLES);
		BoxShapeBuilder.build(mb, 0,0,0,1,1,1);
		boxMesh = mb.end();
	}
	
	@Override
	public void dispose() {
		irrandianceShader.dispose();
		boxMesh.dispose();
	}
	
	public Cubemap createIrradiance(Cubemap cubemap, int size){
		FrameBufferCubemap fbo = new FrameBufferCubemap(Format.RGB888, size, size, false){
			@Override
			protected void disposeColorTexture(Cubemap colorTexture) {
			}
		};

		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		fbo.begin();
		cubemap.bind();
		while(fbo.nextSide()){
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			CubemapSide side = fbo.getSide();
			renderSideIrradiance(side);
		}
		fbo.end();
		Cubemap map = fbo.getColorBufferTexture();
		fbo.dispose();
		return map;
	}
	
	public Array<Pixmap> createPixmaps(Cubemap cubemap, int size){
		Array<Pixmap> pixmaps = new Array<Pixmap>();
		FrameBufferCubemap fbo = new FrameBufferCubemap(Format.RGB888, size, size, false);

		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		fbo.begin();
		cubemap.bind();
		while(fbo.nextSide()){
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			CubemapSide side = fbo.getSide();
			renderSideIrradiance(side);
			
			pixmaps.add(ScreenUtils.getFrameBufferPixmap(0, 0, size, size));
		}
		fbo.end();
		fbo.dispose();
		return pixmaps;
	}
	
	private void renderSideIrradiance(CubemapSide side) {
		
		ShaderProgram shader = irrandianceShader;
		
		shader.bind();
		shader.setUniformi("environmentMap", 0);
		matrix.setToProjection(.1f, 10f, 90, 1);
		shader.setUniformMatrix("projection", matrix);
		matrix.setToLookAt(side.direction, side.up);
		shader.setUniformMatrix("view", matrix);
		
		shader.setUniformf("sampleDelta", 0.025f); // TODO config
		
		boxMesh.render(shader, GL20.GL_TRIANGLES);
	}
}
