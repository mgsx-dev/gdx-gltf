package net.mgsx.gltf.ibl.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import net.mgsx.gltf.ibl.events.ExportEnvMapEvent;
import net.mgsx.gltf.ibl.io.FileSelector;
import net.mgsx.gltf.ibl.io.RGBE.Header;
import net.mgsx.gltf.ibl.model.IBLSettings;
import net.mgsx.gltf.ibl.util.GLUtils;

public class IBLComposerUI extends Table
{
	private Image imgRaw;
	private Slider exposureSlider;
	private Label hdrInfo;
	private SelectBox<MapSize> envSize;
	private IBLSettings settings;
	private Slider fovSlider;
	private Slider albedoSlider;
	private Slider metallicSlider;
	private Slider roughnessSlider;
	private Slider ambientSlider;
	private Slider LightSlider;
	private SelectBox<MapSize> radSize;
	private SelectBox<MapSize> irrSize;
	private SelectBox<MapSize> brdfSize;
	private TextButton brdfBuiltin;
	private TextButton brdf16;
	private Slider LightAzimuth;
	private Slider LightElevation;
	private Slider LightHue;
	private Slider LightSat;

	public IBLComposerUI(Skin skin, IBLSettings settings) {
		super(skin);
		this.settings = settings;
		
		float lum = .2f;
		Table menuRight = new Table(skin);
		menuRight.setBackground(skin.newDrawable("white", lum, lum, lum, 1f));
		menuRight.defaults().fillX().pad(2);
		menuRight.pad(10);
		
		Table menuLeft = new Table(skin);
		menuLeft.setBackground(skin.newDrawable("white", lum, lum, lum, 1f));
		menuLeft.defaults().fillX().pad(2);
		menuLeft.pad(10);
		
		add(menuLeft).expandY().top();
		add().expand();
		add(menuRight).expandY().top();
		
		Table menu = menuRight;
		menu.add(title("HDRi")).colspan(2).row();
		
		menu.add(UI.change(new TextButton("Open HDR File", getSkin()), event->openHDR())).colspan(2).row();
		menu.add(hdrInfo = new Label("", getSkin())).colspan(2).row();
		menu.add(imgRaw = new Image()).maxWidth(200).maxHeight(100).colspan(2).row();
		
		menu.add("Exposure");
		menu.add(exposureSlider = UI.change(new Slider(-1, 1, .01f, false, getSkin()), event->settings.setExposure(sliderToExposure(exposureSlider.getValue())))).row();
		
		menu.add(title("Environment Map")).colspan(2).row();
		
		menu.add("Size");
		menu.add(envSize = createSizeSelector()).row();
		
		menu.add(UI.change(new TextButton("Export", getSkin()), event->exportEnvMap())).colspan(2).row();
		
		menu.add(title("Irradiance Map")).colspan(2).row();
		// TODO params
		menu.add("Size");
		menu.add(irrSize = createSizeSelector()).row();

		menu.add(UI.change(new TextButton("Generate", getSkin()), event->settings.invalidateIrradiance()));
		menu.add(UI.change(new TextButton("Export", getSkin()), event->{})).row(); // TODO
		
		menu.add(title("Radiance Map")).colspan(2).row();
		// TODO params
		menu.add("Size");
		menu.add(radSize = createSizeSelector()).row();
		menu.add(UI.change(new TextButton("Generate", getSkin()), event->settings.invalidateRadiance()));
		menu.add(UI.change(new TextButton("Export", getSkin()), event->{})).row(); // TODO
		
		menu.add(title("BRDF Lookup")).colspan(2).row();
		// TODO params
		menu.add(brdfBuiltin = UI.toggle(getSkin(), "Builtin", settings.useDefaultBRDF, value->{settings.useDefaultBRDF = value; settings.invalidateBRDF();}));
		menu.add(brdf16 = UI.toggle(getSkin(), "16bits", settings.brdf16, value->{settings.brdf16 = value; settings.invalidateBRDF();}));
		menu.row();
		
		menu.add("Size");
		menu.add(brdfSize = createSizeSelector()).row();
		menu.add(UI.change(new TextButton("Generate", getSkin()), event->settings.invalidateBRDF()));
		menu.add(UI.change(new TextButton("Export", getSkin()), event->{})).row(); // TODO
		
		menu = menuLeft;
		
		menu.add(title("Camera Preview")).colspan(2).row();
		
		menu.add("FOV");
		menu.add(fovSlider = UI.change(new Slider(0, 180, .01f, false, getSkin()), event->settings.previewFov = fovSlider.getValue())).row();
		
		menu.add(title("Sun Preview")).colspan(2).row();
		
		menu.add("Sun power");
		menu.add(LightSlider = UI.change(new Slider(0, 3, .01f, false, getSkin()), event->settings.previewLightIntensity = LightSlider.getValue())).row();
		menu.add("Sun Hue");
		menu.add(LightHue = UI.change(new Slider(0, 360, .01f, false, getSkin()), event->settings.previewLightHue = LightHue.getValue())).row();
		menu.add("Sun Saturation");
		menu.add(LightSat = UI.change(new Slider(0, 1, .01f, false, getSkin()), event->settings.previewLightSaturation = LightSat.getValue())).row();
		menu.add("Azimuth");
		menu.add(LightAzimuth = UI.change(new Slider(0, 360, .01f, false, getSkin()), event->settings.previewLightAzymuth = LightAzimuth.getValue())).row();
		menu.add("Elevation");
		menu.add(LightElevation = UI.change(new Slider(-90, 90, .01f, false, getSkin()), event->settings.previewLightElevation = LightElevation.getValue())).row();
		
		menu.add(UI.change(new TextButton("Copy light code", getSkin()), event->copyLightCode())).colspan(2).row();
		
		menu.add(title("Lighting Preview")).colspan(2).row();
		
		menu.add("Ambient Light");
		menu.add(ambientSlider = UI.change(new Slider(0, 1, .01f, false, getSkin()), event->settings.previewAmbient = ambientSlider.getValue())).row();
		
		menu.add(title("Material Preview")).colspan(2).row();
		
		menu.add("Albedo");
		menu.add(albedoSlider = UI.change(new Slider(0, 1, .01f, false, getSkin()), event->settings.previewAlbedo = albedoSlider.getValue())).row();
		menu.add("Metallic");
		menu.add(metallicSlider = UI.change(new Slider(0, 1, .01f, false, getSkin()), event->settings.previewMetallic = metallicSlider.getValue())).row();
		menu.add("Roughness");
		menu.add(roughnessSlider = UI.change(new Slider(0, 1, .01f, false, getSkin()), event->settings.previewRoughness = roughnessSlider.getValue())).row();
		
		menu.add().colspan(2).expandY();
		
		imgRaw.setScaling(Scaling.fit);
		
		exposureSlider.setValue(0);
		fovSlider.setValue(settings.previewFov);
		albedoSlider.setValue(settings.previewAlbedo);
		metallicSlider.setValue(settings.previewMetallic);
		roughnessSlider.setValue(settings.previewRoughness);
		ambientSlider.setValue(settings.previewAmbient);
		LightSlider.setValue(settings.previewLightIntensity);
		LightAzimuth.setValue(settings.previewLightAzymuth);
		LightElevation.setValue(settings.previewLightElevation);
		
		LightHue.setValue(settings.previewLightHue);
		LightSat.setValue(settings.previewLightSaturation);
		
		UI.change(envSize, event->settings.setEnvMapSize(envSize.getSelected().size));
		UI.change(irrSize, event->settings.setIrradianceMapSize(irrSize.getSelected().size));
		UI.change(radSize, event->settings.setRadianceMapSize(radSize.getSelected().size));
		UI.change(brdfSize, event->settings.setBRDFMapSize(brdfSize.getSelected().size));
		
		UI.changeCompleted(exposureSlider, event->settings.invalidateMaps());
	}
	
