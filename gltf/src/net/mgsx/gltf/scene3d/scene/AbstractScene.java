package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.utils.ObjectMap;
import net.mgsx.gltf.scene3d.animation.AnimationsPlayer;

public interface AbstractScene extends RenderableProvider, Updatable {

    Camera getCamera(String name);

    BaseLight getLight(String name);

    int getDirectionalLightCount();

    ModelInstance getModelInstance();

    void setModelInstance(ModelInstance modelInstance);

    AnimationController getAnimationController();

    void setAnimationController(AnimationController animationController);

    ObjectMap<Node, BaseLight> getLights();

    ObjectMap<Node, Camera> getCameras();

    AnimationsPlayer getAnimations();
}
