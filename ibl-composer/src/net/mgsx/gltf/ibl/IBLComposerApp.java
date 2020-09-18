package net.mgsx.gltf.ibl;

import java.io.IOException;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import net.mgsx.gltf.ibl.events.ExportBRDFMapEvent;
import net.mgsx.gltf.ibl.events.ExportEnvMapEvent;
import net.mgsx.gltf.ibl.events.ExportIrradianceMapEvent;
import net.mgsx.gltf.ibl.events.ExportRadianceMapEvent;
import net.mgsx.gltf.ibl.events.UIScaleEvent;
import net.mgsx.gltf.ibl.model.IBLComposer;
import net.mgsx.gltf.ibl.model.IBLSettings;
import net.mgsx.gltf.ibl.ui.IBLComposerUI;
import net.mgsx.gltf.ibl.ui.IBLPreviewScene;
import net.mgsx.gltf.ibl.ui.UI;
import net.mgsx.gltf.ibl.util.GLCapabilities;
import net.mgsx.gltf.ibl.util.GLUtils;
import net.mgsx.gltf.ibl.util.PerfUtil;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;

public class IBLComposerApp extends ApplicationAdapter
{
	private Stage stage;
	private Skin skin;
	private IBLComposerUI ui;
	private IBLPreviewScene preview;
	private IBLComposer composer;
	private IBLSettings settings;
	private ScreenViewport viewport;
	
	public IBLComposerApp(String defaultHdr) {
		this.settings = new IBLSettings();
		this.settings.setHDRPath(defaultHdr);
	}
	
	@Override
	public void create() {
		GLCapabilities.i = new GLCapabilities();
		GLUtils.onGlError(code->Gdx.app.error("GL Error", "code " + code));
		skin = new Skin(Gdx.files.internal("skins/uiskin.json"));
		skin.getAtlas().getRegions().first().getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		stage = new Stage(viewport = new ScreenViewport());
		stage.addActor(ui = new IBLComposerUI(skin, settings));
		ui.setFillParent(true);
		preview = new IBLPreviewScene(settings);
		
		// XXX default BRDF... should be an option
		preview.setBRDF(new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png")));
		
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, preview.cameraController));
		UI.change(ui, event->{
			if(event instanceof ExportEnvMapEvent){
				exportEnvMap(((ExportEnvMapEvent) event).path);
			}
			if(event instanceof ExportIrradianceMapEvent){
				exportIrrMap(((ExportIrradianceMapEvent) event).path);
			}
			if(event instanceof ExportRadianceMapEvent){
				exportRadMap(((ExportRadianceMapEvent) event).path);
			}
			if(event instanceof ExportBRDFMapEvent){
				exportBRDFMap(((ExportBRDFMapEvent) event).path);
			}
			if(event instanceof UIScaleEvent){
				viewport.setUnitsPerPixel(1f / ((UIScaleEvent) event).newScale);
				viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
			}
		});
		
