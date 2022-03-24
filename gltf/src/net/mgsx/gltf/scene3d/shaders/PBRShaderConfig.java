package net.mgsx.gltf.scene3d.shaders;

import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

public class PBRShaderConfig extends DefaultShader.Config
{
	public static enum SRGB{NONE,FAST,ACCURATE}
	/**
	 * Enable conversion of SRGB space textures into linear space in shader.
	 * Should be {@link SRGB#NONE} if your textures are already in linear space
	 * or automatically converted by OpenGL when using {@link com.badlogic.gdx.graphics.GL30#GL_SRGB} format.
	 */
	public SRGB manualSRGB = SRGB.ACCURATE;

	/**
	 * Enable/Disable gamma correction.
	 * Since gamma correction should only be done once as a final step,
	 * this should be disabled when you want to apply it later (eg. in case of post process lighting calculation).
	 * It also should be disabled when drawing to SRGB framebuffers since gamma correction will
	 * be automatically done by OpenGL.
	 * Default is true.
	 */
	public boolean gammaCorrection = true;
	
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
