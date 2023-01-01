package net.mgsx.gltf;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Benchmark extends ApplicationAdapter {

  public static void main(String[] args) {
    new LwjglApplication(new Benchmark());
  }

  @Override
  public void create() {
    System.out.println("Consistency check...");
    run(1, false, true);

    System.out.println("Warmup...");
    run(10, false, false);

    System.out.println("Measuring...");
    run(100, true, false);

    System.out.println("Done.");
    Gdx.app.exit();
  }

  public void run(final int iterations, final boolean trace, final boolean check) {
    measure("G3DJ", iterations, trace, new Runnable() {
      @Override
      public void run() {
        Model g3dj = new G3dModelLoader(new JsonReader()).loadModel(Gdx.files.classpath("benchmark/four-spheres.g3dj"));
        if (check) assertConsistent(g3dj);
        g3dj.dispose();
      }
    });
    measure("G3DB", iterations, trace, new Runnable() {
      @Override
      public void run() {
        Model g3db = new G3dModelLoader(new UBJsonReader()).loadModel(Gdx.files.classpath("benchmark/four-spheres.g3db"));
        if (check) assertConsistent(g3db);
        g3db.dispose();
      }
    });
    measure("GLTF separated", iterations, trace, new Runnable() {
      @Override
      public void run() {
        SceneAsset assetSep = new GLTFLoader().load(Gdx.files.classpath("benchmark/separated/four-spheres.gltf"));
        if (check) assertConsistent(assetSep.scene.model);
        assetSep.dispose();
      }
    });
    measure("GLTF embeded", iterations, trace, new Runnable() {
      @Override
      public void run() {
        SceneAsset assetEmb = new GLTFLoader().load(Gdx.files.classpath("benchmark/embeded/four-spheres.gltf"));
        if (check) assertConsistent(assetEmb.scene.model);
        assetEmb.dispose();
      }
    });
    measure("GLB", iterations, trace, new Runnable() {
      @Override
      public void run() {
        SceneAsset assetBin = new GLBLoader().load(Gdx.files.classpath("benchmark/binary/four-spheres.glb"));
        if (check) assertConsistent(assetBin.scene.model);
        assetBin.dispose();
      }
    });
    measure("OBJ", iterations, trace, new Runnable() {
      @Override
      public void run() {
        Model model = new ObjLoader().loadModel(Gdx.files.classpath("benchmark/obj/four-spheres.obj"));
        if (check) assertConsistent(model);
        model.dispose();
      }
    });
  }

  protected void assertConsistent(Model model) {
    final long expectedAttributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

    assertEquals(0, model.animations.size);
    // assertEquals(1, model.materials.size); TODO GLTF should collect that?
    assertEquals(4, model.meshes.size);
    // assertEquals(1, model.meshParts.size); TODO GLTF should collect that?
    assertEquals(4, model.nodes.size);

    for (Node node : model.nodes) {
      assertEquals(0, node.getChildCount());
      assertEquals(1, node.parts.size);
      MeshPart mp = node.parts.first().meshPart;
      assertEquals(0, mp.offset);
      assertEquals(GL20.GL_TRIANGLES, mp.primitiveType);
      assertEquals(36864, mp.size);
      assertEquals(expectedAttributes, mp.mesh.getVertexAttributes().getMask());
      boolean isIndexed = mp.mesh.getNumIndices() > 0;
      if (isIndexed) { // XXX OBJ doesn't have indexed meshes
        assertEquals(24576, mp.mesh.getNumVertices());
        assertEquals(36864, mp.mesh.getNumIndices());
      } else {
        assertEquals(36864, mp.mesh.getNumVertices());
      }
    }
  }

  private void assertEquals(long expected, long actual) {
    if (expected != actual) {
      throw new GdxRuntimeException("fail: expected:" + expected + ", actual:" + actual);
    }
  }

  private void measure(String label, int iterations, boolean trace, Runnable runnable) {
    long ptime = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      runnable.run();
    }
    long ctime = System.nanoTime();
    long dtime = ctime - ptime;
    long dtimeMS = dtime / 1000000;
    float time = (float) dtimeMS / (float) iterations;

    if (trace) {
      System.out.println(label + " (" + iterations + " iterations), average time:" + time + "ms");
    }
  }
}
