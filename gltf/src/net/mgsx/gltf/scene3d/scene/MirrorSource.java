package net.mgsx.gltf.scene3d.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import net.mgsx.gltf.scene3d.attributes.ClippingPlaneAttribute;
import net.mgsx.gltf.scene3d.attributes.MirrorSourceAttribute;

/**
 * Mirror source renders reflected scene into a framebuffer.
 * The frame buffer can be used for the final rendering pass for dynamic reflection effect.
 * 
 * Mirror source replaces IBL specular cube map for all materials having a {@link net.mgsx.gltf.scene3d.attributes.MirrorAttribute}.
 * 
 * @author mgsx
 *
 */
public class MirrorSource implements Disposable
{
	protected FrameBuffer fbo;
	private int width;
	private int height;
	private final Vector3 originalCameraPosition = new Vector3();
	private final Vector3 originalCameraDirection = new Vector3();
	private final Vector3 originalCameraUp = new Vector3();
	
	private final Vector3 planeOrigin = new Vector3();
	private final Vector3 planeToCamera = new Vector3();
	
	/**
	 * enable/disable scene clipping. When enabled, objects behind mirror are not rendered.
	 */
	public boolean clipScene = true;
	
	protected final MirrorSourceAttribute mirrorAttribute = new MirrorSourceAttribute();
	
	private ClippingPlaneAttribute clippingPlane = new ClippingPlaneAttribute(Vector3.Y, 0);
	private Camera camera;
	private Environment environment;
	private SceneSkybox skyBox;
	
	public MirrorSource() {
		mirrorAttribute.textureDescription.minFilter = TextureFilter.MipMap;
		mirrorAttribute.textureDescription.magFilter = TextureFilter.Linear;
		mirrorAttribute.normal.set(clippingPlane.plane.normal);
	}
	
	protected FrameBuffer createFrameBuffer(int width, int height){
		return new FrameBuffer(Format.RGBA8888, width, height, true);
	}
	
	/**
	 * Set mirror source frame buffer size (usually the same as the final render resolution).
	 * 
	 * @param width when set to zero, default back buffer width will be used.
	 * @param height when set to zero, default back buffer height will be used.
	 */
	public void setSize(int width, int height){
		this.width = width;
		this.height = height;
	}
	
	/**
	 * set mirror plane
	 * @param nx normal x
	 * @param ny normal y
	 * @param nz normal z
	 * @param d plan origin
	 */
	public void setPlane(float nx, float ny, float nz, float d) {
		clippingPlane.plane.normal.set(nx, ny, nz).nor();
		clippingPlane.plane.d = d;
		mirrorAttribute.normal.set(clippingPlane.plane.normal);
	}
	
	/**
	 * set mirror source
	 * @param nx normal x
	 * @param ny normal y
	 * @param nz normal z
	 * @param d plan origin
	 * @param clipScene if objects behind the plane should be clipped.
	 */
	public MirrorSource set(float nx, float ny, float nz, float d, boolean clipScene) {
		setPlane(nx, ny, nz, d);
		this.clipScene = clipScene;
		return this;
	}
	
	public void begin(Camera camera, Environment environment, SceneSkybox skyBox) {
		this.camera = camera;
		this.environment = environment;
		this.skyBox = skyBox;
		
		setupCamera(camera, clippingPlane.plane);
		
		if(skyBox != null){
			skyBox.update(camera, 0);
		}
		if(clipScene){
			environment.set(clippingPlane);
		}
		
		ensureFrameBufferSize(width, height);
		fbo.begin();
		Gdx.gl.glClearColor(0,0,0,0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

	}
	
	public void end(){
		fbo.end();
		
		Texture texture = fbo.getColorBufferTexture();
		texture.bind();
		Gdx.gl.glGenerateMipmap(texture.glTarget);
		mirrorAttribute.textureDescription.texture = fbo.getColorBufferTexture();
		
		restoreCamera(camera);
		environment.set(mirrorAttribute);
		environment.remove(ClippingPlaneAttribute.Type);
		if(skyBox != null){
			skyBox.update(camera, 0);
		}
		
		this.camera = null;
		this.environment = null;
		this.skyBox = null;
	}
	
	private void setupCamera(Camera camera, Plane plane) {
		originalCameraPosition.set(camera.position);
		originalCameraDirection.set(camera.direction);
		originalCameraUp.set(camera.up);
		
		// compute mirror
		planeOrigin.set(clippingPlane.plane.normal).scl(clippingPlane.plane.d);
		planeToCamera.set(camera.position).sub(planeOrigin);
		camera.position.sub(planeToCamera);
		reflect(planeToCamera, clippingPlane.plane.normal);
		camera.position.add(planeToCamera);
		
		reflect(camera.direction, clippingPlane.plane.normal);
		reflect(camera.up, clippingPlane.plane.normal);
		camera.update();
	}

	private void reflect(Vector3 vector, Vector3 normal) {
		vector.mulAdd(normal, -2 * vector.dot(normal));
	}

	private void restoreCamera(Camera camera) {
		camera.position.set(originalCameraPosition);
		camera.direction.set(originalCameraDirection);
		camera.up.set(originalCameraUp);
		camera.update();
	}
	
	private void ensureFrameBufferSize(int width, int height) {
		if(width <= 0) width = Gdx.graphics.getBackBufferWidth();
		if(height <= 0) height = Gdx.graphics.getBackBufferHeight();
		
		if(fbo == null || fbo.getWidth() != width || fbo.getHeight() != height){
			if(fbo != null) fbo.dispose();
			fbo = createFrameBuffer(width, height);
		}
		
	}
	
	@Override
	public void dispose() {
		if(fbo != null){
			fbo.dispose();
		}
	}

}
