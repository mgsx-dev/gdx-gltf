
#ifdef fogFlag
uniform vec4 u_fogColor;

#ifdef fogEquationFlag
uniform vec3 u_fogEquation;
#endif

#endif // fogFlag


#ifdef ambientLightFlag
uniform vec3 u_ambientLight;
#endif // ambientLightFlag


uniform vec4 u_cameraPosition;

uniform mat4 u_worldTrans;

varying vec3 v_position;


#ifdef transmissionSourceFlag
uniform sampler2D u_transmissionSourceSampler;
uniform float u_transmissionSourceMipmapScale;
#endif

uniform mat4 u_projViewTrans;

uniform vec4 u_clippingPlane;

void applyClippingPlane(){
#ifdef clippingPlaneFlag
	if(dot(v_position - u_clippingPlane.xyz * u_clippingPlane.w, u_clippingPlane.xyz) < 0.0){
		discard;
	}
#endif
}