		if(!GLCapabilities.i.hasMemoryInfo){
			UI.dialog(stage, skin, "GPU Info", "Your driver doesn't support " + GLUtils.GL_NVX_gpu_memory_info_ext + " extension.\n" +
					"High resolution generation may crash your graphic card and freeze your OS.");
		}
	}
	
	private void exportBRDFMap(String path) {
		Pixmap pixmap = composer.getBRDFPixmap(settings.brdfMapSize, settings.brdf16);
		FileHandle fileBase = Gdx.files.absolute(path);
		FileHandle folder = fileBase.parent();
		String baseName = fileBase.nameWithoutExtension();
		FileHandle file = folder.child(baseName + ".png");
		PixmapIO.writePNG(file, pixmap);
	}

	private void exportRadMap(String path) {
		FileHandle fileBase = Gdx.files.absolute(path);
		FileHandle folder = fileBase.parent();
		String baseName = fileBase.nameWithoutExtension();
		// save 6 files per level
		Array<Pixmap> pixmaps = composer.getRadianceMapPixmaps(settings.radMapSize);
		int level = 0;
		for(int i=0 ; i<pixmaps.size ; ){
			for(int j=0 ; j<6 ; j++, i++){
				Pixmap pixmap = pixmaps.get(i);
				FileHandle file = folder.child(baseName + "_" + EnvironmentUtil.FACE_NAMES_NEG_POS[j] + "_" + level + ".png");
				PixmapIO.writePNG(file, pixmap);
				pixmap.dispose();
			}
			level++;
		}
		UI.dialog(stage, skin, "Export", "done");
	}

	private void exportIrrMap(String path) {
		FileHandle fileBase = Gdx.files.absolute(path);
		FileHandle folder = fileBase.parent();
		String baseName = fileBase.nameWithoutExtension();
		// save 6 files
		Array<Pixmap> pixmaps = composer.getIrradianceMapPixmaps(settings.irrMapSize);
		for(int i=0 ; i<pixmaps.size ; i++){
			Pixmap pixmap = pixmaps.get(i);
			FileHandle file = folder.child(baseName + "_" + EnvironmentUtil.FACE_NAMES_NEG_POS[i] + ".png");
			PixmapIO.writePNG(file, pixmap);
			pixmap.dispose();
		}
		UI.dialog(stage, skin, "Export", "done");
	}

	private void exportEnvMap(String path) {
		FileHandle fileBase = Gdx.files.absolute(path);
		FileHandle folder = fileBase.parent();
		String baseName = fileBase.nameWithoutExtension();
		// save 6 files
		Array<Pixmap> pixmaps = composer.getEnvMapPixmaps(settings.envMapSize, settings.exposure);
		for(int i=0 ; i<pixmaps.size ; i++){
			Pixmap pixmap = pixmaps.get(i);
			FileHandle file = folder.child(baseName + "_" + EnvironmentUtil.FACE_NAMES_NEG_POS[i] + ".png");
			PixmapIO.writePNG(file, pixmap);
			pixmap.dispose();
		}
		UI.dialog(stage, skin, "Export", "done");
	}

	private void openHDR(FileHandle file) {
		try {
			if(composer != null) composer.dispose();
			composer = new IBLComposer();
			composer.loadHDR(file);
			ui.setHDRInfo(composer.hdrHeader);
			ui.setHDRImage(composer.getHDRTexture());
		} catch (IOException e) {
			UI.dialog(stage, skin, "Error loading HDR file", "Unable to load " + file.path(), e);
		}
	}

	@Override
	public void resize(int width, int height) {
		preview.resize(width, height);
		stage.getViewport().update(width, height);
	}
	
	@Override
	public void render() {
		float delta = Gdx.graphics.getDeltaTime();
		
		stage.act();
		
		validate();
		
		preview.update(delta);

		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		preview.render();
		
		viewport.apply(true);
		stage.draw();
	}

	private void validate() 
	{
		if(!settings.hdrValid){
			if(settings.hdrPath != null){
				ui.hdrStats.setText(PerfUtil.millisecondsHuman(()->
					openHDR(Gdx.files.absolute(settings.hdrPath))
				));
			}
		}
		if(composer != null){
			if(!settings.envMapValid){
				ui.envStats.setText(PerfUtil.millisecondsHuman(()->
					preview.setEnvMap(composer.getEnvMap(settings.envMapSize, settings.exposure))
				));
			}
			if(!settings.irradianceValid){
				ui.irradianceStats.setText(PerfUtil.millisecondsHuman(()->
					preview.setDiffuse(composer.getIrradianceMap(settings.irrMapSize))
				));
			}
			if(!settings.radianceValid){
				ui.radianceStats.setText(PerfUtil.millisecondsHuman(()->
					preview.setSpecular(composer.getRadianceMap(settings.radMapSize))
				));
			}
			if(!settings.brdfMapValid){
				if(settings.useDefaultBRDF){
					preview.setBRDF(composer.getDefaultBRDFMap());
				}else{
					ui.brdfStats.setText(PerfUtil.millisecondsHuman(()->
						preview.setBRDF(composer.getBRDFMap(settings.brdfMapSize, settings.brdf16))
					));
				}
			}
		}
		settings.validate();
	}
}
