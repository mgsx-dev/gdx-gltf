package net.mgsx.gltf.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Scaling;

import net.mgsx.gltf.GLTFDemo.ShaderMode;
import net.mgsx.gltf.demo.ModelEntry;
import net.mgsx.gltf.scene3d.NodePartPlus;
import net.mgsx.gltf.scene3d.NodePlus;
import net.mgsx.gltf.scene3d.PBRColorAttribute;
import net.mgsx.gltf.scene3d.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.PBRShader;
import net.mgsx.gltf.scene3d.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.SceneAsset;

public class GLTFDemoUI extends Table {
	public SelectBox<ModelEntry> entrySelector;
	public SelectBox<String> variantSelector;
	public SelectBox<String> animationSelector;
	public Table screenshotsTable;
	public Slider ambiantSlider;
	public Slider lightSlider;
	public Slider specularSlider; 
	public SelectBox<String> cameraSelector;
	public Slider lightFactorSlider;
	
	public final Vector3UI lightDirectionControl;
	public SelectBox<ShaderMode> shaderSelector;
	private SelectBox<String> materialSelector;
	private Table materialTable;
	private SceneAsset model;
	
	private final ObjectMap<String, Material> materialMap = new ObjectMap<String, Material>();
	private final ObjectMap<String, Node> nodeMap = new ObjectMap<String, Node>();
	private SelectBox<String> nodeSelector;
	private Table nodeTable;
	private TextButton btNodeExclusive;
	private Node selectedNode;
	
