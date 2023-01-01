package net.mgsx.gltf.loaders.gltf;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.data.GLTFBuffer;
import net.mgsx.gltf.data.data.GLTFBufferView;
import net.mgsx.gltf.data.texture.GLTFImage;
import net.mgsx.gltf.loaders.exceptions.GLTFIllegalException;
import net.mgsx.gltf.loaders.shared.data.DataFileResolver;
import net.mgsx.gltf.loaders.shared.texture.PixmapBinaryLoaderHack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class SeparatedDataFileResolver implements DataFileResolver {

  private final ObjectMap<Integer, ByteBuffer> bufferMap = new ObjectMap<>();
  private GLTF glModel;
  private FileHandle path;

  @Override
  public void load(FileHandle file) {
    glModel = new Json().fromJson(GLTF.class, file);
    path = file.parent();
    loadBuffers(path);
  }

  @Override
  public GLTF getRoot() {
    return glModel;
  }

  private ObjectMap<Integer, ByteBuffer> loadBuffers(FileHandle path) {
    if (glModel.buffers != null) {
      for (int i = 0; i < glModel.buffers.size; i++) {
        GLTFBuffer glBuffer = glModel.buffers.get(i);
        ByteBuffer buffer = ByteBuffer.allocate(glBuffer.byteLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        if (glBuffer.uri.startsWith("data:")) {
          // data:application/octet-stream;base64,
          String[] headerBody = glBuffer.uri.split(",", 2);
          String body = headerBody[1];
          byte[] data = Base64Coder.decode(body);
          buffer.put(data);
        } else {
          FileHandle file = path.child(decodePath(glBuffer.uri));
          buffer.put(file.readBytes());
        }
        bufferMap.put(i, buffer);
      }
    }
    return bufferMap;
  }

  private String decodePath(String uri) {
    byte[] src = uri.getBytes();
    byte[] bytes = new byte[src.length];
    int pos = 0;
    for (int i = 0; i < src.length; i++) {
      byte c = src[i];
      if (c == '%') {
        int code = Integer.parseInt(uri.substring(i + 1, i + 3), 16);
        bytes[pos++] = (byte) code;
        i += 2;
      } else {
        bytes[pos++] = c;
      }
    }
    return new String(bytes, 0, pos, StandardCharsets.UTF_8);
    // TODO following code is cleaner but not emulated by libgdx GWT backend.
//		try {
//			return URLDecoder.decode(uri, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			throw new GdxRuntimeException(e);
//		}
  }

  @Override
  public ByteBuffer getBuffer(int buffer) {
    return bufferMap.get(buffer);
  }

  @Override
  public Pixmap load(GLTFImage glImage) {
    if (glImage.uri == null) {
      // load from buffer view
      if (glImage.mimeType == null) {
        throw new GLTFIllegalException("GLTF image: both URI and mimeType cannot be null");
      }
      if (glImage.mimeType.equals("image/png") || glImage.mimeType.equals("image/jpeg")) {
        GLTFBufferView bufferView = glModel.bufferViews.get(glImage.bufferView);
        ByteBuffer data = bufferMap.get(bufferView.buffer, null);
        byte[] bytes = new byte[bufferView.byteLength];
        data.position(bufferView.byteOffset);
        data.get(bytes, 0, bufferView.byteLength);
        data.rewind();
        return PixmapBinaryLoaderHack.load(bytes, 0, bytes.length);
      } else {
        throw new GLTFIllegalException("GLTF image: unexpected mimeType: " + glImage.mimeType);
      }
    } else if (glImage.uri.startsWith("data:")) {
      // data:application/octet-stream;base64,
      String[] headerBody = glImage.uri.split(",", 2);
      String header = headerBody[0];
      System.out.println(header);
      String body = headerBody[1];
      byte[] data = Base64Coder.decode(body);
      return PixmapBinaryLoaderHack.load(data, 0, data.length);
    } else {
      return new Pixmap(path.child(decodePath(glImage.uri)));
    }
  }

  public FileHandle getImageFile(GLTFImage glImage) {
    if (glImage.uri != null && !glImage.uri.startsWith("data:")) {
      return path.child(decodePath(glImage.uri));
    }
    return null;
  }
}
