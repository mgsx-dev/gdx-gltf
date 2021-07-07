package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

public class PBRShaderConfig extends DefaultShader.Config
{
	public static enum SRGB{NONE,FAST,ACCURATE}
	public SRGB manualSRGB = SRGB.ACCURATE;
	
	/** string to prepend to shaders (version), automatic if null */
	public String glslVersion = null;

	/** Max vertex color layers. Default {@link PBRShader} only use 1 layer, 
	 * custom shaders can implements more.
	 */
	public int numVertexColors = 1;

	/**
	 * Some custom GLSL code to inject in shaders.
	 * If not null it will be added after #version 
	 */
	public String prefix = null;
}
