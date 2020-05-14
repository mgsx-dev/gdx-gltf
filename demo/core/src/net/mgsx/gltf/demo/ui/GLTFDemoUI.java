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
import net.mgsx.gltf.demo.events.FileOpenEvent;
import net.mgsx.gltf.demo.events.FileSaveEvent;
import net.mgsx.gltf.demo.events.IBLFolderChangeEvent;
import net.mgsx.gltf.demo.model.IBLStudio;
import net.mgsx.gltf.demo.model.IBLStudio.IBLPreset;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.model.NodePartPlus;
import net.mgsx.gltf.scene3d.model.NodePlus;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneModel;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;

public class GLTFDemoUI extends Table {
	public static FileSelector fileSelector = null;
	
	public static boolean LIVE_STATS = true;

	public SelectBox<ModelEntry> entrySelector;
	public SelectBox<String> variantSelector;
	public SelectBox<String> animationSelector;
	public Table screenshotsTable;
	public Slider debugAmbiantSlider;
	public Slider ambiantSlider;
	public Slider lightSlider;
	public Slider debugSpecularSlider; 
	public SelectBox<String> cameraSelector;
	
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
	protected CollapsableUI shaderOptions;
	public SelectBox<SRGB> shaderSRGB;
	private CollapsableUI lightOptions;
	public SelectBox<SceneModel> sceneSelector;
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

	public BooleanUI IBLEnabled;

	private Table IBLChooser;

	private CollapsableUI IBLOptions;

	public BooleanUI IBLSpecular;

	public BooleanUI IBLLookup;

	public Vector4UI skyBoxColor;

	public BooleanUI outlinesEnabled;

	public FloatUI outlinesWidth;

	public FloatUI outlineDepthMin;

	public FloatUI outlineDepthMax;

	public Vector4UI outlineInnerColor;

	public Vector4UI outlineOuterColor;

	private CollapsableUI outlineOptions;

	public FloatUI outlineDistFalloff;

	public BooleanUI outlineDistFalloffOption;

	private Label lightLabel;

	private SceneManager sceneManager;

	public final SelectBox<IBLPreset> IBLSelector;

