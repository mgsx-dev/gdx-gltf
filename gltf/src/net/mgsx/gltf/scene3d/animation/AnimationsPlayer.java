package net.mgsx.gltf.scene3d.animation;

import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.scene3d.scene.Scene;

public class AnimationsPlayer {

	private Scene scene;
	
	private Array<AnimationController> controllers = new Array<AnimationController>();

	public AnimationsPlayer(Scene scene) {
		this.scene = scene;
	}
	
	public void playAll(){
		if(scene.animationController != null){
			scene.animationController.setAnimation(null);
		}
		controllers.clear();
		for(int i=0, n=count() ; i<n ; i++){
			AnimationControllerHack c = new AnimationControllerHack(scene.modelInstance);
			c.calculateTransforms = false;
			c.setAnimation(scene.modelInstance.animations.get(i));
			controllers.add(c);
		}
	}
	
	private int count() {
		return scene.modelInstance.animations.size;
	}

	public void stopAll(){
		controllers.clear();
		if(scene.animationController != null){
			scene.animationController.setAnimation(null);
		}
	}
	
	public void update(float delta){
		if(controllers.size > 0){
			for(AnimationController controller : controllers){
				controller.update(delta);
			}
			scene.modelInstance.calculateTransforms();
		}else{
			if(scene.animationController != null){
				scene.animationController.update(delta);
			}
		}
	}

}
