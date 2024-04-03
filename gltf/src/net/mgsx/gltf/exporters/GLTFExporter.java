package net.mgsx.gltf.exporters;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import net.mgsx.gltf.data.GLTF;
import net.mgsx.gltf.data.GLTFAsset;
import net.mgsx.gltf.data.data.GLTFAccessor;
import net.mgsx.gltf.data.geometry.GLTFMesh;
import net.mgsx.gltf.data.geometry.GLTFPrimitive;
import net.mgsx.gltf.data.scene.GLTFNode;
import net.mgsx.gltf.data.scene.GLTFScene;
import net.mgsx.gltf.loaders.exceptions.GLTFIllegalException;
import net.mgsx.gltf.loaders.exceptions.GLTFRuntimeException;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneModel;

public class GLTFExporter {

	private static final String GENERATOR_INFO = "GDX glTF exporter 1.0";
	
	GLTF root;
	GLTFBinaryExporter binManager;
	private final GLTFMeshExporter meshExporter;
	
	final Array<Node> nodeMapping = new Array<Node>();
	final Array<Material> materialMapping = new Array<Material>();
	final Array<Texture> textureMapping = new Array<Texture>();
	
	/** current texture file index */
	protected int textureFileIndex;
	/** current file handle name without extension */
	protected String fileHandleName = "";
	
	private final GLTFExporterConfig config;

	
	/**
	 * create with default config.
	 */
	public GLTFExporter() {
		this(new GLTFExporterConfig());
	}
	
	public GLTFExporter(GLTFExporterConfig config) {
		this.config = config;
		meshExporter = new GLTFMeshExporter(this);
	}
	
	/**
	 * sub class may override this method in order to implement some custom name mapping.
	 * @param texture
	 * @return a unique name for the texture
	 */
	protected String getImageName(Texture texture) {
		String name = fileHandleName + "texture" + textureFileIndex;
		textureFileIndex++;
		return name;
	}
	
	private void reset() {
		root = null;
		binManager.reset();
		nodeMapping.clear();
		materialMapping.clear();
		textureMapping.clear();
		textureFileIndex = 0;
	}
	
	/** convenient method to export a single mesh 
	 * primitiveType can be any of OpenGL primitive: 
	 * {@link com.badlogic.gdx.graphics.GL20#GL_POINTS}, 
	 * {@link com.badlogic.gdx.graphics.GL20#GL_LINES}, 
	 * {@link com.badlogic.gdx.graphics.GL20#GL_LINE_STRIP}, 
	 * {@link com.badlogic.gdx.graphics.GL20#GL_TRIANGLES}, 
	 * {@link com.badlogic.gdx.graphics.GL20#GL_TRIANGLE_STRIP},
	 * {@link com.badlogic.gdx.graphics.GL20#GL_TRIANGLE_FAN},
	 * etc..
	 * */
	public void export(Mesh mesh, int primitiveType, FileHandle file){
		GLTFScene scene = beginSingleScene(file);

		GLTFNode glNode = obtainNode();
		scene.nodes = new Array<Integer>();
		scene.nodes.add(root.nodes.size-1);
		
		GLTFMesh gltfMesh = obtainMesh();
		glNode.mesh = root.meshes.size-1;
		
		MeshPart meshPart = new MeshPart();
		meshPart.mesh = mesh;
		meshPart.offset = 0;
		meshPart.primitiveType = primitiveType;
		meshPart.size = mesh.getNumIndices();
		if(meshPart.size == 0) meshPart.size = mesh.getNumVertices();
		
		gltfMesh.primitives = new Array<GLTFPrimitive>();
		GLTFPrimitive primitive = meshExporter.exportMeshPart(meshPart);
		gltfMesh.primitives.add(primitive);
		
		end(file);
	}
	/** convenient method to export a single model */
	public void export(Model model, FileHandle file)
	{
		GLTFScene scene = beginSingleScene(file);

		new GLTFMaterialExporter(this).export(model.nodes);

		scene.nodes = exportNodes(scene, model.nodes);
		
		new GLTFSkinExporter(this).export();
		new GLTFAnimationExporter(this).export(model.animations);
		
		end(file);
	}
	/** convenient method to export a single scene */
	public void export(Scene scene, FileHandle file) {
		GLTFScene glScene = beginSingleScene(file);
		
		exportScene(glScene, scene);
		
		end(file);
	}
	/** convenient method to export a single scene */
	public void export(SceneModel scene, FileHandle file) {
		GLTFScene glScene = beginSingleScene(file);

		exportScene(glScene, scene);
		
		end(file);
	}
	
	private void exportScene(GLTFScene glScene, Scene scene){
		new GLTFMaterialExporter(this).export(scene.modelInstance.nodes);
		
		glScene.nodes = exportNodes(glScene, scene.modelInstance.nodes);
		
		if(config.exportCameras){
			new GLTFCameraExporter(this).export(scene.cameras);
		}
		if(config.exportLights){
			new GLTFLightExporter(this).export(scene.lights);
		}
		
		new GLTFSkinExporter(this).export();
		new GLTFAnimationExporter(this).export(scene.modelInstance.animations);
	}
	
