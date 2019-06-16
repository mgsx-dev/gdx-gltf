package net.mgsx.gltf.loaders.shared;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

import net.mgsx.gltf.data.camera.GLTFCamera;
import net.mgsx.gltf.data.data.GLTFAccessor;
import net.mgsx.gltf.data.texture.GLTFSampler;
import net.mgsx.gltf.loaders.shared.animation.Interpolation;
import net.mgsx.gltf.scene3d.model.WeightVector;

public class GLTFTypes {

	/** https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#primitivemode */
	public static int mapPrimitiveMode(Integer glMode){
		if(glMode == null) return GL20.GL_TRIANGLES; // TODO not sure
		switch (glMode) {
		case 0: return GL20.GL_POINTS;
		case 1: return GL20.GL_LINES;
		case 2: return GL20.GL_LINE_LOOP;
		case 3: return GL20.GL_LINE_STRIP;
		case 4: return GL20.GL_TRIANGLES;
		case 5: return GL20.GL_TRIANGLE_STRIP;
		case 6: return GL20.GL_TRIANGLE_FAN;
		}
		throw new GdxRuntimeException("unsupported mode " + glMode);
	}
	
	public static Color mapColor(float [] c, Color defaultColor){
		if(c == null){
			return new Color(defaultColor);
		}
		if(c.length < 4){
			return new Color(c[0], c[1], c[2], 1f);
		}else{
			return new Color(c[0], c[1], c[2], c[3]);
		}
	}
	
	public static Quaternion map(Quaternion q, float[] fv) {
		return q.set(fv[0], fv[1], fv[2], fv[3]);
	}

	public static Quaternion map(Quaternion q, float[] fv, int offset) {
		return q.set(fv[offset+0], fv[offset+1], fv[offset+2], fv[offset+3]);
	}

	public static Vector3 map(Vector3 v, float[] fv) {
		return v.set(fv[0], fv[1], fv[2]);
	}
	public static Vector3 map(Vector3 v, float[] fv, int offset) {
		return v.set(fv[offset+0], fv[offset+1], fv[offset+2]);
	}
	
	public static WeightVector map(WeightVector w, float[] outputData, int offset)
	{
		for(int i=0 ; i<w.count ; i++){
			w.values[i] = outputData[offset + i];
		}
		return w;
	}
	

	// https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#accessor-element-size
	public static int accessorTypeSize(GLTFAccessor accessor){
		if("SCALAR".equals(accessor.type)){
			return 1;
		}else if("VEC2".equals(accessor.type)){
			return 2;
		}else if("VEC3".equals(accessor.type)){
			return 3;
		}else if("VEC4".equals(accessor.type)){
			return 4;
		}else if("MAT2".equals(accessor.type)){
			return 4;
		}else if("MAT3".equals(accessor.type)){
			return 9;
		}else if("MAT4".equals(accessor.type)){
			return 16;
		}else{
			throw new GdxRuntimeException("type not known yet : " + accessor.type);
		}
	}
	public static int accessorComponentTypeSize(GLTFAccessor accessor){
		switch(accessor.componentType){
		case 5120: return 1; // ubyte
		case 5121: return 1; // byte
		case 5122: return 2; // short
		case 5123: return 2; // ushort
		case 5125: return 4; // uint
		case 5126: return 4; // float
		default:
			throw new GdxRuntimeException("type not known yet : " + accessor.componentType);
		}
	}
	public static int accessorStrideSize(GLTFAccessor accessor){
		return accessorTypeSize(accessor) * accessorComponentTypeSize(accessor);
	}
	public static int accessorSize(GLTFAccessor accessor){
		return accessorStrideSize(accessor) * accessor.count;
	}

