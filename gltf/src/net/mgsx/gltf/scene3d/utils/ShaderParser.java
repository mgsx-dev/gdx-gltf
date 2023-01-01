package net.mgsx.gltf.scene3d.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;

import static java.lang.String.format;
import static net.mgsx.gltf.constant.CommonConstants.EOL;

/**
 * ShaderParser allows to recursively load shader code split into several files.
 * <p>
 * It brings support for file inclusion like: <pre>#include&lt;part.glsl&gt;</pre>
 * <p>
 * Given paths are relative to the file declaring the include statement.
 *
 * @author mgsx
 */
public class ShaderParser {

  private static final String INCLUDE_BEFORE = "#include <";
  private static final String INCLUDE_AFTER = ">";

  public static String parse(FileHandle file) {
    String content = file.readString();
    String[] lines = content.split(EOL);
    StringBuilder result = new StringBuilder();
    for (String line : lines) {
      String cleanLine = line.trim();

      if (cleanLine.startsWith(INCLUDE_BEFORE)) {
        int end = cleanLine.indexOf(INCLUDE_AFTER, INCLUDE_BEFORE.length());
        if (end < 0) {
          throw new GdxRuntimeException("malformed include: " + cleanLine);
        }
        String path = cleanLine.substring(INCLUDE_BEFORE.length(), end);
        FileHandle subFile = file.sibling(path);
        result.append(EOL).append("//////// ").append(path).append(EOL);
        result.append(parse(subFile));
      } else {
        result.append(line).append(EOL);
      }
    }
    return result.toString();
  }

  private ShaderParser() {
    throw new IllegalStateException(format("Cannot create instance of %s", getClass()));
  }
}
