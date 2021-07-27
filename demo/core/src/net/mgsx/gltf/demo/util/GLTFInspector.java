package net.mgsx.gltf.demo.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

import net.mgsx.gltf.scene3d.animation.NodeAnimationHack;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneModel;
import net.mgsx.gltf.scene3d.utils.LightUtils;
import net.mgsx.gltf.scene3d.utils.LightUtils.LightsInfo;

public class GLTFInspector {
	
	private Array<BaseLight> allLights = new Array<BaseLight>();
	private Array<Camera> allCameras = new Array<Camera>();
	
	public void inspect(SceneAsset asset){
		allLights.clear();
		allCameras.clear();
		if(asset.scenes.size > 0) logScenes(asset);
		if(asset.animations.size > 0) logAnimations(asset);
		if(asset.textures.size > 0) logTexures(asset);
		if(allLights.size > 0) logLights(allLights);
		if(allCameras.size > 0) logCameras(allCameras);
	}
	
	private void logCameras(Array<Camera> cameras) {
		int nPerspective = 0;
		int nOrtho = 0;
		for(Camera camera : cameras){
			if(camera instanceof OrthographicCamera){
				nOrtho++;
			}else if(camera instanceof PerspectiveCamera){
				nPerspective++;
			}
		}
		log("Cameras", "count", cameras.size, "perspective", nPerspective, "orthographic", nOrtho);
	}

	private void logLights(Array<BaseLight> lights) {
		LightsInfo info = LightUtils.getLightsInfo(new LightsInfo(), lights);
		log("Lights", "count", lights.size, "dirs", info.dirLights, "points", info.pointLights, "spots", info.spotLights);
	}

	private void logScenes(SceneAsset asset) {
		int nBones = 0;
		int nNodes = 0;
		int meshNodes = 0;
		int nMeshParts = 0;
		int nMesh = 0;
		int nVert = 0;
		int nTri = 0;
		int nRealTri = 0;
		ObjectSet<Material> mapSet = new ObjectSet<Material>();
		for(SceneModel scene : asset.scenes){
			nMesh += scene.model.meshes.size;
			
			allCameras.addAll(scene.cameras.values().toArray());
			allLights.addAll(scene.lights.values().toArray());
			
			for(Mesh mesh : scene.model.meshes ){
				nVert += mesh.getNumVertices();
				nTri += mesh.getNumIndices() / 3;
			}
			
			Array<Node> nodes = NodeUtil.getAllNodes(new Array<Node>(), scene.model);
			nNodes += nodes.size;
			for(Node node : nodes){
				if(node.parts.size > 0){
					nMeshParts += node.parts.size;
					for(NodePart np : node.parts){
						nRealTri += np.meshPart.size / 3;
						mapSet.add(np.material);
						nBones += np.bones != null ? np.bones.length : 0;
					}
					meshNodes++;
				}
			}
		}
		int nMaterials = mapSet.size;
		log("Scene Graph", "scenes", asset.scenes.size, "nodes", nNodes, "empty", nNodes - meshNodes);
		if(nMesh > 0) log("Mesh", "count", nMesh, "parts", nMeshParts, "Vertices", nVert, "Tris", nTri, "Rendered", nRealTri);
		if(nMaterials > 0) log("Materials", "count", nMaterials);
		if(asset.maxBones > 0) log("Bones", "max", asset.maxBones, "count", nBones);
	}

	private void logTexures(SceneAsset asset) {
		log("Managed textures", "count", asset.textures.size);
	}

	private void logAnimations(SceneAsset asset) {
		int t=0, r=0, s=0, w=0;
		for(Animation a : asset.animations){
			for(NodeAnimation na : a.nodeAnimations){
				t += na.translation == null ? 0 : na.translation.size;
				r += na.rotation == null ? 0 : na.rotation.size;
				s += na.scaling == null ? 0 : na.scaling.size;
				w += na instanceof NodeAnimationHack &&  ((NodeAnimationHack)na).weights == null ? 0 : ((NodeAnimationHack)na).weights.size;
			}
		}
		
		log("Animations", "count", asset.animations.size, "KeyFrames", t+r+s+w, "T", t, "R", r, "S", s, "W", w);
		
	}

	private void log(String title, Object...args) {
		String s = "[" + title + "] ";
		for(int i=0 ; i<args.length-1 ; i+=2){
			s += args[i] + ":" + args[i+1] + " ";
		}
		Gdx.app.log("GLTF Inspector", s);
	}
	
}
