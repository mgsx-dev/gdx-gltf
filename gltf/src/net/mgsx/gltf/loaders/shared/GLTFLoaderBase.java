package net.mgsx.gltf.loaders.shared;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.ObjectSet;
import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.camera.GLTFCamera;
import net.mgsx.gltf.data.extensions.KHRLightsPunctual;
import net.mgsx.gltf.data.extensions.KHRLightsPunctual.GLTFLight;
import net.mgsx.gltf.data.extensions.KHRMaterialsEmissiveStrength;
import net.mgsx.gltf.data.extensions.KHRMaterialsIOR;
import net.mgsx.gltf.data.extensions.KHRMaterialsIridescence;
import net.mgsx.gltf.data.extensions.KHRMaterialsPBRSpecularGlossiness;
import net.mgsx.gltf.data.extensions.KHRMaterialsSpecular;
import net.mgsx.gltf.data.extensions.KHRMaterialsTransmission;
import net.mgsx.gltf.data.extensions.KHRMaterialsUnlit;
import net.mgsx.gltf.data.extensions.KHRMaterialsVolume;
import net.mgsx.gltf.data.extensions.KHRTextureTransform;
import net.mgsx.gltf.data.scene.GLTFNode;
import net.mgsx.gltf.data.scene.GLTFScene;
import net.mgsx.gltf.loaders.exceptions.GLTFUnsupportedException;
import net.mgsx.gltf.loaders.shared.animation.AnimationLoader;
import net.mgsx.gltf.loaders.shared.data.DataFileResolver;
import net.mgsx.gltf.loaders.shared.data.DataResolver;
import net.mgsx.gltf.loaders.shared.geometry.MeshLoader;
import net.mgsx.gltf.loaders.shared.material.MaterialLoader;
import net.mgsx.gltf.loaders.shared.material.PBRMaterialLoader;
import net.mgsx.gltf.loaders.shared.scene.NodeResolver;
import net.mgsx.gltf.loaders.shared.scene.SkinLoader;
import net.mgsx.gltf.loaders.shared.texture.ImageResolver;
import net.mgsx.gltf.loaders.shared.texture.TextureResolver;
import net.mgsx.gltf.scene3d.model.NodePlus;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneModel;

public class GLTFLoaderBase implements Disposable {

  public static final String TAG = "GLTF";

  public static final ObjectSet<String> supportedExtensions = new ObjectSet<>();

  static {
    supportedExtensions.addAll(
      KHRMaterialsPBRSpecularGlossiness.EXT,
      KHRTextureTransform.EXT,
      KHRLightsPunctual.EXT,
      KHRMaterialsUnlit.EXT,
      KHRMaterialsTransmission.EXT,
      KHRMaterialsVolume.EXT,
      KHRMaterialsIOR.EXT,
      KHRMaterialsSpecular.EXT,
      KHRMaterialsIridescence.EXT,
      KHRMaterialsEmissiveStrength.EXT
    );
  }

  private static final ObjectSet<Material> materialSet = new ObjectSet<>();
  private static final ObjectSet<MeshPart> meshPartSet = new ObjectSet<>();
  private static final ObjectSet<Mesh> meshSet = new ObjectSet<>();
  private final ObjectSet<Mesh> loadedMeshes = new ObjectSet<>();

  private final Array<Camera> cameras = new Array<>();
  private final Array<BaseLight<?>> lights = new Array<>();

  /**
   * node name to light index
   */
  private final ObjectMap<String, Integer> lightMap = new ObjectMap<>();


  /**
   * node name to camera index
   */
  private final ObjectMap<String, Integer> cameraMap = new ObjectMap<>();

  private final Array<SceneModel> scenes = new Array<>();

  protected GLTF glModel;

  protected DataFileResolver dataFileResolver;
  protected MaterialLoader materialLoader;
  protected TextureResolver textureResolver;
  protected AnimationLoader animationLoader;
  protected DataResolver dataResolver;
  protected SkinLoader skinLoader;
  protected NodeResolver nodeResolver;
  protected MeshLoader meshLoader;
  protected ImageResolver imageResolver;

  public GLTFLoaderBase() {
    this(null);
  }

  public GLTFLoaderBase(TextureResolver textureResolver) {
    this.textureResolver = textureResolver;
    animationLoader = new AnimationLoader();
    nodeResolver = new NodeResolver();
    meshLoader = new MeshLoader();
    skinLoader = new SkinLoader();
  }

  public SceneAsset load(DataFileResolver dataFileResolver, boolean withData) {
    try {
      this.dataFileResolver = dataFileResolver;

      glModel = dataFileResolver.getRoot();

      // prerequisites (mandatory)
      if (glModel.extensionsRequired != null) {
        for (String extension : glModel.extensionsRequired) {
          if (!supportedExtensions.contains(extension)) {
            throw new GLTFUnsupportedException("Extension " + extension + " required but not supported");
          }
        }
      }
      // prerequisites (optional)
      if (glModel.extensionsUsed != null) {
        for (String extension : glModel.extensionsUsed) {
          if (!supportedExtensions.contains(extension)) {
            Gdx.app.error(TAG, "Extension " + extension + " used but not supported");
          }
        }
      }

      // load deps from lower to higher

      // images (pixmaps)
      dataResolver = new DataResolver(glModel, dataFileResolver);

      if (textureResolver == null) {
        imageResolver = new ImageResolver(dataFileResolver); // TODO no longer necessary
        imageResolver.load(glModel.images);
        textureResolver = new TextureResolver();
        textureResolver.loadTextures(glModel.textures, glModel.samplers, imageResolver);
        imageResolver.dispose();
      }

      materialLoader = new PBRMaterialLoader(textureResolver);
      // materialLoader = new DefaultMaterialLoader(textureResolver);
      materialLoader.loadMaterials(glModel.materials);

      loadCameras();
      loadLights();
      loadScenes();

      animationLoader.load(glModel.animations, nodeResolver, dataResolver);
      skinLoader.load(glModel.skins, glModel.nodes, nodeResolver, dataResolver);

      // create scene asset
      SceneAsset model = new SceneAsset();
      if (withData) model.data = glModel;
      model.scenes = scenes;
      model.scene = scenes.get(glModel.scene);
      model.maxBones = skinLoader.getMaxBones();
      model.textures = textureResolver.getTextures(new Array<Texture>());
      model.animations = animationLoader.animations;
      // XXX don't know where the animation are ...
      for (SceneModel scene : model.scenes) {
        scene.model.animations.addAll(animationLoader.animations);
      }

      model.meshes = new Array<>();
      copy(loadedMeshes, model.meshes);
      loadedMeshes.clear();

      return model;
    } catch (RuntimeException e) {
      dispose();
      throw e;
    }
  }

