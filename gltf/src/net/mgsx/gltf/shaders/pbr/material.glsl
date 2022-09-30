#ifdef normalFlag
#ifdef tangentFlag
varying mat3 v_TBN;
#else
varying vec3 v_normal;
#endif

#endif //normalFlag

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

#ifndef v_normalUV
#define v_normalUV v_texCoord0
#endif

#ifndef v_occlusionUV
#define v_occlusionUV v_texCoord0
#endif

#ifndef v_metallicRoughnessUV
#define v_metallicRoughnessUV v_texCoord0
#endif

#ifndef v_transmissionUV
#define v_transmissionUV v_texCoord0
#endif

#ifndef v_thicknessUV
#define v_thicknessUV v_texCoord0
#endif

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef baseColorFactorFlag
uniform vec4 u_BaseColorFactor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
uniform float u_NormalScale;
#endif

#ifdef emissiveColorFlag
uniform vec4 u_emissiveColor;
#endif

#ifdef emissiveTextureFlag
uniform sampler2D u_emissiveTexture;
#endif

#ifdef occlusionTextureFlag
uniform sampler2D u_OcclusionSampler;
uniform float u_OcclusionStrength;
#endif

#ifdef metallicRoughnessTextureFlag
uniform sampler2D u_MetallicRoughnessSampler;
#endif

#ifdef transmissionTextureFlag
uniform sampler2D u_transmissionSampler;
#endif

#ifdef transmissionFlag
uniform float u_transmissionFactor;
#endif

#ifdef volumeFlag
uniform float u_thicknessFactor;
uniform float u_attenuationDistance;
uniform vec3 u_attenuationColor;
#endif

#ifdef thicknessTextureFlag
uniform sampler2D u_thicknessSampler;
#endif

#ifdef iorFlag
uniform float u_ior;
#else
#define u_ior 1.5
#endif

uniform vec2 u_MetallicRoughnessValues;

// Encapsulate the various inputs used by the various functions in the shading equation
// We store values in structs to simplify the integration of alternative implementations
// PBRSurfaceInfo contains light independant information (surface/material only)
// PBRLightInfo contains light information (incident rays)
struct PBRSurfaceInfo
{
	vec3 n;						  // Normal vector at surface point
	vec3 v;						  // Vector from surface point to camera
	float NdotV;                  // cos angle between normal and view direction
	float perceptualRoughness;    // roughness value, as authored by the model creator (input to shader)
	float metalness;              // metallic value at the surface
	vec3 reflectance0;            // full reflectance color (normal incidence angle)
	vec3 reflectance90;           // reflectance color at grazing angle
	float alphaRoughness;         // roughness mapped to a more linear change in the roughness (proposed by [2])
	vec3 diffuseColor;            // color contribution from diffuse lighting
	vec3 specularColor;           // color contribution from specular lighting

	float thickness;           	  // volume thickness at surface point (used for refraction)
};

#ifndef unlitFlag
// Find the normal for this fragment, pulling either from a predefined normal map
// or from the interpolated mesh normal and tangent attributes.
vec3 getNormal()
{
#ifdef tangentFlag
#ifdef normalTextureFlag
    vec3 n = texture2D(u_normalTexture, v_normalUV).rgb;
    n = normalize(v_TBN * ((2.0 * n - 1.0) * vec3(u_NormalScale, u_NormalScale, 1.0)));
#else
    vec3 n = normalize(v_TBN[2].xyz);
#endif
#else
    vec3 n = normalize(v_normal);
#endif

    return n;
}
#endif

float getThickness()
{
#ifdef volumeFlag
	float thickness = u_thicknessFactor;
#ifdef thicknessTextureFlag
	thickness *= texture2D(u_thicknessSampler, v_thicknessUV).g;
#endif
	return thickness;
#else
	return 0.0;
#endif
}