	private void copyLightCode() {
		Color color = settings.getLightColor(new Color());
		Vector3 direction = settings.getLightDirection(new Vector3());
		
		/* generated code template:
		DirectionalLightEx sunLight = new DirectionalLightEx();
		sunLight.color.set(1f, 1f, 1f, 1f);
		sunLight.direction.set(1f, 1f, 1f);
		sunLight.intensity = 1f;
		sunLight.updateColor();
		*/

		String content = "";
		content += "DirectionalLightEx sunLight = new DirectionalLightEx();\n";
		content += "sunLight.color.set(" + color.r + "f, " + color.g + "f, " + color.b + "f, 1.0f);\n";
		content += "sunLight.direction.set(" + direction.x + "f, " + direction.y + "f, " + direction.z + "f);\n";
		content += "sunLight.intensity = " + settings.previewLightIntensity + "f;\n";
		content += "sunLight.updateColor();\n";
		Gdx.app.getClipboard().setContents(content);
	}

	private Actor title(String text) {
		Table t = new Table(getSkin());
		t.add(text).expandX().center().getActor().setColor(Color.ROYAL);
		float lum = .3f;
		t.setBackground(getSkin().newDrawable("white", lum, lum, lum, 1f));
		return t;
	}

	private void exportEnvMap() {
		FileSelector.instance.save(file->fire(new ExportEnvMapEvent(file.path())));
	}

	private SelectBox<MapSize> createSizeSelector(){
		SelectBox<MapSize> selector = new SelectBox<>(getSkin());
		return selector;
	}
	
	public void setHDRInfo(Header header){
		hdrInfo.setText(header.getProgramType() + " " + header.getWidth() + "x" + header.getHeight() + "\nexp=" + header.getExposure() + " gamma=" + header.getGamma());
	
		int base = GLUtils.sizeToPOT(header.getWidth()/4); 
		int max = GLUtils.sizeToPOT(GLUtils.getMaxCubemapSize());
		int min = 0;
		envSize.setItems(MapSize.createPOT(min, max));
		envSize.setSelectedIndex(base - min);
		
		irrSize.setItems(MapSize.createPOT(min, max));
		irrSize.setSelectedIndex(base - min - 2); // TODO ?
		
		radSize.setItems(MapSize.createPOT(min, max));
		radSize.setSelectedIndex(base - min - 1); // TODO ?
		
		brdfSize.setItems(MapSize.createPOT(min, max));
		brdfSize.setSelectedIndex(base - min - 1); // TODO ?
	}
	
	private float sliderToExposure(float value) {
		return (float)Math.pow(10, value);
	}

	private void openHDR() {
		FileSelector.instance.open(file->settings.setHDRPath(file.path()));
	}

	public void setHDRImage(Texture hdrTexture) 
	{
		imgRaw.setDrawable(new TextureRegionDrawable(hdrTexture));
	}

}