	public GLTFDemoUI(SceneManager sceneManager, Skin skin) {
		super(skin);
		this.sceneManager = sceneManager;
		
		Table root = new Table(skin);
		root.defaults().pad(5);
		Table rootRight = new Table(skin);
		rootRight.defaults().pad(5);
		
		add(root).expandY().top();
		add().expand();
		add(rootRight).expandY().top();
		
		if(fileSelector != null){
			TextButton btOpenFile = new TextButton("Open file", skin);
			TextButton btExportFile = new TextButton("Save to file", skin);
			root.add("File");
			Table btTable = new Table();
			btTable.add(btOpenFile);
			btTable.add(btExportFile);
			root.add(btTable).row();
			
			btOpenFile.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					fileSelector.open(new Runnable() {
						@Override
						public void run() {
							GLTFDemoUI.this.fire(new FileOpenEvent(fileSelector.lastFile));
						}
					});
				}
			});
			
			btExportFile.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					fileSelector.open(new Runnable() {
						@Override
						public void run() {
							GLTFDemoUI.this.fire(new FileSaveEvent(fileSelector.lastFile));
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
		sceneSelector = new SelectBox<SceneModel>(skin){
			@Override
			protected String toString(SceneModel item) {
				return item.name == null ? "<null>" : item.name;
			}
		};
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
		
		shaderOptions.optTable.add("SkyBox Color");
		shaderOptions.optTable.add(skyBoxColor = new Vector4UI(skin, new Color(Color.WHITE))).row();
		
		// Outlines
		root.add();
		root.add(outlineOptions = new CollapsableUI(skin, "Outline Options", false)).row();

		outlineOptions.optTable.add("Outlines");
		outlineOptions.optTable.add(outlinesEnabled = new BooleanUI(skin, false)).row();
		
		outlineOptions.optTable.add("Thickness");
		outlineOptions.optTable.add(outlinesWidth = new FloatUI(skin, 0f)).row();
		
		outlineOptions.optTable.add("Depth min");
		outlineOptions.optTable.add(outlineDepthMin = new FloatUI(skin, .35f)).row();
		outlineOptions.optTable.add("Depth max");
		outlineOptions.optTable.add(outlineDepthMax = new FloatUI(skin, .9f)).row();
		
		outlineOptions.optTable.add("Inner color");
		outlineOptions.optTable.add(outlineInnerColor = new Vector4UI(skin, new Color(0,0,0,.3f))).row();
		
		outlineOptions.optTable.add("Outer color");
		outlineOptions.optTable.add(outlineOuterColor = new Vector4UI(skin, new Color(0,0,0,.7f))).row();
		
		outlineOptions.optTable.add("Distance Falloff");
		outlineOptions.optTable.add(outlineDistFalloffOption = new BooleanUI(skin, false)).row();

		outlineOptions.optTable.add("Distance Exponent");
		outlineOptions.optTable.add(outlineDistFalloff = new FloatUI(skin, 1f)).row();
		
		// Lighting options
		
		root.add();
		root.add(lightOptions = new CollapsableUI(skin, "Light Options", false)).row();
		
		
		lightSlider = new Slider(0, 100, .01f, false, skin);
		lightOptions.optTable.add("Light intensity");
		lightOptions.optTable.add(lightSlider).row();
		lightSlider.setValue(1f);
		
		lightOptions.optTable.add("Dir Light");
		lightOptions.optTable.add(lightDirectionControl = new Vector3UI(skin, new Vector3())).row();

		lightOptions.optTable.add("Shadows");
		lightOptions.optTable.add(lightShadow = new BooleanUI(skin, false)).row();
		
		lightOptions.optTable.add("Shadow Bias");
		lightOptions.optTable.add(shadowBias = new FloatUI(skin, 0)).row();
		
		ambiantSlider = new Slider(0, 1, .01f, false, skin);
		ambiantSlider.setValue(1f);
		lightOptions.optTable.add("Ambient Light");
		lightOptions.optTable.add(ambiantSlider).row();

		// IBL options
		
		root.add();
		root.add(IBLOptions = new CollapsableUI(skin, "IBL Options", false)).row();
		
		IBLOptions.optTable.add("Overall");
		IBLOptions.optTable.add(IBLEnabled = new BooleanUI(skin, true)).row();
		
		IBLOptions.optTable.add("Radiance");
		IBLOptions.optTable.add(IBLSpecular = new BooleanUI(skin, true)).row();

		IBLOptions.optTable.add("BSDF");
		IBLOptions.optTable.add(IBLLookup = new BooleanUI(skin, true)).row();

		IBLOptions.optTable.add("Presets");
		IBLOptions.optTable.add(IBLChooser = new Table(skin)).row();
		
		IBLChooser.add(IBLSelector = new SelectBox<IBLStudio.IBLPreset>(getSkin()));
		if(fileSelector != null){
			TextButton btOpenFile = new TextButton("Load", skin);
			IBLChooser.add(btOpenFile);
			
			btOpenFile.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					fileSelector.selectFolder(new Runnable() {
						@Override
						public void run() {
							GLTFDemoUI.this.fire(new IBLFolderChangeEvent(fileSelector.lastFile));
						}
					});
				}
			});
		}
		
		// Scene options
		
		cameraSelector = new SelectBox<String>(skin);
		root.add("Camera");
		root.add(cameraSelector).row();
		
		lightSelector = new SelectBox<String>(skin);
		root.add(lightLabel = new Label("Lights", skin));
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
				WeightsUI weightEditor = new WeightsUI(getSkin(), np.weights, np.morphTargetNames);
				nodeTable.add("Morph Targets").row();
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
			materialTable.add(bt);
			
			Image pict = new Image(attribute.textureDescription.texture);
			
			pict.setScaling(Scaling.fit);
			
			materialTable.add(pict).size(64);
			
			materialTable.row();
			
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

	public void setCameras(ObjectMap<Node, Camera> cameras) {
		Array<String> cameraNames = new Array<String>();
		cameraNames.add("");
		for(Entry<Node, Camera> e : cameras){
			cameraNames.add(e.key.id);
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
		for(Node e : nodes){
			names.add(e.id);
			nodeMap.put(e.id, e);
		}
		names.sort();
		names.insert(0, "");
		nodeSelector.setItems();
		nodeSelector.setItems(names);
	}

	public void setScenes(Array<SceneModel> scenes) {
		if(scenes == null){
			sceneSelector.setDisabled(true);
			sceneSelector.setItems(new Array<SceneModel>());
		}else{
			sceneSelector.setDisabled(false);
			Array<SceneModel> items = new Array<SceneModel>();
			for(int i=0 ; i<scenes.size ; i++){
				items.add(scenes.get(i));
			}
			sceneSelector.setItems(items);
		}
	}

	public void setLights(ObjectMap<Node, BaseLight> lights) {
		Array<String> names = new Array<String>();
		names.add("");
		for(Entry<Node, BaseLight> entry : lights){
			names.add(entry.key.id);
		}
		lightSelector.setItems(names);
	}
	
	@Override
	public void act(float delta) {
		if(LIVE_STATS ){
			lightLabel.setText("Lights (" + sceneManager.getActiveLightsCount() + "/" + sceneManager.getTotalLightsCount() + ")");
		}
		super.act(delta);
	}

}
