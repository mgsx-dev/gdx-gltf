
package net.mgsx.gltf.ibl.model;

import java.io.*;
import java.nio.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.utils.*;

import net.mgsx.gltf.ibl.exceptions.*;
import net.mgsx.gltf.ibl.io.*;
import net.mgsx.gltf.ibl.io.RGBE.*;

public class IBLComposer implements Disposable {

	public Header hdrHeader;
	private byte[] hdrData;
	private Pixmap pixmapRaw;
	private Texture textureRaw;
	private EnvironmentBaker environmentBaker;
	private IrradianceBaker irradianceBaker;
	private RadianceBaker radianceBaker;
	private Cubemap irradianceMap;
	private Cubemap radianceMap;
	private BRDFBaker brdfBaker;
	private Texture brdfMap;
	private Texture builtinBRDF;

	public IBLComposer() {
		environmentBaker = new EnvironmentBaker();
		irradianceBaker = new IrradianceBaker();
		radianceBaker = new RadianceBaker();
		brdfBaker = new BRDFBaker();
		builtinBRDF = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
	}

	public void loadHDR(FileHandle file) throws IOException {
		DataInputStream in = null;
		try {
			in = new DataInputStream(new BufferedInputStream(file.read()));
			hdrHeader = RGBE.readHeader(in);
			hdrData = new byte[hdrHeader.getWidth() * hdrHeader.getHeight() * 4];
			RGBE.readPixelsRawRLE(in, hdrData, 0, hdrHeader.getWidth(), hdrHeader.getHeight());
		} finally {
			if (in != null) in.close();
		}
	}

	/** @param file the file to load
	 * @param flipV set true to flip the hdr image verically */
	public void loadHDR(FileHandle file, boolean flipV) throws IOException {
		loadHDR(file);

		if (flipV) {
			final int h = hdrHeader.getHeight();
			final int w = hdrHeader.getWidth();

			int scanlineBytes = w * 4;
			byte[] pixrow = new byte[scanlineBytes];

			// Flip the image vertically (ExtraFast, uses only one scanline buffer (Mik here..))
			for (int i = 0; i < (h / 2); i++) {
				// copy top to scanline
				System.arraycopy(hdrData, i * scanlineBytes, pixrow, 0, scanlineBytes);

				// copy bottom to top
				System.arraycopy(hdrData, (h - i - 1) * scanlineBytes, hdrData, i * scanlineBytes, scanlineBytes);

				// copy scanline to bottom
				System.arraycopy(pixrow, 0, hdrData, (h - i - 1) * scanlineBytes, scanlineBytes);
			}
		}
	}

	@Override
	public void dispose() {
		if (pixmapRaw != null) pixmapRaw.dispose();
		if (textureRaw != null) textureRaw.dispose();
		if (irradianceMap != null) irradianceMap.dispose();
		environmentBaker.dispose();
		irradianceBaker.dispose();
		radianceBaker.dispose();
		brdfBaker.dispose();
	}

	public Texture getHDRTexture() {
		if (textureRaw == null) {
			// convert to pixmap applying optional exposure
			float[] pixels = new float[3];
			int imageWidth = hdrHeader.getWidth();
			int imageHeight = hdrHeader.getHeight();

			// XXX
			boolean classicMode = false;

			if (classicMode) {
				pixmapRaw = new Pixmap(imageWidth, imageHeight, Format.RGB888);
				pixmapRaw.setBlending(Blending.None);
				Color color = new Color();
				for (int y = 0; y < imageHeight; y++) {
					for (int x = 0; x < imageWidth; x++) {
						int idx = (y * imageWidth + x) * 4;
						RGBE.rgbe2float(pixels, hdrData, idx); // TODO exposure should be done in this call for best precision.
						/*
						 * for(int i=0 ; i<3 ; i++){ pixels[i] = (float)Math.pow(pixels[i], 0.5f); }
						 */
						color.set(pixels[0], pixels[1], pixels[2], 1);
						pixmapRaw.drawPixel(x, y, Color.rgba8888(color));
					}
				}
				textureRaw = new Texture(pixmapRaw);
			} else {
				GLOnlyTextureData data = new GLOnlyTextureData(hdrHeader.getWidth(), hdrHeader.getHeight(), 0, GL30.GL_RGB32F, GL30.GL_RGB, GL30.GL_FLOAT);
				textureRaw = new Texture(data);
				FloatBuffer buffer = BufferUtils.newFloatBuffer(imageWidth * imageHeight * 3);
				for (int i = 0; i < hdrData.length; i += 4) {
					RGBE.rgbe2float(pixels, hdrData, i);
					buffer.put(pixels);
				}
				buffer.flip();
				textureRaw.bind();
				Gdx.gl.glTexImage2D(textureRaw.glTarget, 0, GL30.GL_RGB32F, hdrHeader.getWidth(), hdrHeader.getHeight(), 0, GL30.GL_RGB, GL30.GL_FLOAT, buffer);
			}

		}
		return textureRaw;
	}

	public Cubemap getEnvMap(int size, float exposure) {
		getHDRTexture();
		return environmentBaker.getEnvMap(textureRaw, size, exposure);
	}

	public Array<Pixmap> getEnvMapPixmaps(int size, float exposure) {
		getHDRTexture();
		return environmentBaker.createEnvMapPixmaps(textureRaw, size, exposure);
	}

	public Cubemap getIrradianceMap(int size) {
		Cubemap cubemap = environmentBaker.getLastMap(); // getEnvMap(size, exposure);
		if (irradianceMap != null) irradianceMap.dispose();
		try {
			irradianceMap = irradianceBaker.createIrradiance(cubemap, size);
		} catch (IllegalStateException e) {
			irradianceMap = new Cubemap(1, 1, 1, Format.RGB888);
			throw new FrameBufferError(e);
		}
		return irradianceMap;
	}

	public Array<Pixmap> getIrradianceMapPixmaps(int size) {
		Cubemap cubemap = environmentBaker.getLastMap();
		return irradianceBaker.createPixmaps(cubemap, size);
	}

	public Array<Pixmap> getRadianceMapPixmaps(int size) {
		Cubemap cubemap = environmentBaker.getLastMap();
		return radianceBaker.createPixmaps(cubemap, size);
	}

	public Cubemap getRadianceMap(int size) {
		Cubemap cubemap = environmentBaker.getLastMap(); // getEnvMap(size, exposure);
		if (radianceMap != null) radianceMap.dispose();
		try {
			radianceMap = radianceBaker.createRadiance(cubemap, size);
		} catch (IllegalStateException e) {
			radianceMap = new Cubemap(1, 1, 1, Format.RGB888);
			throw new FrameBufferError(e);
		}
		return radianceMap;
	}

	public Texture getBRDFMap(int size, boolean rg16) {
		if (brdfMap != null && brdfMap != builtinBRDF) brdfMap.dispose();
		try {
			brdfMap = brdfBaker.createBRDF(size, rg16);
		} catch (IllegalStateException e) {
			brdfMap = new Texture(1, 1, Format.RGB888);
			throw new FrameBufferError(e);
		}
		return brdfMap;
	}

	public Texture getDefaultBRDFMap() {
		return builtinBRDF;
	}

	public Pixmap getBRDFPixmap(int size, boolean brdf16) {
		return brdfBaker.createBRDFPixmap(size, brdf16);
	}

}
