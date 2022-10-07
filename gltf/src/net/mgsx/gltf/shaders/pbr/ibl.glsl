#ifdef ENV_ROTATION
uniform mat3 u_envRotation;
#endif



#ifdef USE_IBL
uniform samplerCube u_DiffuseEnvSampler;

#ifdef diffuseSpecularEnvSeparateFlag
uniform samplerCube u_SpecularEnvSampler;
#else
#define u_SpecularEnvSampler u_DiffuseEnvSampler
#endif

#ifdef brdfLUTTexture
uniform sampler2D u_brdfLUT;
#endif

#ifdef USE_TEX_LOD
uniform float u_mipmapScale; // = 9.0 for resolution of 512x512
#endif

#endif


// Calculation of the lighting contribution from an optional Image Based Light source.
// Precomputed Environment Maps are required uniform inputs and are computed as outlined in [1].
// See our README.md on Environment Maps [3] for additional discussion.
#ifdef USE_IBL

vec3 getIBLTransmissionContribution(PBRSurfaceInfo pbrSurface, vec3 n, vec3 v)
{
    // Sample GGX LUT to get the specular component.

	// TODO refactor with IBL contrib : sampleBRDF, sampleGGX, sampleLambertian

#ifdef brdfLUTTexture
    vec2 brdfSamplePoint = clamp(vec2(pbrSurface.NdotV, pbrSurface.perceptualRoughness), vec2(0.0, 0.0), vec2(1.0, 1.0));
	vec2 brdf = texture2D(u_brdfLUT, brdfSamplePoint).xy;
#else // TODO not sure about how to compute it ...
	vec2 brdf = vec2(pbrSurface.NdotV, pbrSurface.perceptualRoughness);
#endif

	// TODO have an option to either sample IBL or sample FBO (with prviously opaque objects rendered)

#ifdef volumeFlag
	// Compute transmission ray in order to change view angle with IBL
	vec3 transmissionRay = getVolumeTransmissionRay(n, v, pbrSurface);
	vec3 refractedRayExit = v_position + transmissionRay;
	v = normalize(refractedRayExit - u_cameraPosition.xyz);
#endif

#ifdef ENV_ROTATION
	vec3 specularDirection = u_envRotation * v;
#else
	vec3 specularDirection = v;
#endif

#ifdef USE_TEX_LOD
    // IOR has impact on roughness
#ifdef iorFlag
	float lod = applyIorToRoughness(pbrSurface.perceptualRoughness) * u_mipmapScale;
#else
	float lod = pbrSurface.perceptualRoughness * u_mipmapScale;
#endif


    vec3 specularLight = SRGBtoLINEAR(textureCubeLodEXT(u_SpecularEnvSampler, specularDirection, lod)).rgb;
#else
    vec3 specularLight = SRGBtoLINEAR(textureCube(u_SpecularEnvSampler, specularDirection)).rgb;
#endif


    vec3 specularColor = pbrSurface.reflectance0 * brdf.x + pbrSurface.reflectance90 * brdf.y;

    vec3 attenuatedColor = specularLight;

#ifdef volumeFlag
    attenuatedColor = applyVolumeAttenuation(attenuatedColor, length(transmissionRay), pbrSurface);
#endif

    return (1.0 - specularColor) * attenuatedColor * pbrSurface.diffuseColor;
}


PBRLightContribs getIBLContribution(PBRSurfaceInfo pbrSurface, vec3 n, vec3 reflection)
{
    // retrieve a scale and bias to F0. See [1], Figure 3
#ifdef brdfLUTTexture
	vec2 brdf = texture2D(u_brdfLUT, vec2(pbrSurface.NdotV, 1.0 - pbrSurface.perceptualRoughness)).xy;
#else // TODO not sure about how to compute it ...
	vec2 brdf = vec2(pbrSurface.NdotV, pbrSurface.perceptualRoughness);
#endif

#ifdef ENV_ROTATION
	vec3 diffuseDirection = u_envRotation * n;
#else
	vec3 diffuseDirection = n;
#endif
    vec3 diffuseLight = SRGBtoLINEAR(textureCube(u_DiffuseEnvSampler, diffuseDirection)).rgb;

#ifdef ENV_ROTATION
	vec3 specularDirection = u_envRotation * reflection;
#else
	vec3 specularDirection = reflection;
#endif

#ifdef USE_TEX_LOD
    float lod = (pbrSurface.perceptualRoughness * u_mipmapScale);
    vec3 specularLight = SRGBtoLINEAR(textureCubeLodEXT(u_SpecularEnvSampler, specularDirection, lod)).rgb;
#else
    vec3 specularLight = SRGBtoLINEAR(textureCube(u_SpecularEnvSampler, specularDirection)).rgb;
#endif

    vec3 diffuse = diffuseLight * pbrSurface.diffuseColor;
    vec3 specular = specularLight * (pbrSurface.specularColor * brdf.x + brdf.y) * pbrSurface.specularWeight;

#ifdef transmissionFlag
    vec3 transmission = getIBLTransmissionContribution(pbrSurface, n, -pbrSurface.v);
#else
    vec3 transmission = vec3(0.0);
#endif

    return PBRLightContribs(diffuse, specular, transmission);
}

#endif
