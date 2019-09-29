package net.mgsx.gltf.demo.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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

import net.mgsx.gltf.demo.GLTFDemo.ShaderMode;
import net.mgsx.gltf.demo.data.ModelEntry;
import net.mgsx.gltf.demo.events.FileChangeEvent;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.model.NodePartPlus;
import net.mgsx.gltf.scene3d.model.NodePlus;
import net.mgsx.gltf.scene3d.scene.SceneModel;
import net.mgsx.gltf.scene3d.shaders.PBRShader;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class GLTFDemoUI extends Table {
	public static FileSelector fileSelector = null;
	
	public SelectBox<ModelEntry> entrySelector;
	public SelectBox<String> variantSelector;
	public SelectBox<String> animationSelector;
	public Table screenshotsTable;
	public Slider debugAmbiantSlider;
	public Slider ambiantSlider;
	public Slider lightSlider;
	public Slider debugSpecularSlider; 
	public SelectBox<String> cameraSelector;
	public Slider lightFactorSlider;
	
	public final Vector3UI lightDirectionControl;
	public SelectBox<ShaderMode> shaderSelector;
	private SelectBox<String> materialSelector;
	private Table materialTable;
	
	private final ObjectMap<String, Material> materialMap = new ObjectMap<String, Material>();
	private final ObjectMap<String, Node> nodeMap = new ObjectMap<String, Node>();
	private SelectBox<String> nodeSelector;
	private Table nodeTable;
	private TextButton btNodeExclusive;
	private Node selectedNode;
	public CollapsableUI shaderDebug;
	protected CollapsableUI shaderOptions;
	public SelectBox<SRGB> shaderSRGB;
	private CollapsableUI lightOptions;
	public SelectBox<String> sceneSelector;
	public SelectBox<String> lightSelector;
	public Label shaderCount;
	public TextButton skeletonButton;
	public BooleanUI lightShadow;
	public FloatUI shadowBias;
	public TextButton btAllAnimations;
	public BooleanUI fogEnabled;
	public BooleanUI skyBoxEnabled;
	public Vector4UI fogColor;
	public Vector3UI fogEquation;
	
	public GLTFDemoUI(Skin skin) {
		super(skin);
		
		Table root = new Table(skin);
		root.defaults().pad(5);
		Table rootRight = new Table(skin);
		rootRight.defaults().pad(5);
		
		add(root).expandY().top();
		add().expand();
		add(rootRight).expandY().top();
		
		if(fileSelector != null){
			TextButton btOpenFile = new TextButton("Open file", skin);
			root.add("File");
			root.add(btOpenFile).row();
			
			btOpenFile.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					fileSelector.open(new Runnable() {
						@Override
						public void run() {
							GLTFDemoUI.this.fire(new FileChangeEvent(fileSelector.lastFile));
						}
					});
				}
			});
		}
		
		entrySelector = new SelectBox<ModelEntry>(skin);
		root.add("Model");
		root.add(entrySelector).row();
		
		root.add("Screenshot");
		root.add(screenshotsTable = new Table(skin)).row();
		
		variantSelector = new SelectBox<String>(skin);
		root.add("File");
		root.add(variantSelector).row();
		
		// scene
		sceneSelector = new SelectBox<String>(skin);
		root.add("Scene");
		root.add(sceneSelector).row();
		
		
		
		// Shader options
		
		shaderSelector = new SelectBox<ShaderMode>(skin);
		root.add("Shader");
		root.add(shaderSelector).row();
		shaderSelector.setItems(ShaderMode.values());
		
		root.add("Shader count");
		root.add(shaderCount = new Label("", skin)).row();
		
		root.add();
		root.add(shaderOptions = new CollapsableUI(skin, "Shader Options", false)).row();
		shaderOptions.optTable.add("SRGB");
		shaderOptions.optTable.add(shaderSRGB = new SelectBox<SRGB>(skin)).row();
		shaderSRGB.setItems(SRGB.values());
		shaderSRGB.setSelected(SRGB.ACCURATE);

		// Fog
		shaderOptions.optTable.add("Fog");
		shaderOptions.optTable.add(fogEnabled = new BooleanUI(skin, false)).row();
		
		shaderOptions.optTable.add("Fog Color");
		shaderOptions.optTable.add(fogColor = new Vector4UI(skin, new Color())).row();

		shaderOptions.optTable.add("Fog Equation");
		shaderOptions.optTable.add(fogEquation = new Vector3UI(skin, new Vector3(-1f, 1f, -0.8f))).row();

		// Skybox
		shaderOptions.optTable.add("SkyBox");
		shaderOptions.optTable.add(skyBoxEnabled = new BooleanUI(skin, true)).row();
		
		root.add();
		root.add(shaderDebug = new CollapsableUI(skin, "Debug Mode", false)).row();
		
		debugAmbiantSlider = new Slider(0, 1, .01f, false, skin);
		shaderDebug.optTable.add("Ambient Light");
		shaderDebug.optTable.add(debugAmbiantSlider).row();
		debugAmbiantSlider.setValue(1f);

		debugSpecularSlider = new Slider(0, 1, .01f, false, skin);
		shaderDebug.optTable.add("Specular Light");
		shaderDebug.optTable.add(debugSpecularSlider).row();
		debugSpecularSlider.setValue(1f);
		
		shaderDebug.optTable.add("ScaleFGDSpec");
		shaderDebug.optTable.add(new Vector4UI(skin, PBRShader.ScaleFGDSpec)).row();
		
		shaderDebug.optTable.add("ScaleDiffBaseMR");
		shaderDebug.optTable.add(new Vector4UI(skin, PBRShader.ScaleDiffBaseMR)).row();
		
		// Lighting options
		
		root.add();
		root.add(lightOptions = new CollapsableUI(skin, "Light Options", false)).row();
		
		
		ambiantSlider = new Slider(0, 1, .01f, false, skin);
		lightOptions.optTable.add("Ambient Light");
		lightOptions.optTable.add(ambiantSlider).row();
		ambiantSlider.setValue(.5f);

		lightSlider = new Slider(0, 1, .01f, false, skin);
		lightOptions.optTable.add("Diffuse Light");
		lightOptions.optTable.add(lightSlider).row();
		lightSlider.setValue(1f);
		
		lightFactorSlider = new Slider(0, 10, .01f, false, skin);
		lightOptions.optTable.add("Light factor");
		lightOptions.optTable.add(lightFactorSlider).row();
		lightFactorSlider.setValue(1f);
		
		lightOptions.optTable.add("Dir Light");
		lightOptions.optTable.add(lightDirectionControl = new Vector3UI(skin, new Vector3())).row();

		lightOptions.optTable.add("Shadows");
		lightOptions.optTable.add(lightShadow = new BooleanUI(skin, false)).row();
		
		lightOptions.optTable.add("Shadow Bias");
		lightOptions.optTable.add(shadowBias = new FloatUI(skin, 0)).row();
		
		
		
		cameraSelector = new SelectBox<String>(skin);
		root.add("Camera");
		root.add(cameraSelector).row();
		
		lightSelector = new SelectBox<String>(skin);
		root.add("Lights");
		root.add(lightSelector).row();
		
		animationSelector = new SelectBox<String>(skin);
		btAllAnimations = new TextButton("All", skin, "toggle");
		root.add("Animation");
		root.add(animationSelector);
		root.add(btAllAnimations).row();
		
		skeletonButton = new TextButton("Skeletons", getSkin(), "toggle");
		root.add("Skeletons");
		root.add(skeletonButton).row();
		
		materialSelector = new SelectBox<String>(skin);
		rootRight.add("Materials");
		rootRight.add(materialSelector).row();
		
		materialTable = new Table(skin);
		rootRight.add();
		rootRight.add(materialTable).row();
		
		nodeSelector = new SelectBox<String>(skin);
		rootRight.add("Nodes");
		rootRight.add(nodeSelector).row();
		rootRight.add();
		rootRight.add(btNodeExclusive = new TextButton("Hide other nodes", getSkin(), "toggle")).row();
		
		nodeTable = new Table(skin);
		rootRight.add();
		rootRight.add(nodeTable).row();
		
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
		
		btAllAnimations.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
			}
		});
	}

	protected TextButton toggle(String name, boolean checked) {
		TextButton bt = new TextButton(name, getSkin(), "toggle");
		bt.setChecked(checked);
		return bt;
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
			img.setScaling(Scaling.fit);
			screenshotsTable.add(img).height(100);
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

	public void setCameras(ObjectMap<Camera, Node> cameras) {
		Array<String> cameraNames = new Array<String>();
		cameraNames.add("");
		for(Entry<Camera, Node> e : cameras){
			cameraNames.add(e.value.id);
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

	public void setScenes(Array<SceneModel> scenes) {
		if(scenes == null){
			sceneSelector.setDisabled(true);
			sceneSelector.setItems(new Array<String>());
		}else{
			sceneSelector.setDisabled(false);
			Array<String> names = new Array<String>();
			names.add("");
			for(int i=0 ; i<scenes.size ; i++){
				names.add(scenes.get(i).name);
			}
			sceneSelector.setItems(names);
			
		}
	}

	public void setLights(ObjectMap<BaseLight, Node> lights) {
		Array<String> names = new Array<String>();
		names.add("");
		for(Entry<BaseLight, Node> entry : lights){
			names.add(entry.value.id);
		}
		lightSelector.setItems(names);
	}

}
