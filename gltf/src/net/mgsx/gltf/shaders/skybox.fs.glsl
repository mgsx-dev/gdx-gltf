// Compatibility copied from PBR shader compat.fs.glsl

// Extensions required for WebGL and some Android versions

#ifdef GLSL3
#define textureCubeLodEXT textureLod
#define texture2DLodEXT textureLod
#else
#ifdef USE_TEXTURE_LOD_EXT
#extension GL_EXT_shader_texture_lod: enable
#else
// Note : "textureCubeLod" is used for compatibility but should be "textureLod" for GLSL #version 130 (OpenGL 3.0+)
#define textureCubeLodEXT textureCubeLod
#define texture2DLodEXT texture2DLod
#endif
#endif

// required to have same precision in both shader for light structure
#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision highp float;
#else
#define MED
#define LOWP
#define HIGH
#endif

// translate GLSL 120 to 130
#ifdef GLSL3
#define varying in
out vec4 out_FragColor;
#define textureCube texture
#define texture2D texture
#else
#define out_FragColor gl_FragColor
#endif

uniform samplerCube u_environmentCubemap;

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef ENV_LOD
uniform float u_lod;
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

varying vec3 v_dir;

void main() {

#ifdef ENV_LOD
	vec4 color = SRGBtoLINEAR(textureCubeLodEXT(u_environmentCubemap, v_dir, u_lod));
#else
	vec4 color = SRGBtoLINEAR(textureCube(u_environmentCubemap, v_dir));
#endif

#ifdef diffuseColorFlag
	color *= u_diffuseColor;
#endif
#ifdef GAMMA_CORRECTION
	out_FragColor = vec4(pow(color.rgb, vec3(1.0/GAMMA_CORRECTION)), color.a);
#else
	out_FragColor = vec4(color.rgb, color.a);
#endif
}
