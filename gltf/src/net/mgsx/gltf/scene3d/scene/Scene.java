package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;

import net.mgsx.gltf.scene3d.animation.AnimationControllerPlus;
import net.mgsx.gltf.scene3d.model.ModelInstancePlus;

public class Scene {
	public ModelInstance modelInstance;
	public AnimationController animationController;
	
	/**
	 * Default constructor create animated scene if model contains animations.
	 * use {@link #Scene(Model, boolean)} constructor to force animation management.
	 * @param model
	 */
	public Scene(Model model) {
		this(new ModelInstancePlus(model));
	}

	/**
	 * Default constructor create animated scene if model instance contains animations.
	 * use {@link #Scene(Model, boolean)} constructor to force animation management.
	 * @param modelInstance
	 */
	public Scene(ModelInstance modelInstance) {
		this(modelInstance, modelInstance.animations.size > 0);
	}

	/**
	 * Create a scene
	 * @param modelInstance
	 * @param animated
	 */
	public Scene(ModelInstance modelInstance, boolean animated) {
		super();
		this.modelInstance = modelInstance;
		if(animated){
			this.animationController = new AnimationControllerPlus(modelInstance);
		}
	}
	public Scene(Model model, boolean animated) {
		this(new ModelInstancePlus(model), animated);
	}

	public void upadte(float delta){
		if(animationController != null){
			animationController.update(delta);
		}
	}
}
