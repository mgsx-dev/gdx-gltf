package net.mgsx.gltf.ibl.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public class IBLSettings {
	public int envMapSize;
	public int irrMapSize;
	public int radMapSize;
	public String hdrPath;
	public float exposure;
	public float previewFov = 67;
	public transient boolean envMapValid = true;
	public transient boolean hdrValid = true;
	public transient boolean irradianceValid = true;
	public transient boolean radianceValid = true;
	public float previewAlbedo = .7f;
	public float previewMetallic = .8f;
	public float previewRoughness = .3f;
	public float previewAmbient = 1f;
	public float previewLightIntensity = 1f;
	public float previewLightHue = 0f;
	public float previewLightSaturation = 0f;
	public boolean autoIrradiance = true;
	public boolean autoRadiance = true;
	public int brdfMapSize;
	public boolean autoBRDF = true;
	public boolean brdfMapValid = true;
	public boolean useDefaultBRDF = true;
	public boolean brdf16 = false;
	public float previewLightAzymuth = 180;
	public float previewLightElevation = 60;
	
	public void setEnvMapSize(int size) {
		this.envMapSize = size;
		invalidateEnvMap();
	}
	public void setIrradianceMapSize(int size) {
		this.irrMapSize = size;
		if(autoIrradiance) invalidateIrradiance();
	}

	public void setRadianceMapSize(int size) {
		this.radMapSize = size;
		if(autoRadiance) invalidateRadiance();
	}
	
	public void setBRDFMapSize(int size) {
		this.brdfMapSize = size;
		if(autoBRDF) invalidateBRDF();
	}
	
	public void invalidateBRDF() {
		brdfMapValid = false;
	}
	private void invalidateEnvMap() {
		envMapValid = false;
	}

	public void setHDRPath(String path) {
		this.hdrPath = path;
		invalidateAll();
	}

	private void invalidateAll() {
		hdrValid = false;
		envMapValid = false;
		if(autoRadiance) invalidateRadiance();
		if(autoIrradiance) invalidateIrradiance();
	}
	
	public void invalidateMaps() {
		if(autoRadiance) invalidateRadiance();
		if(autoIrradiance) invalidateIrradiance();
	}
	
	public void validate(){
		hdrValid = true;
		envMapValid = true;
		irradianceValid = true;
		radianceValid = true;
		brdfMapValid = true;
	}

	public void setExposure(float value) {
		this.exposure = value;
		invalidateEnvMap();
	}

	public void invalidateIrradiance() {
		irradianceValid = false;
	}
	
	public void invalidateRadiance() {
		radianceValid = false;
	}
	public Color getLightColor(Color color) {
		return color.fromHsv(previewLightHue, previewLightSaturation, 1f);
	}
	public Vector3 getLightDirection(Vector3 dir) {
		return dir.set(0,0,1)
		.rotate(Vector3.X, previewLightElevation)
		.rotate(Vector3.Y, previewLightAzymuth)
		.nor();
	}
	
}
