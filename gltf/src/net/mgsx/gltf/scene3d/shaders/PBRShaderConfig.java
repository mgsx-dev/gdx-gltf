package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

public class PBRShaderConfig extends DefaultShader.Config
{
	public static enum SRGB{NONE,FAST,ACCURATE}
	public SRGB manualSRGB = SRGB.ACCURATE;
	
	/** string to prepend to shaders (version), automatic if null */
	public String glslVersion = null;
}
