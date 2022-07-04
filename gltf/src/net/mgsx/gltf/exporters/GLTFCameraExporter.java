package net.mgsx.gltf.exporters;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gltf.data.camera.GLTFCamera;
import net.mgsx.gltf.data.camera.GLTFOrthographic;
import net.mgsx.gltf.data.camera.GLTFPerspective;
import net.mgsx.gltf.data.scene.GLTFNode;
import net.mgsx.gltf.loaders.exceptions.GLTFRuntimeException;
import net.mgsx.gltf.loaders.exceptions.GLTFUnsupportedException;

class GLTFCameraExporter {

	private GLTFExporter base;

	public GLTFCameraExporter(GLTFExporter base) {
		this.base = base;
	}

	public void export(ObjectMap<Node, Camera> cameras) {
		for(Entry<Node, Camera> entry : cameras){
			int nodeID = base.nodeMapping.indexOf(entry.key, true);
			if(nodeID < 0) throw new GLTFRuntimeException("node not found");
			GLTFNode glNode = base.root.nodes.get(nodeID);
			if(base.root.cameras == null){
				base.root.cameras = new Array<GLTFCamera>();
			}
			glNode.camera = base.root.cameras.size;
			base.root.cameras.add(export(entry.value));
		}
	}
	
	private GLTFCamera export(Camera camera) {
		GLTFCamera glCamera = new GLTFCamera();
		if(camera instanceof PerspectiveCamera){
			PerspectiveCamera pcam = (PerspectiveCamera)camera;
			glCamera.type = "perspective";
			glCamera.perspective = new GLTFPerspective();
			glCamera.perspective.yfov = pcam.fieldOfView * MathUtils.degreesToRadians; // TODO not sure
			glCamera.perspective.znear = camera.near;
			glCamera.perspective.zfar = camera.far;
			glCamera.perspective.aspectRatio = camera.viewportWidth / camera.viewportHeight; // TODO not sure
			// TODO aspect ratio and fov should be recomputed...
		}
		else if(camera instanceof OrthographicCamera){
			OrthographicCamera ocam = (OrthographicCamera)camera;
			glCamera.type = "orthographic";
			glCamera.orthographic = new GLTFOrthographic();
			glCamera.orthographic.znear = camera.near;
			glCamera.orthographic.zfar = camera.far;
			glCamera.orthographic.xmag = camera.viewportWidth * ocam.zoom; // TODO not sure
			glCamera.orthographic.ymag = camera.viewportHeight * ocam.zoom; // TODO not sure
		}
		else{
			throw new GLTFUnsupportedException("unsupported camera type " + camera.getClass());
		}
		
		return glCamera;
	}

}
