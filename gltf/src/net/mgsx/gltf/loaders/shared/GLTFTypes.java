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
import net.mgsx.gltf.loaders.exceptions.GLTFIllegalException;
import net.mgsx.gltf.loaders.shared.animation.Interpolation;
import net.mgsx.gltf.scene3d.model.CubicQuaternion;
import net.mgsx.gltf.scene3d.model.CubicVector3;
import net.mgsx.gltf.scene3d.model.CubicWeightVector;
import net.mgsx.gltf.scene3d.model.WeightVector;

public class GLTFTypes {

	// https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#accessor-element-size
	
	public static final String TYPE_SCALAR = "SCALAR";
	public static final String TYPE_VEC2 = "VEC2";
	public static final String TYPE_VEC3 = "VEC3";
	public static final String TYPE_VEC4 = "VEC4";
	public static final String TYPE_MAT2 = "MAT2";
	public static final String TYPE_MAT3 = "MAT3";
	public static final String TYPE_MAT4 = "MAT4";

	public static final int C_BYTE = 5120;
	public static final int C_UBYTE = 5121;
	public static final int C_SHORT = 5122;
	public static final int C_USHORT = 5123;
	public static final int C_UINT = 5125;
	public static final int C_FLOAT = 5126;
	
	
	/** https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#primitivemode */
	public static int mapPrimitiveMode(Integer glMode){
		if(glMode == null) return GL20.GL_TRIANGLES;
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
	public static CubicVector3 map(CubicVector3 v, float[] fv, int offset) {
		v.tangentIn.set(fv[offset+0], fv[offset+1], fv[offset+2]);
		v.set(fv[offset+3], fv[offset+4], fv[offset+5]);
		v.tangentOut.set(fv[offset+6], fv[offset+7], fv[offset+8]);
		return v;
	}
	public static CubicQuaternion map(CubicQuaternion v, float[] fv, int offset) {
		v.tangentIn.set(fv[offset+0], fv[offset+1], fv[offset+2], fv[offset+3]);
		v.set(fv[offset+4], fv[offset+5], fv[offset+6], fv[offset+7]);
		v.tangentOut.set(fv[offset+8], fv[offset+9], fv[offset+10], fv[offset+11]);
		return v;
	}
	
	public static WeightVector map(WeightVector w, float[] outputData, int offset)
	{
		for(int i=0 ; i<w.count ; i++){
			w.values[i] = outputData[offset + i];
		}
		return w;
	}
	
	/** https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#animations end of chapter :
	 * When used with CUBICSPLINE interpolation, tangents (ak, bk) and values (vk) are grouped within keyframes:
	 * a1,a2,...an,v1,v2,...vn,b1,b2,...bn
	 *  */
	public static CubicWeightVector map(CubicWeightVector w, float[] outputData, int offset)
	{
		for(int i=0 ; i<w.count ; i++){
			w.tangentIn.values[i] = outputData[offset + i];
		}
		offset += w.count;
		for(int i=0 ; i<w.count ; i++){
			w.values[i] = outputData[offset + i];
		}
		offset += w.count;
		for(int i=0 ; i<w.count ; i++){
			w.tangentOut.values[i] = outputData[offset + i];
		}
		return w;
	}
	

	// https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#accessor-element-size
	public static int accessorTypeSize(GLTFAccessor accessor){
		if(TYPE_SCALAR.equals(accessor.type)){
			return 1;
		}else if(TYPE_VEC2.equals(accessor.type)){
			return 2;
		}else if(TYPE_VEC3.equals(accessor.type)){
			return 3;
		}else if(TYPE_VEC4.equals(accessor.type)){
			return 4;
		}else if(TYPE_MAT2.equals(accessor.type)){
			return 4;
		}else if(TYPE_MAT3.equals(accessor.type)){
			return 9;
		}else if(TYPE_MAT4.equals(accessor.type)){
			return 16;
		}else{
			throw new GLTFIllegalException("illegal accessor type: " + accessor.type);
		}
	}
	public static int accessorComponentTypeSize(GLTFAccessor accessor){
		switch(accessor.componentType){
		case C_UBYTE: 
		case C_BYTE:   
			return 1; 
		case C_SHORT:
		case C_USHORT: 
			return 2; 
		case C_UINT:
		case C_FLOAT:  
			return 4; 
		default:
			throw new GLTFIllegalException("illegal accessor component type: " + accessor.componentType);
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
			// see https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#perspectivezfar
			// emulate an infinite matrix (based on 16 bits depth buffer)
			// TODO is it the proper ay to do it?
			float znear = glCamera.perspective.znear;
			float zfar = glCamera.perspective.zfar != null ? glCamera.perspective.zfar : znear * 16384f; 
			
			// convert scale ratio to canvas size
			float canvasRatio =  (float)Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight();
			float aspectRatio = glCamera.perspective.aspectRatio != null ? glCamera.perspective.aspectRatio : canvasRatio;
			float yfov = (float)Math.atan(Math.tan(glCamera.perspective.yfov * 0.5) * aspectRatio / canvasRatio) * 2.0f;
			
			PerspectiveCamera camera = new PerspectiveCamera();
			
		    camera.fieldOfView = yfov * MathUtils.radiansToDegrees;
			camera.near = znear;
			camera.far = zfar;
			camera.viewportWidth = Gdx.graphics.getWidth();
			camera.viewportHeight = Gdx.graphics.getHeight();
			return camera;
		}
		else if("orthographic".equals(glCamera.type)){
			OrthographicCamera camera = new OrthographicCamera();
			camera.near = glCamera.orthographic.znear;
			camera.far = glCamera.orthographic.zfar;
			float canvasRatio =  (float)Gdx.graphics.getWidth() / (float)Gdx.graphics.getHeight();
			camera.viewportWidth = glCamera.orthographic.xmag;
			camera.viewportHeight = glCamera.orthographic.ymag / canvasRatio;
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
	public static void mapTextureSampler(TextureParameter textureParameter) {
		textureParameter.minFilter = GLTFTypes.mapTextureMinFilter(null);
		textureParameter.magFilter = GLTFTypes.mapTextureMagFilter(null);
		textureParameter.wrapU = GLTFTypes.mapTextureWrap(null);
		textureParameter.wrapV = GLTFTypes.mapTextureWrap(null);
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