	public static Camera map(GLTFCamera glCamera) {
		if("perspective".equals(glCamera.type)){
			PerspectiveCamera camera = new PerspectiveCamera();
			camera.fieldOfView = glCamera.perspective.yfov * MathUtils.radiansToDegrees;
			camera.near = glCamera.perspective.znear;
			camera.far = glCamera.perspective.zfar;
			camera.viewportWidth = Gdx.graphics.getWidth();
			camera.viewportHeight = Gdx.graphics.getHeight();
			return camera;
		}
		else if("orthographic".equals(glCamera.type)){
			OrthographicCamera camera = new OrthographicCamera();
			camera.near = glCamera.orthographic.znear;
			camera.far = glCamera.orthographic.zfar;
			// TODO map xMag yMag to something ?!
			return camera;
		}else{
			throw new GdxRuntimeException("unknow camera type " + glCamera.type);
		}
	}

	// https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#sampler
	public static void mapTextureSampler(TextureDescriptor<Texture> textureDescriptor, GLTFSampler glSampler) {
		textureDescriptor.minFilter = GLTFTypes.mapTextureMinFilter(glSampler.minFilter);
		textureDescriptor.magFilter = GLTFTypes.mapTextureMagFilter(glSampler.magFilter);
		textureDescriptor.uWrap = GLTFTypes.mapTextureWrap(glSampler.wrapS);
		textureDescriptor.vWrap = GLTFTypes.mapTextureWrap(glSampler.wrapT);
	}
	public static void mapTextureSampler(TextureParameter textureParameter, GLTFSampler glSampler) {
		textureParameter.minFilter = GLTFTypes.mapTextureMinFilter(glSampler.minFilter);
		textureParameter.magFilter = GLTFTypes.mapTextureMagFilter(glSampler.magFilter);
		textureParameter.wrapU = GLTFTypes.mapTextureWrap(glSampler.wrapS);
		textureParameter.wrapV = GLTFTypes.mapTextureWrap(glSampler.wrapT);
	}
	
	// https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#samplerwraps
	// https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#samplerwrapt
	private static TextureWrap mapTextureWrap(Integer wrap) {
		if(wrap == null) return TextureWrap.Repeat;
		switch(wrap){
		case 33071: return TextureWrap.ClampToEdge;
		case 33648: return TextureWrap.MirroredRepeat;
		case 10497: return TextureWrap.Repeat;
		}
		throw new GdxRuntimeException("unexpected texture wrap " + wrap);
	}

	// https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#samplermagfilter
	public static TextureFilter mapTextureMagFilter(Integer filter) {
		if(filter == null) return TextureFilter.Linear;
		switch(filter){
		case 9728: return TextureFilter.Nearest;
		case 9729: return TextureFilter.Linear;
		}
		throw new GdxRuntimeException("unexpected texture mag filter " + filter);
	}
	
	// https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#samplerminfilter
	public static TextureFilter mapTextureMinFilter(Integer filter) {
		if(filter == null) return TextureFilter.Linear;
		switch(filter){
		case 9728: return TextureFilter.Nearest;
		case 9729: return TextureFilter.Linear;
		case 9984: return TextureFilter.MipMapNearestNearest;
		case 9985: return TextureFilter.MipMapLinearNearest;
		case 9986: return TextureFilter.MipMapNearestLinear;
		case 9987: return TextureFilter.MipMapLinearLinear;
		}
		throw new GdxRuntimeException("unexpected texture mag filter " + filter);
	}

	public static boolean isMipMapFilter(GLTFSampler sampler) {
		TextureFilter filter = mapTextureMinFilter(sampler.minFilter);
		switch(filter){
		case Nearest:
		case Linear:
			return false;
		case MipMapNearestNearest:
		case MipMapLinearNearest:
		case MipMapNearestLinear:
		case MipMapLinearLinear:
			return true;
		default:
			throw new GdxRuntimeException("unexpected texture min filter " + filter);
		}
	}
	
	// https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#animation-samplerinterpolation
	public static Interpolation mapInterpolation(String type){
		if(type == null) return Interpolation.LINEAR;
		if("LINEAR".equals(type)){
			return Interpolation.LINEAR;
		}else if("STEP".equals(type)){
			return Interpolation.STEP;
		}else if("CUBICSPLINE".equals(type)){
			return Interpolation.CUBICSPLINE;
		}else{
			throw new GdxRuntimeException("unexpected interpolation type " + type);
		}
	}
}
