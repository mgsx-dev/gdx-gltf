#ifdef ENV_ROTATION
uniform mat3 u_envRotation;
#endif


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

#ifdef mirrorSpecularFlag
uniform sampler2D u_mirrorSpecularSampler;
uniform float u_mirrorMipmapScale;
uniform vec3 u_mirrorNormal;
#endif

uniform vec2 u_viewportInv;

// Calculation of the lighting contribution from an optional Image Based Light source.
// Precomputed Environment Maps are required uniform inputs and are computed as outlined in [1].
// See our README.md on Environment Maps [3] for additional discussion.

vec2 sampleBRDF(PBRSurfaceInfo pbrSurface)
{
#ifdef brdfLUTTexture
    vec2 brdfSamplePoint = clamp(vec2(pbrSurface.NdotV, 1.0 - pbrSurface.perceptualRoughness), vec2(0.0, 0.0), vec2(1.0, 1.0));
	return texture2D(u_brdfLUT, brdfSamplePoint).xy;
#else // TODO not sure about how to compute it ...
	return vec2(pbrSurface.NdotV, pbrSurface.perceptualRoughness);
#endif
}

#ifdef transmissionSourceFlag


vec3 getTransmissionSample(vec2 fragCoord, float roughness)
{
#ifdef USE_TEX_LOD
    float framebufferLod = u_transmissionSourceMipmapScale * applyIorToRoughness(roughness);
    vec3 transmittedLight = tsSRGBtoLINEAR(texture2DLodEXT(u_transmissionSourceSampler, fragCoord.xy, framebufferLod)).rgb;
#else
    vec3 transmittedLight = tsSRGBtoLINEAR(texture2D(u_transmissionSourceSampler, fragCoord.xy)).rgb;
#endif
    return transmittedLight;
}


vec3 getIBLTransmissionContribution(PBRSurfaceInfo pbrSurface, vec3 n, vec3 v, vec2 brdf)
{
#ifdef volumeFlag
	// Compute transmission ray in order to change view angle with IBL
	vec3 transmissionRay = getVolumeTransmissionRay(n, -v, pbrSurface);
	vec3 refractedRayExit = v_position + transmissionRay;
#else
	vec3 refractedRayExit = v_position;
#endif

    // Project refracted vector on the framebuffer, while mapping to normalized device coordinates.
    vec4 ndcPos = u_projViewTrans * vec4(refractedRayExit, 1.0);
    vec2 refractionCoords = ndcPos.xy / ndcPos.w;
    refractionCoords += 1.0;
    refractionCoords /= 2.0;

    // Sample framebuffer to get pixel the refracted ray hits.
    vec3 transmittedLight = getTransmissionSample(refractionCoords, pbrSurface.perceptualRoughness);

#ifdef volumeFlag
    transmittedLight = applyVolumeAttenuation(transmittedLight, length(transmissionRay), pbrSurface);
#endif

    vec3 specularColor = pbrSurface.reflectance0 * brdf.x + pbrSurface.reflectance90 * brdf.y;

    return (1.0 - specularColor) * transmittedLight * pbrSurface.diffuseColor;
}

#else

vec3 getIBLTransmissionContribution(PBRSurfaceInfo pbrSurface, vec3 n, vec3 v, vec2 brdf)
{
#ifdef volumeFlag
	// Compute transmission ray in order to change view angle with IBL
	vec3 transmissionRay = getVolumeTransmissionRay(n, -v, pbrSurface);
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

#endif


PBRLightContribs getIBLContribution(PBRSurfaceInfo pbrSurface, vec3 n, vec3 reflection)
{
	vec2 brdf = sampleBRDF(pbrSurface);

#ifdef ENV_ROTATION
	vec3 diffuseDirection = u_envRotation * n;
#else
	vec3 diffuseDirection = n;
#endif
    vec3 diffuseLight = SRGBtoLINEAR(textureCube(u_DiffuseEnvSampler, diffuseDirection)).rgb;

#ifdef mirrorSpecularFlag
    float lod = (pbrSurface.perceptualRoughness * u_mirrorMipmapScale);
    vec2 mirrorCoord = gl_FragCoord.xy * u_viewportInv;

    // normal perturbation
	vec3 i1 = reflect(reflection, n);
	vec3 i2 = reflect(reflection, u_mirrorNormal);
	vec2 p = (u_projViewTrans * vec4(i2 - i1, 0.0)).xy;
	mirrorCoord += p / 2.0;
	mirrorCoord.x = 1.0 - mirrorCoord.x;

    vec3 specularLight = msSRGBtoLINEAR(texture2DLodEXT(u_mirrorSpecularSampler, mirrorCoord, lod)).rgb;

#else

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

#endif


#ifdef iridescenceFlag

    // GGX
    vec3 ggx_Fr = max(vec3(1.0 - pbrSurface.perceptualRoughness), pbrSurface.specularColor) - pbrSurface.specularColor;
    vec3 ggx_k_S = mix(pbrSurface.specularColor + ggx_Fr * pow(1.0 - pbrSurface.NdotV, 5.0), pbrSurface.iridescenceFresnel, pbrSurface.iridescenceFactor);
    vec3 ggx_FssEss = ggx_k_S * brdf.x + brdf.y;

    vec3 specular = specularLight * ggx_FssEss * pbrSurface.specularWeight;

    // Lambertian
    vec3 iridescenceF0Max = vec3(max(max(pbrSurface.iridescenceF0.r, pbrSurface.iridescenceF0.g), pbrSurface.iridescenceF0.b));
    vec3 mixedF0 = mix(pbrSurface.specularColor, iridescenceF0Max, pbrSurface.iridescenceFactor);

    vec3 lam_Fr = max(vec3(1.0 - pbrSurface.perceptualRoughness), mixedF0) - mixedF0;
    vec3 lam_k_S = mixedF0 + lam_Fr * pow(1.0 - pbrSurface.NdotV, 5.0);
    vec3 lam_FssEss = pbrSurface.specularWeight * lam_k_S * brdf.x + brdf.y;

    float Ems = (1.0 - (brdf.x + brdf.y));
    vec3 F_avg = pbrSurface.specularWeight * (mixedF0 + (1.0 - mixedF0) / 21.0);
    vec3 FmsEms = Ems * lam_FssEss * F_avg / (1.0 - F_avg * Ems);
    vec3 k_D = pbrSurface.diffuseColor * (1.0 - lam_FssEss + FmsEms);

    vec3 diffuse = (FmsEms + k_D) * diffuseLight;

#else
    vec3 diffuse = diffuseLight * pbrSurface.diffuseColor;
    vec3 specular = specularLight * (pbrSurface.specularColor * brdf.x + brdf.y) * pbrSurface.specularWeight;
#endif

#ifdef transmissionFlag
    vec3 transmission = getIBLTransmissionContribution(pbrSurface, n, -pbrSurface.v, brdf);
#else
    vec3 transmission = vec3(0.0);
#endif

    return PBRLightContribs(diffuse, specular, transmission);
}