  private void loadLights() {
    if (glModel.extensions != null) {
      KHRLightsPunctual.GLTFLights lightExt = glModel.extensions.get(KHRLightsPunctual.GLTFLights.class, KHRLightsPunctual.EXT);
      if (lightExt != null) {
        for (GLTFLight light : lightExt.lights) {
          lights.add(KHRLightsPunctual.map(light));
        }
      }
    }
  }

  @Override
  public void dispose() {
    if (imageResolver != null) {
      imageResolver.dispose();
    }
    if (textureResolver != null) {
      textureResolver.dispose();
    }
    for (SceneModel scene : scenes) {
      scene.dispose();
    }
    for (Mesh mesh : loadedMeshes) {
      mesh.dispose();
    }
    loadedMeshes.clear();
  }

  private void loadScenes() {
    for (int i = 0; i < glModel.scenes.size; i++) {
      scenes.add(loadScene(glModel.scenes.get(i)));
    }
  }

  private void loadCameras() {
    if (glModel.cameras != null) {
      for (GLTFCamera glCamera : glModel.cameras) {
        cameras.add(GLTFTypes.map(glCamera));
      }
    }
  }

  private SceneModel loadScene(GLTFScene gltfScene) {
    SceneModel sceneModel = new SceneModel();
    sceneModel.name = gltfScene.name;
    sceneModel.model = new Model();

    // add root nodes
    if (gltfScene.nodes != null) {
      for (int id : gltfScene.nodes) {
        sceneModel.model.nodes.add(getNode(id));
      }
    }
    // add scene cameras (filter from all scenes cameras)
    for (Entry<String, Integer> entry : cameraMap) {
      Node node = sceneModel.model.getNode(entry.key, true);
      if (node != null) {
        sceneModel.cameras.put(node, cameras.get(entry.value));
      }
    }
    // add scene lights (filter from all scenes lights)
    for (Entry<String, Integer> entry : lightMap) {
      Node node = sceneModel.model.getNode(entry.key, true);
      if (node != null) {
        sceneModel.lights.put(node, lights.get(entry.value));
      }
    }

    // collect data references to store in model
    collectData(sceneModel.model, sceneModel.model.nodes);

    loadedMeshes.addAll(meshSet);

    copy(meshSet, sceneModel.model.meshes);
    copy(meshPartSet, sceneModel.model.meshParts);
    copy(materialSet, sceneModel.model.materials);

    meshSet.clear();
    meshPartSet.clear();
    materialSet.clear();

    return sceneModel;
  }

  private void collectData(Model model, Iterable<Node> nodes) {
    for (Node node : nodes) {
      for (NodePart part : node.parts) {
        meshSet.add(part.meshPart.mesh);
        meshPartSet.add(part.meshPart);
        materialSet.add(part.material);
      }
      collectData(model, node.getChildren());
    }
  }

  private static <T> void copy(ObjectSet<T> src, Array<T> dst) {
    for (T e : src) {
      dst.add(e);
    }
  }

  private Node getNode(int id) {
    Node node = nodeResolver.get(id);
    if (node == null) {
      node = new NodePlus();
      nodeResolver.put(id, node);

      GLTFNode glNode = glModel.nodes.get(id);

      if (glNode.matrix != null) {
        Matrix4 matrix = new Matrix4(glNode.matrix);
        matrix.getTranslation(node.translation);
        matrix.getScale(node.scale);
        matrix.getRotation(node.rotation, true);
      } else {
        if (glNode.translation != null) {
          GLTFTypes.map(node.translation, glNode.translation);
        }
        if (glNode.rotation != null) {
          GLTFTypes.map(node.rotation, glNode.rotation);
        }
        if (glNode.scale != null) {
          GLTFTypes.map(node.scale, glNode.scale);
        }
      }

      node.id = glNode.name == null ? "glNode " + id : glNode.name;

      if (glNode.children != null) {
        for (int childId : glNode.children) {
          node.addChild(getNode(childId));
        }
      }

      if (glNode.mesh != null) {
        meshLoader.load(node, glModel.meshes.get(glNode.mesh), dataResolver, materialLoader);
      }

      if (glNode.camera != null) {
        cameraMap.put(node.id, glNode.camera);
      }

      // node extensions
      if (glNode.extensions != null) {
        KHRLightsPunctual.GLTFLightNode nodeLight = glNode.extensions.get(KHRLightsPunctual.GLTFLightNode.class, KHRLightsPunctual.EXT);
        if (nodeLight != null) {
          lightMap.put(node.id, nodeLight.light);
        }
      }

    }
    return node;
  }
}
