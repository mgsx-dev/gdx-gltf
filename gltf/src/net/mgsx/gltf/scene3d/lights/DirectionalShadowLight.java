package net.mgsx.gltf.scene3d.lights;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.environment.ShadowMap;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;

/**
 * Copied from original deprecated DirectionalShadowLight with new features.
 */
public class DirectionalShadowLight extends DirectionalLightEx implements ShadowMap, Disposable
{
	protected static final float SQRT2 = (float)Math.sqrt(2.0);
	
	protected FrameBuffer fbo;
	protected Camera cam;
	protected final Vector3 tmpV = new Vector3();
	protected final TextureDescriptor textureDesc;
	protected final Vector3 center = new Vector3();

	public DirectionalShadowLight(){
		this(1024, 1024);
	}
	
	public DirectionalShadowLight(int shadowMapWidth, int shadowMapHeight){
		this(shadowMapWidth, shadowMapHeight, 100, 100, 0, 100);
	}
			
	public DirectionalShadowLight(int shadowMapWidth, int shadowMapHeight, float shadowViewportWidth,
			float shadowViewportHeight, float shadowNear, float shadowFar) {
		fbo = createFrameBuffer(shadowMapWidth, shadowMapHeight);
		cam = new OrthographicCamera(shadowViewportWidth, shadowViewportHeight);
		cam.near = shadowNear;
		cam.far = shadowFar;
		textureDesc = new TextureDescriptor();
		textureDesc.minFilter = textureDesc.magFilter = Texture.TextureFilter.Nearest;
		textureDesc.uWrap = textureDesc.vWrap = Texture.TextureWrap.ClampToEdge;
	}
	
	public DirectionalShadowLight setShadowMapSize(int shadowMapWidth, int shadowMapHeight){
		if(fbo == null || fbo.getWidth() != shadowMapWidth || fbo.getHeight() != shadowMapHeight){
			if(fbo != null){
				fbo.dispose();
			}
			fbo = createFrameBuffer(shadowMapWidth, shadowMapHeight);
		}
		return this;
	}
	
	protected FrameBuffer createFrameBuffer(int width, int height){
		return new FrameBuffer(Format.RGBA8888, width, height, true);
	}
	
	public DirectionalShadowLight setViewport(float shadowViewportWidth, float shadowViewportHeight, float shadowNear, float shadowFar){
		cam.viewportWidth = shadowViewportWidth;
		cam.viewportHeight = shadowViewportHeight;
		cam.near = shadowNear;
		cam.far = shadowFar;
		return this;
	}
	
	public DirectionalShadowLight setCenter(Vector3 center) {
		this.center.set(center);
		return this;
	}
	
	public DirectionalShadowLight setCenter(float x, float y, float z) {
		this.center.set(x, y, z);
		return this;
	}
	
	public DirectionalShadowLight setBounds(BoundingBox box){
		float w = box.getWidth();
		float h = box.getHeight();
		float d = box.getDepth();
		
		float s = Math.max(Math.max(w, h), d);
		
		w = h = d = s * SQRT2;
		
		box.getCenter(center);
		
		return setViewport(w, h, 0, d);
	}
	
	protected void validate(){
		float halfDepth = cam.near + 0.5f * (cam.far - cam.near);
		cam.position.set(direction).scl(-halfDepth).add(center);
		cam.direction.set(direction).nor();
		cam.normalizeUp();
		cam.update();
	}
	
	public void begin() {
		validate();
		final int w = fbo.getWidth();
		final int h = fbo.getHeight();
		fbo.begin();
		Gdx.gl.glViewport(0, 0, w, h);
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
		Gdx.gl.glScissor(1, 1, w - 2, h - 2);
	}
	
	public void end(){
		Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
		fbo.end();
	}
	
	public FrameBuffer getFrameBuffer () {
		return fbo;
	}

	public Camera getCamera () {
		return cam;
	}

	@Override
	public Matrix4 getProjViewTrans () {
		return cam.combined;
	}

	@Override
	public TextureDescriptor getDepthMap () {
		textureDesc.texture = fbo.getColorBufferTexture();
		return textureDesc;
	}
	
	@Override
	public void dispose() {
		if (fbo != null) fbo.dispose();
		fbo = null;
	}
	
	@Override
	public boolean equals(DirectionalLightEx other) {
		return (other instanceof DirectionalShadowLight) ? equals((DirectionalShadowLight)other) : false;
	}
	
	public boolean equals(DirectionalShadowLight other) {
		return (other != null) && (other == this); // No comparaison, same as identity ==
	}
}
