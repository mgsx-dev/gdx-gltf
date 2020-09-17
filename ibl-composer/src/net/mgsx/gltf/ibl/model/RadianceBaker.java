package net.mgsx.gltf.ibl.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gltf.ibl.util.GLUtils;
import net.mgsx.gltf.scene3d.utils.FacedMultiCubemapData;

public class RadianceBaker implements Disposable {
	
	private static final Matrix4 matrix = new Matrix4();
	
	private ShaderProgram randianceShader;
	private Mesh boxMesh;

	public RadianceBaker() {
		randianceShader = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-radiance.vs.glsl"), 
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-radiance.fs.glsl"));
		if(!randianceShader.isCompiled()) throw new GdxRuntimeException(randianceShader.getLog());
		
		MeshBuilder mb = new MeshBuilder();
		mb.begin(Usage.Position, GL20.GL_TRIANGLES);
		BoxShapeBuilder.build(mb, 0,0,0,1,1,1);
		boxMesh = mb.end();
	}

	@Override
	public void dispose() {
		randianceShader.dispose();
		boxMesh.dispose();
	}
	
	public Cubemap createRadiance(Cubemap cubemap, int baseSize){
		int mipMapLevels = GLUtils.sizeToPOT(baseSize);
		Pixmap[] maps = new Pixmap[mipMapLevels * 6];
		int index = 0;
		cubemap.bind();
		for(int level=0 ; level<mipMapLevels ; level++){
			int size = 1 << (mipMapLevels - level - 1);
			FrameBuffer fbo = new FrameBuffer(Format.RGB888, size, size, false);
			fbo.begin();
			for(int s=0 ; s<6 ; s++){
				Gdx.gl.glClearColor(0, 0, 0, 0);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				
				CubemapSide side = CubemapSide.values()[s];
				renderSideRadiance(side, level, mipMapLevels+1);
				
				maps[index] = ScreenUtils.getFrameBufferPixmap(0, 0, size, size);
				index++;
			}
			fbo.end();
			fbo.dispose();
		}
		FacedMultiCubemapData data = new FacedMultiCubemapData(maps, mipMapLevels);
		Cubemap map = new Cubemap(data);
		map.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
		return map;
	}
	
	public Array<Pixmap> createPixmaps(Cubemap cubemap, int baseSize){
		int mipMapLevels = GLUtils.sizeToPOT(baseSize);
		Pixmap[] maps = new Pixmap[mipMapLevels * 6];
		int index = 0;
		cubemap.bind();
		for(int level=0 ; level<mipMapLevels ; level++){
			int size = 1 << (mipMapLevels - level - 1);
			FrameBuffer fbo = new FrameBuffer(Format.RGB888, size, size, false);
			fbo.begin();
			for(int s=0 ; s<6 ; s++){
				Gdx.gl.glClearColor(0, 0, 0, 0);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
				
				CubemapSide side = CubemapSide.values()[s];
				renderSideRadiance(side, level, mipMapLevels+1);
				
				maps[index] = ScreenUtils.getFrameBufferPixmap(0, 0, size, size);
				index++;
			}
			fbo.end();
			fbo.dispose();
		}
		return new Array<Pixmap>(maps);
	}
	
	private void renderSideRadiance(CubemapSide side, int mip, int maxMipLevels) {
		
		ShaderProgram shader = randianceShader;
		
		shader.bind();
		shader.setUniformi("environmentMap", 0);
		matrix.setToProjection(.1f, 10f, 90, 1);
		shader.setUniformMatrix("projection", matrix);
		matrix.setToLookAt(side.direction, side.up);
		shader.setUniformMatrix("view", matrix);
		
		float roughness = (float)mip / (float)(maxMipLevels - 1);
	    shader.setUniformf("roughness", roughness);
		
	    boxMesh.render(shader, GL20.GL_TRIANGLES);
	}
	
}
