package net.mgsx.gltf.loaders.shared.data;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.texture.GLTFImage;

import java.nio.ByteBuffer;

public interface DataFileResolver {

  void load(FileHandle file);

  GLTF getRoot();

  ByteBuffer getBuffer(int buffer);

  Pixmap load(GLTFImage glImage);
}
