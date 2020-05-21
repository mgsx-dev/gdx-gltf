package net.mgsx.gltf.ibl.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
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
	
	private void renderSideRadiance(CubemapSide side, int mip, int maxMipLevels) {
		
		ShaderProgram shader = randianceShader;
		
		shader.begin();
		shader.setUniformi("environmentMap", 0);
		matrix.setToProjection(.1f, 10f, 90, 1);
		shader.setUniformMatrix("projection", matrix);
		matrix.setToLookAt(side.direction, side.up);
		shader.setUniformMatrix("view", matrix);
		
		float roughness = (float)mip / (float)(maxMipLevels - 1);
	    shader.setUniformf("roughness", roughness);
		
	    boxMesh.render(shader, GL20.GL_TRIANGLES);
	}
	
	private Cubemap createRadianceLevel(Cubemap cubemap, int level){
		int mip = level;
		int maxMipLevels = MathUtils.round((float)(Math.log(cubemap.getWidth()) / Math.log(2.0)));
		int size = 1 << (maxMipLevels - mip);
		return createRadianceLevel(cubemap, size, mip, maxMipLevels);
	}
	
	private Cubemap createRadianceLevel(Cubemap cubemap, int size, int mip, int maxMipLevels){
		FrameBufferCubemap fbo = new FrameBufferCubemap(Format.RGBA8888, size, size, false){
			@Override
			protected void disposeColorTexture(Cubemap colorTexture) {
			}
		};

		// Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		cubemap.bind();
		fbo.begin();
		while(fbo.nextSide()){
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			CubemapSide side = fbo.getSide();
			renderSideRadiance(side, mip, maxMipLevels);
			
			// ici on peut capture les pixmaps
		}
		fbo.end();
		// Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Cubemap map = fbo.getColorBufferTexture();
		fbo.dispose();
		return map;
	}
	
	private Texture createRadiancePacked(Cubemap cubemap){
		int maxMipLevels = MathUtils.round((float)(Math.log(cubemap.getWidth()) / Math.log(2.0))) - 1;
		int baseSize = 1 << maxMipLevels;
		cubemap.bind();
		FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, baseSize * 4, baseSize * 4, false){
			@Override
			protected void disposeColorTexture(Texture colorTexture) {
			}
		};
		fbo.begin();
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		int ox = 0;
		int oy = 0;
		for(int level = 0 ; level<=maxMipLevels ; level++){
			int size = 1 << (maxMipLevels - level);
			int sideIndex = 0;
			for(CubemapSide side : Cubemap.CubemapSide.values()){
				int lx = sideIndex % 4;
				int ly = sideIndex / 4;
				int x = ox + lx * size;
				int y = oy + ly * size;
				Gdx.gl.glViewport(x, y, size, size);
				renderSideRadiance(side, level, maxMipLevels);
				sideIndex++;
			}
			ox += size * 2;
			oy += size;
		}
		
		
		// CUBEMAP packed format (env and irradiance):
		// -X +X -Y 
		// +Y -Z +Z
		// to be loaded via pixmap and transferd as sub pixmap
		
		// CUBEMAP mipmaps packed format (randiance): 
		// -X +X -Y +Y
		// -Z +Z mipmaps
		// where mipmaps is a resursice packed layout.
		// 2 pixels are unused for mipmaps cubemaps
		
		/*
		ox = 0;
		oy = baseSize * 2;
		{
			int size = baseSize / 4;
			int sideIndex = 0;
			for(CubemapSide side : Cubemap.CubemapSide.values()){
				int lx = sideIndex % 4;
				int ly = sideIndex / 4;
				int x = ox + lx * size;
				int y = oy + ly * size;
				Gdx.gl.glViewport(x, y, size, size);
				renderSideIrradiance(side);
				sideIndex++;
			}
			ox += size * 2;
			oy += size;
		}
		*/
		
		fbo.end();
		Texture map = fbo.getColorBufferTexture();
		fbo.dispose();
		
		return map;
	}
	
}
