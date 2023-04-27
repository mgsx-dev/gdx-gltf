// Constants
const float M_PI = 3.141592653589793;
const float c_MinRoughness = 0.04;

// Utilities
#define saturate(_v) clamp((_v), 0.0, 1.0)

// sRGB conversions
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

// sRGB conversions for transmission source
vec4 tsSRGBtoLINEAR(vec4 srgbIn)
{
    #ifdef TS_MANUAL_SRGB
    #ifdef TS_SRGB_FAST_APPROXIMATION
    vec3 linOut = pow(srgbIn.xyz,vec3(2.2));
    #else
    vec3 bLess = step(vec3(0.04045),srgbIn.xyz);
    vec3 linOut = mix( srgbIn.xyz/vec3(12.92), pow((srgbIn.xyz+vec3(0.055))/vec3(1.055),vec3(2.4)), bLess );
    #endif
    return vec4(linOut,srgbIn.w);;
    #else
    return srgbIn;
    #endif
}

// sRGB conversions for mirror source
vec4 msSRGBtoLINEAR(vec4 srgbIn)
{
    #ifdef MS_MANUAL_SRGB
    #ifdef MS_SRGB_FAST_APPROXIMATION
    vec3 linOut = pow(srgbIn.xyz,vec3(2.2));
    #else
    vec3 bLess = step(vec3(0.04045),srgbIn.xyz);
    vec3 linOut = mix( srgbIn.xyz/vec3(12.92), pow((srgbIn.xyz+vec3(0.055))/vec3(1.055),vec3(2.4)), bLess );
    #endif
    return vec4(linOut,srgbIn.w);;
    #else
    return srgbIn;
    #endif
}