	private void exportScene(GLTFScene glScene, SceneModel scene){
		new GLTFMaterialExporter(this).export(scene.model.nodes);
		
		glScene.nodes = exportNodes(glScene, scene.model.nodes);
		
		if(config.exportCameras){
			new GLTFCameraExporter(this).export(scene.cameras);
		}
		if(config.exportLights){
			new GLTFLightExporter(this).export(scene.lights);
		}
		
		new GLTFSkinExporter(this).export();
		new GLTFAnimationExporter(this).export(scene.model.animations);
	}
	
	/** multi scene export */
	public void export(SceneAsset asset, FileHandle file) {
		export(asset.scenes, asset.scene, file);
	}
	/** multi scene export */
	public void export(Array<SceneModel> scenes, SceneModel defaultScene, FileHandle file) {
		beginMultiScene(file);
		
		for(SceneModel scene : scenes){
			GLTFScene glScene = obtainScene();
			exportScene(glScene, scene);
		}
		
		root.scene = scenes.indexOf(defaultScene, true);
		if(root.scene < 0) throw new GLTFIllegalException("scene not found");
		
		end(file);
	}
	/** multi scene export */
	public void export(Array<Scene> scenes, Scene defaultScene, FileHandle file) {
		beginMultiScene(file);
		
		for(Scene scene : scenes){
			GLTFScene glScene = obtainScene();
			exportScene(glScene, scene);
		}
		
		root.scene = scenes.indexOf(defaultScene, true);
		if(root.scene < 0) throw new GLTFIllegalException("scene not found");
		
		end(file);
	}
	
	private void beginMultiScene(FileHandle file){
		// get fileHandleName without the extension
		fileHandleName = file.nameWithoutExtension() + "_";

		binManager = new GLTFBinaryExporter(file.parent(), config);
		
		root = new GLTF();
		root.asset = new GLTFAsset();
		root.asset.version = "2.0";
		root.asset.generator = GENERATOR_INFO;
		
		root.scenes = new Array<GLTFScene>();
		
	}
	private GLTFScene beginSingleScene(FileHandle file){
		beginMultiScene(file);
		root.scene = 0;
		return obtainScene();
	}
	private GLTFScene obtainScene() {
		GLTFScene scene = new GLTFScene();
		root.scenes.add(scene);
		return scene;
	}
	
	private void end(FileHandle file){
		root.bufferViews = binManager.views;
		root.buffers = binManager.flushAllToFiles(file.nameWithoutExtension());
		
		Json json = new Json();
		json.setOutputType(OutputType.json);
		json.setUsePrototypes(true);
		file.writeString(json.prettyPrint(root), false);
		
		reset();
	}
	
	private Array<Integer> exportNodes(GLTFScene scene, Iterable<Node> nodes) {
		Array<Integer> indices = null;
		for(Node node : nodes)
		{
			// create node
			GLTFNode data = obtainNode();
			nodeMapping.add(node);
			data.name = node.id;
			
			// transform, either a matrix or individual component (we use individual components but it might be an option)
			if(!node.translation.isZero()){
				data.translation = GLTFExportTypes.toArray(node.translation);
			}
			if(!node.scale.epsilonEquals(1, 1, 1)){
				data.scale = GLTFExportTypes.toArray(node.scale);
			}
			if(!node.rotation.isIdentity()){
				data.rotation = GLTFExportTypes.toArray(node.rotation);
			}
			
			// indexing node
			if(indices == null) indices = new Array<Integer>();
			indices.add(root.nodes.size-1);
			
			// create mesh
			if(node.parts.size > 0){
				GLTFMesh gltfMesh = obtainMesh();
				data.mesh = root.meshes.size-1;
				
				gltfMesh.primitives = new Array<GLTFPrimitive>();
				for(NodePart nodePart : node.parts){
					GLTFPrimitive primitive = meshExporter.exportMeshPart(nodePart.meshPart);
					int materialIndex = materialMapping.indexOf(nodePart.material, true);
					if(materialIndex < 0) throw new GLTFRuntimeException("material not found");
					primitive.material = materialIndex;
					gltfMesh.primitives.add(primitive);
				}
			}
			
			// recursive children export
			data.children = exportNodes(scene, node.getChildren());
		}
		return indices;
	}
	
	GLTFAccessor obtainAccessor() {
		GLTFAccessor a = new GLTFAccessor();
		if(root.accessors == null) root.accessors = new Array<GLTFAccessor>();
		root.accessors.add(a);
		return a;
	}
	
	private GLTFNode obtainNode(){
		GLTFNode data = new GLTFNode();
		if(root.nodes == null) root.nodes = new Array<GLTFNode>();
		root.nodes.add(data);
		return data;
	}
	
	private GLTFMesh obtainMesh(){
		GLTFMesh data = new GLTFMesh();
		if(root.meshes == null) root.meshes = new Array<GLTFMesh>();
		root.meshes.add(data);
		return data;
	}
	void useExtension(String ext, boolean required) {
		if(root.extensionsUsed == null){
			root.extensionsUsed = new Array<String>();
		}
		if(!root.extensionsUsed.contains(ext, false)){
			root.extensionsUsed.add(ext);
		}
		if(required){
			if(root.extensionsRequired == null){
				root.extensionsRequired = new Array<String>();
			}
			if(!root.extensionsRequired.contains(ext, false)){
				root.extensionsRequired.add(ext);
			}
		}
	}
}