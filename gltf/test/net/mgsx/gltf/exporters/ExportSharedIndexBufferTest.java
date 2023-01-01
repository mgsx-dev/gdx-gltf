package net.mgsx.gltf.exporters;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Matrix4;

public class ExportSharedIndexBufferTest extends Game {

  public static void main(String[] args) {
    new LwjglApplication(new ExportSharedIndexBufferTest());
  }

  @Override
  public void create() {
    Material material = new Material();
    ModelBuilder mb = new ModelBuilder();
    MeshPartBuilder mpb;
    mb.begin();

    mpb = mb.part("part1", GL20.GL_TRIANGLES, Usage.Position, material);
    BoxShapeBuilder.build(mpb, 1, 1, 1);

    mpb = mb.part("part2", GL20.GL_TRIANGLES, Usage.Position, material);
    mpb.setVertexTransform(new Matrix4().setToTranslation(2, 0, 0));
    BoxShapeBuilder.build(mpb, 1, 1, 1);

    Model model = mb.end();
    new GLTFExporter().export(model, Gdx.files.absolute("/tmp/ExportSharedIndexBufferTest.gltf"));
    Gdx.app.exit();
  }
}
