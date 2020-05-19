package net.mgsx.gltf.ibl;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.lwjgl.opengl.GL30;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.FrameBufferCubemap;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;

import net.mgsx.gltf.ibl.RGBE.Header;
import net.mgsx.gltf.scene3d.utils.FacedMultiCubemapData;

public class IBLGenerator {

	private static ShaderProgram shaderCubemapUnwrap;
	private byte[] data;
	private float exposure = 1f;
	private Texture textureRaw;
	private Pixmap pixmapRaw;
	private Header header;
	private ShaderProgram rectToCubeShader, rectToCubeShaderGamma;
	private ShapeRenderer shapes;
	private ShaderProgram irrandianceShader;
	private ShaderProgram randianceShader;
	private ShapeRenderer shapes2;
	private ShaderProgram brdfShader;

	public IBLGenerator(FileHandle file) {
		rectToCubeShader = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-make.vs.glsl"), 
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-make.fs.glsl"));
		if(!rectToCubeShader.isCompiled()) throw new GdxRuntimeException(rectToCubeShader.getLog());
		
		rectToCubeShaderGamma = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-make.vs.glsl").readString(), 
				"#define GAMMA_CORRECTION\n" + Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-make.fs.glsl").readString());
		if(!rectToCubeShaderGamma.isCompiled()) throw new GdxRuntimeException(rectToCubeShaderGamma.getLog());
		
		irrandianceShader = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-irradiance.vs.glsl"), 
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-irradiance.fs.glsl"));
		if(!irrandianceShader.isCompiled()) throw new GdxRuntimeException(irrandianceShader.getLog());
		
		randianceShader = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-radiance.vs.glsl"), 
				Gdx.files.classpath("net/mgsx/gltf/shaders/cubemap-radiance.fs.glsl"));
		if(!randianceShader.isCompiled()) throw new GdxRuntimeException(randianceShader.getLog());
		
