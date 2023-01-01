package net.mgsx.gltf.loaders.blender;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import net.mgsx.gltf.data.geometry.GLTFMesh;

import static java.lang.String.format;

public class BlenderShapeKeys {

	/** Blender store shape key names in mesh extras.
	 * <pre>
	 *  "meshes" : [
          {
            "name" : "Plane",
            "extras" : {
                "targetNames" : [
                    "Water",
                    "Mountains"
                ]
            },
            "primitives" : ...,
            "weights" : [0.6, 0.3]
          }
        ]
        </pre>
	 */
  public static Array<String> parse(GLTFMesh glMesh) {
    if (glMesh.extras == null) {
      return null;
    }
    JsonValue targetNames = glMesh.extras.value.get("targetNames");
    if (targetNames != null && targetNames.isArray()) {
      return new Array<>(targetNames.asStringArray());
    }
    return null;
  }

  private BlenderShapeKeys() {
    throw new IllegalStateException(format("Cannot create instance of %s", getClass()));
  }
}
