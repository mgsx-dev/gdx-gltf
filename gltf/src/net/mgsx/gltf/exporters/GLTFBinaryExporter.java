package net.mgsx.gltf.exporters;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import net.mgsx.gltf.data.data.GLTFBuffer;
import net.mgsx.gltf.data.data.GLTFBufferView;
import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.loaders.exceptions.GLTFRuntimeException;
import net.mgsx.gltf.loaders.exceptions.GLTFUnsupportedException;

class GLTFBinaryExporter {
	private final Array<ByteBuffer> buffers = new Array<ByteBuffer>();
	final Array<GLTFBufferView> views = new Array<GLTFBufferView>();
	private Buffer currentBuffer;
	private FileHandle folder;
	private final GLTFExporterConfig config;
	
	public GLTFBinaryExporter(FileHandle folder, GLTFExporterConfig config) {
		this.config = config;
		this.folder = folder;
	}
	
	void reset() {
		buffers.clear();
		views.clear();
		currentBuffer = null;
	}

	private ByteBuffer createBuffer(){
		ByteBuffer buffer = ByteBuffer.allocate(config.maxBinaryFileSize);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer;
	}
	
	private ByteBuffer begin(int size){
		ByteBuffer buffer;
		if(buffers.size == 0){
			buffer = createBuffer();
			buffers.add(buffer);
		}else{
			buffer = buffers.peek();
			if(buffer.remaining() < size){
				buffer = createBuffer();
				buffers.add(buffer);
			}
		}
		return buffer;
	}
	public FloatBuffer beginFloats(int count) {
		FloatBuffer floatBuffer = begin(count * 4).asFloatBuffer();
		currentBuffer = floatBuffer;
		return floatBuffer;
	}
	
	public ShortBuffer beginShorts(int count) {
		ShortBuffer shortBuffer = begin(count * 2).asShortBuffer();
		currentBuffer = shortBuffer;
		return shortBuffer;
	}
	
	/**
	 * end local buffering
	 * @return GLTFBufferView id
	 */
	public int end() {
		GLTFBufferView view = new GLTFBufferView();
		view.buffer = buffers.size - 1;
		
		// update position
		int position = buffers.peek().position();
		view.byteOffset = position;
		int size;
		if(currentBuffer instanceof FloatBuffer){
			size = currentBuffer.position() * 4;
		}else if(currentBuffer instanceof ShortBuffer){
			size = currentBuffer.position() * 2;
		}else{
			throw new GLTFUnsupportedException("bad buffer type");
		}
		currentBuffer = null;
		view.byteLength = size;
		position += size;
		buffers.peek().position(position);
		views.add(view);
		return views.size-1;
	}

	public Array<GLTFBuffer> flushAllToFiles(String baseName){
		Array<GLTFBuffer> out = new Array<GLTFBuffer>();
		int count = 0;
		for(ByteBuffer b : buffers){
			GLTFBuffer buffer = new GLTFBuffer();
			buffer.byteLength = b.position();
			buffer.uri = buffers.size == 1 ? baseName + ".bin" : baseName + (count+1) + ".bin";
			byte[] bytes = new byte[b.position()];
			b.flip();
			b.get(bytes);
			folder.child(buffer.uri).writeBytes(bytes, false);
			out.add(buffer);
			count++;
		}
		return out;
	}

	public void export(GLTFImage image, Texture texture, String baseName) {
		String fileName = baseName + ".png";
		image.uri = fileName;
		FileHandle file = folder.child(fileName);
		FrameBuffer fbo = new FrameBuffer(texture.getTextureData().getFormat(), texture.getWidth(), texture.getHeight(), false);
		fbo.begin();
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		SpriteBatch batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
		batch.begin();
		batch.draw(texture, 0, 0, 1, 1, 0, 0, 1, 1);
		batch.end();
		Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, texture.getWidth(), texture.getHeight());
		fbo.end();
		batch.dispose();
		fbo.dispose();
		savePNG(file, pixmap);
		pixmap.dispose();
	}
	
	public static void savePNG(FileHandle file, Pixmap pixmap){
		if(Gdx.app.getType() == ApplicationType.WebGL){
			throw new GLTFUnsupportedException("saving pixmap not supported for WebGL");
		}else{
			// call PixmapIO.writePNG(file, pixmap); via reflection to
			// avoid compilation error with GWT.
			try {
				Class pixmapIO = ClassReflection.forName("com.badlogic.gdx.graphics.PixmapIO");
				Method pixmapIO_writePNG = ClassReflection.getMethod(pixmapIO, "writePNG", FileHandle.class, Pixmap.class);
				pixmapIO_writePNG.invoke(null, file, pixmap);
			} catch (ReflectionException e) {
				throw new GLTFRuntimeException(e);
			} 
		}
	}
	
}