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
vec3 getIBLContribution(PBRSurfaceInfo pbrSurface, vec3 n, vec3 reflection)
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
    vec3 specular = specularLight * (pbrSurface.specularColor * brdf.x + brdf.y);

    return diffuse + specular;
}
#endif
