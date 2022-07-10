#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

varying vec3 v_position;

uniform samplerCube u_environmentCubemap;

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef ENV_ROTATION
uniform mat3 u_envRotation;
#endif

vec4 SRGBtoLINEAR(vec4 srgbIn)
{
    #ifdef MANUAL_SRGB
    #ifdef SRGB_FAST_APPROXIMATION
    vec3 linOut = pow(srgbIn.xyz,vec3(2.2));
    #else //SRGB_FAST_APPROXIMATION
    vec3 bLess = step(vec3(0.04045),srgbIn.xyz);
    vec3 linOut = mix( srgbIn.xyz/vec3(12.92), pow((srgbIn.xyz+vec3(0.055))/vec3(1.055),vec3(2.4)), bLess );
    #endif //SRGB_FAST_APPROXIMATION
    return vec4(linOut,srgbIn.w);;
    #else //MANUAL_SRGB
    return srgbIn;
    #endif //MANUAL_SRGB
}

void main() {
#ifdef ENV_ROTATION
	vec3 direction = u_envRotation * v_position;
#else
	vec3 direction = v_position;
#endif

	vec4 color = SRGBtoLINEAR(textureCube(u_environmentCubemap, direction));
#ifdef diffuseColorFlag
	color *= u_diffuseColor;
#endif
#ifdef GAMMA_CORRECTION
	gl_FragColor = vec4(pow(color.rgb, vec3(1.0/GAMMA_CORRECTION)), 1.0);
#else
	gl_FragColor = vec4(color.rgb, 1.0);
#endif
}
