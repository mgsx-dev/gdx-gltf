package net.mgsx.gltf.scene3d.animation;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.utils.Array;

import net.mgsx.gltf.scene3d.scene.Scene;

public class AnimationsPlayer {

	private Scene scene;
	
	private Array<AnimationController> controllers = new Array<AnimationController>();

	public AnimationsPlayer(Scene scene) {
		this.scene = scene;
	}
	
	public void addAnimations(Array<AnimationDesc> animations){
		for(AnimationDesc animation : animations){
			addAnimation(animation);
		}
	}
	public void addAnimation(AnimationDesc animation){
		AnimationControllerHack c = new AnimationControllerHack(scene.getModelInstance());
		c.calculateTransforms = false;
		c.setAnimationDesc(animation);
		controllers.add(c);
	}
	public void removeAnimation(Animation animation){
		for(int i=controllers.size-1 ; i>=0 ; i--){
			if(controllers.get(i).current != null && controllers.get(i).current.animation == animation){
				controllers.removeIndex(i);
			}
		}
	}
	
	public void clearAnimations(){
		controllers.clear();
		if(scene.getAnimationController() != null){
			scene.getAnimationController().setAnimation(null);
		}
	}
	
	public void playAll(){
		playAll(false);
	}
	public void loopAll(){
		playAll(true);
	}
	public void playAll(boolean loop){
		clearAnimations();
		for(int i=0, n=scene.getModelInstance().animations.size ; i<n ; i++){
			AnimationControllerHack c = new AnimationControllerHack(scene.getModelInstance());
			c.calculateTransforms = false;
			c.setAnimation(scene.getModelInstance().animations.get(i), loop ? -1 : 1);
			controllers.add(c);
		}
	}
	
	public void stopAll(){
		clearAnimations();
	}
	
	public void update(float delta){
		if(controllers.size > 0){
			for(AnimationController controller : controllers){
				controller.update(delta);
			}
			scene.getModelInstance().calculateTransforms();
		}else{
			if(scene.getAnimationController() != null){
				scene.getAnimationController().update(delta);
			}
		}
	}

}
