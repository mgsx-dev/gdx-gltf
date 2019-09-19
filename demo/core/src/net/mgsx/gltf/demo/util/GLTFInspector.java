package net.mgsx.gltf.demo.util;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntIntMap.Entry;

import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.geometry.GLTFMesh;
import net.mgsx.gltf.data.geometry.GLTFPrimitive;
import net.mgsx.gltf.data.scene.GLTFNode;
import net.mgsx.gltf.data.scene.GLTFSkin;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneModel;

public class GLTFInspector {
	
	private int emptyCount;
	private int meshNodeCount;
	private int elementCount;

	public void inspect(SceneAsset asset){
		log("GLTF asset");
		logz("animations", asset.animations.size);
		logz("textures", asset.textures.size);
		logz("maxBones", asset.maxBones);
		log("scenes", asset.scenes.size);
		log();
		logz(asset.scene);
	}
	
	public void inspect(GLTF gltf){
		log("GLTF");
		log(gltf);
	}
	
	private void log(GLTF gltf) {
		if(gltf.animations != null) logz("animations", gltf.animations.size);
		if(gltf.skins != null){
			for(GLTFSkin skin : gltf.skins){
				log("SKIN");
				logz("joints", skin.joints.size);
			}
		}
		
		for(int i=0 ; i<gltf.nodes.size ; i++){
			GLTFNode node = gltf.nodes.get(i);
			loga("node", i);
			if(node.extras != null){
				loga("custom properties", node.extras.entries());
			}
		}
		log();
		
		for(GLTFMesh mesh : gltf.meshes){
			log("- mesh", mesh.name);
			
			if(mesh.weights != null){
				log("weight", mesh.weights.length);
				for(float weight : mesh.weights){
					System.out.print(weight + " ");
				}
				
				System.out.println();
			}
			for(GLTFPrimitive primitive : mesh.primitives){
				
				// TODO if(primitive.mode)
			}
		}
	}


	private void logz(SceneModel scene) {
		if(scene == null) return;
		log("scene", scene.name);
		logz("cameras", scene.cameras.size);
		logz("lights", scene.lights.size);
		log(scene.model);
	}

	private void log(Model model) {
		logz("animations", model.animations.size);
		logz("materials", model.materials.size);
		logz("meshes", model.meshes.size);
		if(model.meshes.size > 0){
			logmeshes(model.meshes);
		}
		logz("meshParts", model.meshParts.size);
		logz("nodes", model.nodes.size);
		
		collect(model.nodes);
	}


	private void logmeshes(Array<Mesh> meshes) {
		for(Mesh mesh : meshes){
			log(mesh);
		}
	}


	private void log(Mesh mesh) {
		loga("MESH", mesh.getNumIndices() / 3, "poly", human(mesh.getNumIndices() * 2 + mesh.getNumVertices() * mesh.getVertexSize()));
		//log("vertices", mesh.getNumVertices());
		//log("vertex size", mesh.getVertexSize());
		
		VertexAttribute attr = mesh.getVertexAttribute(VertexAttributes.Usage.BoneWeight);
		if(attr != null){
			float[] vertices = new float[mesh.getNumVertices() * mesh.getVertexSize()/4];
			mesh.getVertices(vertices);
			IntIntMap map = new IntIntMap();
			for(int i=0 ; i<mesh.getNumVertices() ; i++){
				int index = (i * mesh.getVertexSize() + attr.offset) /4;
				if(attr.numComponents != 2) throw new GdxRuntimeException("bad components");
				int boneID = (int)vertices[index+0];
				if(vertices[index+1] == 0f){
					System.out.println("EMPTY WEIGHT FOUND");
				}
				map.put(boneID, map.get(boneID, 0) + 1);
			}
			loga("bones", map.size); 
			for(Entry entry : map){
				// loga("bone", entry.key, "used", entry.value);
			}
		}
	}


	private String human(int n) {
		if(n < 2024){
			return n + " B";
		}
		n /= 1024;
		if(n < 2024){
			return n + " kB";
		}
		n /= 1024;
		if(n < 2024){
			return n + " MB";
		}
		n /= 1024;
		if(n < 2024){
			return n + " GB";
		}
		n /= 1024;
		return n + " TB";
	}


	private void collect(Iterable<Node> nodes) {
		for(Node node : nodes){
			
			if(node.parts.size > 0){
				meshNodeCount++;
				log("NODE", node.id);
				log(node.parts);
			}else{
				emptyCount++;
			}
			
			collect(node.getChildren());
		}
	}


	private void log(Array<NodePart> parts) {
		log("parts", parts.size);
		for(NodePart part : parts){
			log(part);
		}
	}


	private void log(NodePart part) {
		boolean indexed = part.meshPart.mesh.getNumIndices() > 0;
		loga(indexed ? "indices" : "vertices", part.meshPart.size, indexed ? "" : " not indexed", 
				part.meshPart.size/3, "poly");
		elementCount += part.meshPart.size;
		if(part.bones != null){
			log("bones influence", part.bones.length);
		}
	}


	private void log(String title, Object v) {
		System.out.println(title + " - " + v);
	}
	private void logz(String title, int v) {
		if(v != 0) log(title, v);
	}
	
	private void log(String title) {
		System.out.println("# " + title + " #");
	}
	
	private void log() {
		System.out.println();
	}
	
	private void loga(Object ... objs){
		String s = "";
		for(Object o : objs){
			if(!s.isEmpty()) s += ", ";
			s += o.toString();
		}
		System.out.println(s);
	}
}