		brdfShader = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/shaders/brdf.vs.glsl"), 
				Gdx.files.classpath("net/mgsx/gltf/shaders/brdf.fs.glsl"));
		if(!brdfShader.isCompiled()) throw new GdxRuntimeException(brdfShader.getLog());
		
		shapes = new ShapeRenderer(20, rectToCubeShader);
		shapes.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		shapes2 = new ShapeRenderer(20, rectToCubeShaderGamma);
		shapes2.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);

		
		load(file);
	}
	
	public Texture createBRDF(int size, boolean RG16){
		// TODO RG16... or packed in a RGBA8888 ?
		FrameBuffer fbo;
		if(RG16){
			FrameBufferBuilder fbb = new FrameBufferBuilder(size, size);
			fbb.addColorTextureAttachment(GL30.GL_RG16F, GL30.GL_RG, GL20.GL_FLOAT);
			fbo = fbb.build();
		}else{
			fbo = new FrameBuffer(Format.RGBA8888, size, size, false){
				@Override
				protected void disposeColorTexture(Texture colorTexture) {
				}
			};
		}
		
		fbo.begin();
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		ShapeRenderer shapes = new ShapeRenderer(20, brdfShader);
		shapes.getProjectionMatrix().setToOrtho2D(0, 1, 1, -1);
		shapes.begin(ShapeType.Filled);
		shapes.rect(0, 0, 1, 1);
		shapes.end();
		shapes.dispose();
		
		fbo.end();
		Texture map = fbo.getColorBufferTexture();
		if(RG16){
			fbo.getTextureAttachments().clear();
			// XXX fbo.dispose();
		}else{
			fbo.dispose();
		}
		
		return map;
	}
	
	public Pixmap createBRDFPacked(Texture brdf){
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
	

	public void load(FileHandle file){
		// decode
		try{
			DataInputStream in = new DataInputStream(new BufferedInputStream(file.read()));
	        header = RGBE.readHeader(in);
	        data = new byte[header.getWidth() * header.getHeight() * 4];
	        RGBE.readPixelsRawRLE(in, data, 0, header.getWidth(), header.getHeight());
	        in.close();
		}catch(IOException e){
			throw new GdxRuntimeException(e);
		}
	}
	
	public IBLGenerator setExposure(float exposure){
		this.exposure = exposure;
		return this;
	}
	
	public Texture createRaw(){
		if(textureRaw == null){
			// convert to pixmap applying optional exposure
	        float [] pixels = new float[3];
	        int imageWidth = header.getWidth();
	        int imageHeight = header.getHeight();
	        pixmapRaw = new Pixmap(imageWidth, imageHeight, Format.RGBA8888); // TODO no need alpha...
	        pixmapRaw.setBlending(Blending.None);
			for(int y=0 ; y<imageHeight ; y++){
				for(int x=0 ; x<imageWidth ; x++){
					int idx = (y*imageWidth+x)*4;
					RGBE.rgbe2float(pixels, data, idx); // TODO exposure should be done in this call for best precision.
					pixels[0] = patch(pixels[0]);
					pixels[1] = patch(pixels[1]);
					pixels[2] = patch(pixels[2]);
					int c = Color.rgba8888(pixels[0], pixels[1], pixels[2], 1);
					pixmapRaw.drawPixel(x, y, c);
				}
			}
			textureRaw = new Texture(pixmapRaw);
		}
		return textureRaw;
	}
	public Cubemap createEnv(){
		int size = textureRaw.getWidth() / 4;
		return createEnv(size);
	}
	public Cubemap createEnv(int size){
		FrameBufferCubemap fbo = new FrameBufferCubemap(Format.RGBA8888, size, size, false){
			@Override
			protected void disposeColorTexture(Cubemap colorTexture) {
			}
		};

		textureRaw.bind();
		fbo.begin();
		while(fbo.nextSide()){
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			CubemapSide side = fbo.getSide();
			renderSide(side, false);
			
			// ici on peut capture les pixmaps
		}
		fbo.end();
		Cubemap map = fbo.getColorBufferTexture();
		fbo.dispose();
		return map;
	}
	
	public Cubemap createEnv(int size, boolean gamma) {
		FrameBufferCubemap fbo = new FrameBufferCubemap(Format.RGBA8888, size, size, false){
			@Override
			protected void disposeColorTexture(Cubemap colorTexture) {
			}
		};

		textureRaw.bind();
		fbo.begin();
		while(fbo.nextSide()){
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			CubemapSide side = fbo.getSide();
			renderSide(side, gamma);
			
			// ici on peut capture les pixmaps
		}
		fbo.end();
		Cubemap map = fbo.getColorBufferTexture();
		fbo.dispose();
		return map;
	}
	
	public Array<Texture> createEnvSeparated() {
		int size = textureRaw.getWidth() / 4;
		return createEnvSeparated(size);
	}
	public Array<Texture> createEnvSeparated(int size) {
		Array<Texture> textures = new Array<Texture>();
		for(CubemapSide side : Cubemap.CubemapSide.values()){
			FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, size, size, false){
				@Override
				protected void disposeColorTexture(Texture colorTexture) {
				}
			};
			textureRaw.bind();
			fbo.begin();
			
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			renderSide(side, false);
			
			fbo.end();
			Texture map = fbo.getColorBufferTexture();
			fbo.dispose();
			
			textures.add(map);
		}
		return textures;
	}
	public Cubemap createIrradiance(Cubemap cubemap){
		return createIrradiance(cubemap, cubemap.getWidth());
	}
	public Cubemap createIrradiance(Cubemap cubemap, int size){
		FrameBufferCubemap fbo = new FrameBufferCubemap(Format.RGBA8888, size, size, false){
			@Override
			protected void disposeColorTexture(Cubemap colorTexture) {
			}
		};

		// Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		cubemap.bind();
		fbo.begin();
		while(fbo.nextSide()){
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			CubemapSide side = fbo.getSide();
			renderSideIrradiance(side);
			
			// ici on peut capture les pixmaps
		}
		fbo.end();
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Cubemap map = fbo.getColorBufferTexture();
		fbo.dispose();
		return map;
	}
	public Cubemap createRadiance(Cubemap cubemap){
		int mipMapLevels = MathUtils.round((float)(Math.log(cubemap.getWidth()) / Math.log(2.0)));
		Pixmap[] maps = new Pixmap[mipMapLevels * 6];
		int index = 0;
		for(int level=0 ; level<mipMapLevels ; level++){
			int size = 1 << (mipMapLevels - level - 1);
			FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, size, size, false);
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
	public Cubemap createRadiance(Cubemap cubemap, int level){
		int mip = level;
		int maxMipLevels = MathUtils.round((float)(Math.log(cubemap.getWidth()) / Math.log(2.0)));
		int size = 1 << (maxMipLevels - mip);
		return createRadiance(cubemap, size, mip, maxMipLevels);
	}
	
	public Cubemap createRadiance(Cubemap cubemap, int size, int mip, int maxMipLevels){
		FrameBufferCubemap fbo = new FrameBufferCubemap(Format.RGBA8888, size, size, false){
			@Override
			protected void disposeColorTexture(Cubemap colorTexture) {
			}
		};

		// Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
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
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Cubemap map = fbo.getColorBufferTexture();
		fbo.dispose();
		return map;
	}
	
	public Texture createRadiancePacked(Cubemap cubemap){
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
		
		fbo.end();
		Texture map = fbo.getColorBufferTexture();
		fbo.dispose();
		
		return map;
	}
	
	private static final Vector3 localDir = new Vector3();
	private static final Vector3 localUp = new Vector3();
	private static final Matrix4 matrix = new Matrix4();

	public void renderSideRadiance(CubemapSide side, int mip, int maxMipLevels) {
		
		MeshBuilder mb = new MeshBuilder();
		mb.begin(Usage.Position, GL20.GL_TRIANGLES);
		// mb.setVertexTransform(matrix);
		BoxShapeBuilder.build(mb, 0,0,0,1,1,1);
		Mesh mesh = mb.end();
		
		ShaderProgram shader = randianceShader;
		
		shader.begin();
		shader.setUniformi("environmentMap", 0);
		matrix.setToProjection(.1f, 10f, 90, 1);
		shader.setUniformMatrix("projection", matrix);
		matrix.setToLookAt(side.direction, side.up);
		shader.setUniformMatrix("view", matrix);
		
		float roughness = (float)mip / (float)(maxMipLevels - 1);
	    shader.setUniformf("roughness", roughness);
		
		mesh.render(shader, GL20.GL_TRIANGLES);
		
		mesh.dispose();
	}
	
	public void renderSideIrradiance(CubemapSide side) {
		
		MeshBuilder mb = new MeshBuilder();
		mb.begin(Usage.Position, GL20.GL_TRIANGLES);
		BoxShapeBuilder.build(mb, 0,0,0,1,1,1);
		Mesh mesh = mb.end();
		
		ShaderProgram shader = irrandianceShader;
		
		shader.begin();
		shader.setUniformi("environmentMap", 0);
		matrix.setToProjection(.1f, 10f, 90, 1);
		shader.setUniformMatrix("projection", matrix);
		matrix.setToLookAt(side.direction, side.up);
		shader.setUniformMatrix("view", matrix);
		
		mesh.render(shader, GL20.GL_TRIANGLES);
		
		mesh.dispose();
	}
	
	public void renderSide(CubemapSide side, boolean gamma) {
		
		localDir.set(side.direction);
		localUp.set(side.up);
		
		// XXX patch
		if(side == CubemapSide.NegativeX || side == CubemapSide.PositiveX){
			localDir.x = -localDir.x;
		}
			
		matrix.setToLookAt(localDir, localUp);//.tra();
		localDir.set(1,0,0).scl(-1, -1, 1).mul(matrix); // XXX patch again
		localUp.set(0,1,0).scl(-1, -1, 1).mul(matrix); // XXX patch again
		
		
		ShaderProgram shader = gamma ? rectToCubeShaderGamma : rectToCubeShader;
		
		shader.begin();
		shader.setUniformi("u_hdr", 0);
		//matrix.idt();
		Vector3 xf = new Vector3();
		Vector3 yf = new Vector3();
		if(side == CubemapSide.PositiveZ){
			//matrix.setToRotation(Vector3.X, 0);
		}
		if(side == CubemapSide.NegativeZ){
			xf.set(1, 0, 0);
			yf.set(0, 1, 0);
		}
		if(side == CubemapSide.PositiveX){
		}
		if(side == CubemapSide.NegativeX){
		}
		shader.setUniformMatrix("u_mat", matrix);

		ShapeRenderer shapes = gamma ? shapes2 : this.shapes;
		shapes.begin(ShapeType.Filled);
		shapes.rect(0, 0, 1, 1);
		shapes.end();
		
	}

	private float patch(float v){
		// see https://hdrihaven.com/hdri/?c=outdoor&h=kloppenheim_02
		v = (float)Math.pow(v, exposure); // here adjust exponent!!!!
		v = MathUtils.clamp(v, 0, 1);
		return v;
	}

	// TODO not used..
	public static ShaderProgram shaderCubemapUnwrap() {
		if(shaderCubemapUnwrap != null) return shaderCubemapUnwrap;
		shaderCubemapUnwrap = new ShaderProgram(
				Gdx.files.classpath("net/mgsx/gltf/demo/shaders/cubemap-unwrap.vs.glsl"), 
				Gdx.files.classpath("net/mgsx/gltf/demo/shaders/cubemap-unwrap.fs.glsl"));
		if(!shaderCubemapUnwrap.isCompiled()) throw new GdxRuntimeException(shaderCubemapUnwrap.getLog());
		return shaderCubemapUnwrap;
	}

}
