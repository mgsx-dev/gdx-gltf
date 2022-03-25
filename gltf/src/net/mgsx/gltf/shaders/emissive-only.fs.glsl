#line 1

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

#ifdef GLSL3
#define varying in
out vec4 out_FragColor;
#define texture2D texture
#else
#define out_FragColor gl_FragColor
#endif

// Utilities
#define saturate(_v) clamp((_v), 0.0, 1.0)

#if defined(colorFlag)
varying vec4 v_color;
#endif

#ifdef blendedFlag
varying float v_opacity;
#ifdef alphaTestFlag
varying float v_alphaTest;
#endif //alphaTestFlag
#endif //blendedFlag

#ifdef textureFlag
varying MED vec2 v_texCoord0;
#endif // textureFlag

#ifdef textureCoord1Flag
varying MED vec2 v_texCoord1;
#endif // textureCoord1Flag

// texCoord unit mapping

#ifndef v_diffuseUV
#define v_diffuseUV v_texCoord0
#endif

#ifndef v_emissiveUV
#define v_emissiveUV v_texCoord0
#endif

#ifdef baseColorFactorFlag
uniform vec4 u_BaseColorFactor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef emissiveColorFlag
uniform vec4 u_emissiveColor;
#endif

#ifdef emissiveTextureFlag
uniform sampler2D u_emissiveTexture;
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
    // may need base texture alpha in case of blending
#ifdef baseColorFactorFlag
	float baseAlphaFactor = u_BaseColorFactor.a;
#else
	float baseAlphaFactor = 1.0;
#endif

#ifdef blendedFlag
#ifdef diffuseTextureFlag
    float baseAlpha = texture2D(u_diffuseTexture, v_diffuseUV).a * baseAlphaFactor;
#else
    float baseAlpha = baseAlphaFactor;
#endif
#else
    float baseAlpha = 1.0;
#endif

#ifdef colorFlag
    baseAlpha *= v_color.a;
#endif

    // Add emissive
#if defined(emissiveTextureFlag) && defined(emissiveColorFlag)
    vec3 color = SRGBtoLINEAR(texture2D(u_emissiveTexture, v_emissiveUV)).rgb * u_emissiveColor.rgb;
#elif defined(emissiveTextureFlag)
    vec3 color = SRGBtoLINEAR(texture2D(u_emissiveTexture, v_emissiveUV)).rgb;
#elif defined(emissiveColorFlag)
    vec3 color = u_emissiveColor.rgb;
#else
    vec3 color = vec3(0.0, 0.0, 0.0);
#endif
    
    // final frag color
#ifdef GAMMA_CORRECTION
    out_FragColor.rgb = vec3(pow(color,vec3(1.0/GAMMA_CORRECTION)));
#else
    out_FragColor.rgb = color;
#endif
    
    // Blending and Alpha Test
#ifdef blendedFlag
	out_FragColor.a = baseAlpha * v_opacity;
	#ifdef alphaTestFlag
		if (out_FragColor.a <= v_alphaTest)
			discard;
	#endif
#else
	out_FragColor.a = baseAlpha;
#endif

}