	public GLTFDemoUI(Skin skin) {
		super(skin);
		
		Table root = this;
		root.defaults().pad(5);
		
		entrySelector = new SelectBox<ModelEntry>(skin);
		root.add("Model");
		root.add(entrySelector).row();
		
		root.add("Screenshot");
		root.add(screenshotsTable = new Table(skin)).row();
		
		variantSelector = new SelectBox<String>(skin);
		root.add("File");
		root.add(variantSelector).row();
		
		shaderSelector = new SelectBox<ShaderMode>(skin);
		root.add("Shader");
		root.add(shaderSelector).row();
		shaderSelector.setItems(ShaderMode.values());
		
		ambiantSlider = new Slider(0, 1, .01f, false, skin);
		root.add("Ambiant Light");
		root.add(ambiantSlider).row();
		ambiantSlider.setValue(1f);
		
		lightSlider = new Slider(0, 1, .01f, false, skin);
		root.add("Diffuse Light");
		root.add(lightSlider).row();
		lightSlider.setValue(1f);
		
		specularSlider = new Slider(0, 1, .01f, false, skin);
		root.add("Specular Light");
		root.add(specularSlider).row();
		specularSlider.setValue(1f);
		
		lightFactorSlider = new Slider(0, 10, .01f, false, skin);
		root.add("Light factor");
		root.add(lightFactorSlider).row();
		lightFactorSlider.setValue(1f);
		
		root.add("Dir Light");
		root.add(lightDirectionControl = new Vector3UI(skin, new Vector3())).row();

		root.add("ScaleFGDSpec");
		root.add(new Vector4UI(skin, PBRShader.ScaleFGDSpec)).row();
		
		root.add("ScaleDiffBaseMR");
		root.add(new Vector4UI(skin, PBRShader.ScaleDiffBaseMR)).row();
		
		cameraSelector = new SelectBox<String>(skin);
		root.add("Camera");
		root.add(cameraSelector).row();
		
		animationSelector = new SelectBox<String>(skin);
		root.add("Animation");
		root.add(animationSelector).row();
		
		materialSelector = new SelectBox<String>(skin);
		root.add("Materials");
		root.add(materialSelector).row();
		
		materialTable = new Table(skin);
		root.add();
		root.add(materialTable).row();
		
		nodeSelector = new SelectBox<String>(skin);
		root.add("Nodes");
		root.add(nodeSelector).row();
		root.add();
		root.add(btNodeExclusive = new TextButton("Hide other nodes", getSkin(), "toggle")).row();
		
		nodeTable = new Table(skin);
		root.add();
		root.add(nodeTable).row();
		
		materialSelector.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setMaterial(materialSelector.getSelected());
			}
		});
		
		nodeSelector.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setNode(nodeSelector.getSelected());
			}
		});
		
		btNodeExclusive.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				showHideOtherNodes(btNodeExclusive.isChecked());
			}
		});
	}

	protected void showHideOtherNodes(boolean hide) {
		for(Entry<String, Node> entry : nodeMap){
			for(NodePart part : entry.value.parts){
				part.enabled = selectedNode == null || !hide || entry.value == selectedNode;
			}
		}
	}

	protected void setNode(String nodeID) {
		nodeTable.clearChildren();
		selectedNode = null;
		if(nodeID == null || nodeID.isEmpty()){
			showHideOtherNodes(false);
			return;
		}
		
		final Node node = nodeMap.get(nodeID);
		
		selectedNode = node;
		
		showHideOtherNodes(btNodeExclusive.isChecked());
		
		if(node instanceof NodePlus){
			final NodePlus np = (NodePlus)node;
			if(np.weights != null){
				WeightsUI weightEditor = new WeightsUI(getSkin(), np.weights);
				nodeTable.add("weights");
				nodeTable.add(weightEditor);
				weightEditor.addListener(new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						for(NodePart part : np.parts){
							NodePartPlus npp = (NodePartPlus)part;
							if(npp.morphTargets != null){
								npp.morphTargets.set(np.weights);
							}
						}
					}
				});
				
			}
		}
	}

	protected void setMaterial(String materialID) {
		materialTable.clearChildren();
		if(materialID == null || materialID.isEmpty()) return;
		
		final Material material = materialMap.get(materialID);
		
		// base color
		materialTable.add(new ColorAttributeUI(getSkin(), material.get(ColorAttribute.class, PBRColorAttribute.BaseColorFactor))).row();
		addMaterialTextureSwitch("Color Texture", material, PBRTextureAttribute.BaseColorTexture);

		// emissive
		materialTable.add(new ColorAttributeUI(getSkin(), material.get(ColorAttribute.class, PBRColorAttribute.Emissive))).row();
		addMaterialTextureSwitch("Emissive Texture", material, PBRTextureAttribute.EmissiveTexture);
		
		// metallic roughness
		materialTable.add(new FloatAttributeUI(getSkin(), material.get(PBRFloatAttribute.class, PBRFloatAttribute.Metallic))).row();
		materialTable.add(new FloatAttributeUI(getSkin(), material.get(PBRFloatAttribute.class, PBRFloatAttribute.Roughness))).row();
		addMaterialTextureSwitch("MR Texture", material, PBRTextureAttribute.MetallicRoughnessTexture);
		
		// normal
		materialTable.add(new FloatAttributeUI(getSkin(), material.get(PBRFloatAttribute.class, PBRFloatAttribute.NormalScale))).row();
		addMaterialTextureSwitch("Normal Texture", material, PBRTextureAttribute.NormalTexture);
		
		// occlusion
		materialTable.add(new FloatAttributeUI(getSkin(), material.get(PBRFloatAttribute.class, PBRFloatAttribute.OcclusionStrength))).row();
		addMaterialTextureSwitch("Occlusion Texture", material, PBRTextureAttribute.OcclusionTexture);
	}
	
	private void addMaterialTextureSwitch(String name, final Material material, long type){
		final PBRTextureAttribute attribute = material.get(PBRTextureAttribute.class, type);
		if(attribute != null){
			final TextButton bt = new TextButton(name, getSkin(), "toggle");
			bt.setChecked(true);
			materialTable.add(bt).row();
			
			bt.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if(bt.isChecked()){
						material.set(attribute);
					}else{
						material.remove(attribute.type);
					}
				}
			});
		}
		
	}

	public void setEntry(ModelEntry entry, FileHandle rootFolder) 
	{
		variantSelector.setSelected(null);
		Array<String> variants = entry.variants.keys().toArray(new Array<String>());
		variants.insert(0, "");
		variantSelector.setItems(variants);
		
		if(entry.variants.size == 1){
			variantSelector.setSelectedIndex(1);
		}else{
			variantSelector.setSelectedIndex(0);
		}
		
		
		if(screenshotsTable.getChildren().size > 0){
			Image imgScreenshot = (Image)screenshotsTable.getChildren().first();
			((TextureRegionDrawable)imgScreenshot.getDrawable()).getRegion().getTexture().dispose();
		}
		screenshotsTable.clear();
	}
	
	public void setImage(Texture texture){
		if(texture != null){
			Image img = new Image(texture);
			img.setScaling(Scaling.none);
			screenshotsTable.add(img);
		}
	}

	public void setAnimations(Array<Animation> animations) {
		if(animations.size > 0){
			Array<String> ids = new Array<String>();
			ids.add("");
			for(Animation anim : animations){
				ids.add(anim.id);
			}
			animationSelector.setItems(ids);
		}else{
			animationSelector.setItems();
		}
	}

	public void setCameras(ObjectMap<String, Integer> cameraMap) {
		Array<String> cameraNames = new Array<String>();
		cameraNames.add("");
		for(Entry<String, Integer> e : cameraMap){
			cameraNames.add(e.key);
		}
		cameraSelector.setItems();
		cameraSelector.setItems(cameraNames);
	}

	public void setMaterials(Array<Material> materials) 
	{
		materialMap.clear();
		
		Array<String> names = new Array<String>();
		names.add("");
		for(Material e : materials){
			names.add(e.id);
			materialMap.put(e.id, e);
		}
		materialSelector.setItems();
		materialSelector.setItems(names);
	}

	public void setNodes(Array<Node> nodes) {
		nodeMap.clear();
		
		Array<String> names = new Array<String>();
		names.add("");
		for(Node e : nodes){
			names.add(e.id);
			nodeMap.put(e.id, e);
		}
		nodeSelector.setItems();
		nodeSelector.setItems(names);
	}

	
}
