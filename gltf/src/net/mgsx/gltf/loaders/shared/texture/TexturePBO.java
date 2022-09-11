package net.mgsx.gltf.loaders.shared.texture;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE_2D;

public class TexturePBO extends Texture {
    private int usesCount = 1;

    public TexturePBO(int glHandle, int pboHandle, Pixmap pixmap, boolean useMipMaps) {
        //we pass an empty "TextureData" so that the parent does not swear
        super(GL_TEXTURE_2D, glHandle, new TextureData() {
            @Override
            public TextureDataType getType() {
                return null;
            }

            @Override
            public boolean isPrepared() {
                return false;
            }

            @Override
            public void prepare() {

            }

            @Override
            public Pixmap consumePixmap() {
                return null;
            }

            @Override
            public boolean disposePixmap() {
                return false;
            }

            @Override
            public void consumeCustomData(int target) {

            }

            @Override
            public int getWidth() {
                return 0;
            }

            @Override
            public int getHeight() {
                return 0;
            }

            @Override
            public Pixmap.Format getFormat() {
                return null;
            }

            @Override
            public boolean useMipMaps() {
                return false;
            }

            @Override
            public boolean isManaged() {
                return false;
            }
        });
        //check if render begin
        if (Gdx.graphics.getFrameId() >= 1) {
            TextureData data = new PixmapTextureData(pixmap, null, useMipMaps, false);

            //set "data" to parent with reflection
            try {
                Field field = this.getClass().getSuperclass().getDeclaredField("data");
                field.setAccessible(true);
                field.set(this, data);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace(System.out);
            }
            //everything is the same as in the original class
            if (!data.isPrepared()) data.prepare();

            final TextureData.TextureDataType type = data.getType();
            if (type == TextureData.TextureDataType.Custom) {
                data.consumeCustomData(GL_TEXTURE_2D);
                return;
            }

            Pixmap dataPixmap = data.consumePixmap();
            boolean disposePixmap = data.disposePixmap();
            if (data.getFormat() != dataPixmap.getFormat()) {
                Pixmap tmp = new Pixmap(dataPixmap.getWidth(), dataPixmap.getHeight(), data.getFormat());
                tmp.setBlending(Pixmap.Blending.None);
                tmp.drawPixmap(dataPixmap, 0, 0, 0, 0, dataPixmap.getWidth(), dataPixmap.getHeight());
                if (data.disposePixmap()) {
                    dataPixmap.dispose();
                }
                dataPixmap = tmp;
                disposePixmap = true;
            }

            //first we load "MipMap" because for some reason it takes less time
            if (useMipMaps) {
                generateMipMapCPU(dataPixmap, dataPixmap.getWidth(), dataPixmap.getHeight(), pboHandle);
            }

            //copy data to buffer
            copyPixmapToPBO(pboHandle, dataPixmap);

            PBOtoTextureTask pBOtoTextureTask = new PBOtoTextureTask(pboHandle, this, dataPixmap, 0);
            Gdx.app.postRunnable(pBOtoTextureTask);
            synchronized (this) {
                try {
                    this.wait(3000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            //everything is the same as in the original class
            if (disposePixmap) dataPixmap.dispose();

            //call "addManagedTexture" with reflection
            if (data.isManaged()) {
                try {
                    Method method = this.getClass().getSuperclass().getDeclaredMethod("addManagedTexture", Application.class, Texture.class);
                    method.invoke(this, Gdx.app, this);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public TexturePBO(Pixmap pixmap, boolean useMipMaps) {
        super(pixmap, useMipMaps);
    }

    @Override
    public void load(TextureData textureData) {
        //if use standard loading
        if (Gdx.graphics.getFrameId() < 1)
            super.load(textureData);
    }

    public class PBOtoTextureTask implements Runnable {
        private final int pboHandle;
        private final Texture texture;
        private final Pixmap pixmap;
        private final int lvl;

        public PBOtoTextureTask(int pboHandle, Texture texture, Pixmap pixmap, int lvl) {
            this.pboHandle = pboHandle;
            this.texture = texture;
            this.pixmap = pixmap;
            this.lvl = lvl;
        }

        @Override
        public void run() {
            Gdx.gl.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, pboHandle);
            Gdx.gl30.glUnmapBuffer(GL30.GL_PIXEL_UNPACK_BUFFER);

            Gdx.gl.glBindTexture(GL_TEXTURE_2D, texture.getTextureObjectHandle());

            Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);

            Gdx.gl30.glTexImage2D(GL_TEXTURE_2D, lvl, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), null);

            if (lvl == 0) {
                unsafeSetFilter(minFilter, magFilter, true);
                unsafeSetWrap(uWrap, vWrap, true);
                unsafeSetAnisotropicFilter(anisotropicFilterLevel, true);
            }

            Gdx.gl.glBindTexture(GL_TEXTURE_2D, 0);
            Gdx.gl.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, 0);

            synchronized (texture) {
                texture.notify();
            }
        }
    }


    private void generateMipMapCPU(Pixmap pixmap, int textureWidth, int textureHeight, int pboHandle) {
        if ((Gdx.gl20 == null) && textureWidth != textureHeight)
            throw new GdxRuntimeException("texture width and height must be square when using mipmapping.");
        int width = pixmap.getWidth() / 2;
        int height = pixmap.getHeight() / 2;
        int level = 1;
        while (width > 0 && height > 0) {
            Pixmap tmp = new Pixmap(width, height, pixmap.getFormat());
            tmp.setBlending(Pixmap.Blending.None);
            tmp.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, width, height);
            if (level > 1) pixmap.dispose();
            pixmap = tmp;

            copyPixmapToPBO(pboHandle, pixmap);

            PBOtoTextureTask pBOtoTextureTask = new PBOtoTextureTask(pboHandle, this, pixmap, level);
            Gdx.app.postRunnable(pBOtoTextureTask);

            synchronized (this) {
                try {
                    this.wait(3000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            width = pixmap.getWidth() / 2;
            height = pixmap.getHeight() / 2;
            level++;
        }
    }

    static class BufferBinder implements Runnable {
        private final Buffer[] mappedBuffer;
        private final int pboHandle;
        private final int pixmapSizeBytes;
        private final Object waitObj;

        BufferBinder(Buffer[] mappedBuffer, int pboHandle, int pixmapSizeBytes, Object waitObj) {
            this.mappedBuffer = mappedBuffer;
            this.pboHandle = pboHandle;
            this.pixmapSizeBytes = pixmapSizeBytes;
            this.waitObj = waitObj;
        }

        @Override
        public void run() {
            Gdx.gl.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, pboHandle);
            mappedBuffer[0] = Gdx.gl30.glMapBufferRange(GL30.GL_PIXEL_UNPACK_BUFFER, 0, pixmapSizeBytes, GL30.GL_MAP_WRITE_BIT | GL30.GL_MAP_UNSYNCHRONIZED_BIT);
            Gdx.gl.glBindBuffer(GL30.GL_PIXEL_UNPACK_BUFFER, 0);
            synchronized (waitObj) {
                waitObj.notify();
            }
        }
    }

    private void copyPixmapToPBO(int pboHandle, Pixmap pixmap) {
        int pixmapSizeBytes = pixmap.getPixels().capacity();
        final Buffer[] mappedBuffer = new Buffer[1];
        Gdx.app.postRunnable(new BufferBinder(mappedBuffer, pboHandle, pixmapSizeBytes, this));
        synchronized (this) {
            try {
                this.wait(3000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        ByteBuffer buffer = pixmap.getPixels();
        BufferUtils.copy(buffer, mappedBuffer[0], pixmapSizeBytes);
    }


    public int getUsesCount() {
        return usesCount;
    }

    public void incrementUsesCount() {
        usesCount++;
    }

    public void decrementUsesCount() {
        usesCount--;
    }

}
