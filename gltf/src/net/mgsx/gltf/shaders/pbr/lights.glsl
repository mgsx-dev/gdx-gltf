
#if numDirectionalLights > 0
struct DirectionalLight
{
	vec3 color;
	vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif // numDirectionalLights


#if numPointLights > 0
struct PointLight
{
	vec3 color;
	vec3 position;
};
uniform PointLight u_pointLights[numPointLights];
#endif // numPointLights

#if numSpotLights > 0
struct SpotLight
{
	vec3 color;
	vec3 position;
	vec3 direction;
	float cutoffAngle;
	float exponent;
};
uniform SpotLight u_spotLights[numSpotLights];
#endif // numSpotLights


struct PBRLightInfo
{
    float NdotL;                  // cos angle between normal and light direction
    float NdotH;                  // cos angle between normal and half vector
    float LdotH;                  // cos angle between light direction and half vector
    float VdotH;                  // cos angle between view direction and half vector
};

struct PBRLightContribs
{
	vec3 diffuse;
	vec3 specular;
	vec3 transmission;
};


// Basic Lambertian diffuse
// Implementation from Lambert's Photometria https://archive.org/details/lambertsphotome00lambgoog
// See also [1], Equation 1
vec3 diffuse(PBRSurfaceInfo pbrSurface)
{
    return pbrSurface.diffuseColor / M_PI;
}

// The following equation models the Fresnel reflectance term of the spec equation (aka F())
// Implementation of fresnel from [4], Equation 15
vec3 specularReflection(PBRSurfaceInfo pbrSurface, PBRLightInfo pbrLight)
{
    return pbrSurface.reflectance0 + (pbrSurface.reflectance90 - pbrSurface.reflectance0) * pow(clamp(1.0 - pbrLight.VdotH, 0.0, 1.0), 5.0);
}

// This calculates the specular geometric attenuation (aka G()),
// where rougher material will reflect less light back to the viewer.
// This implementation is based on [1] Equation 4, and we adopt their modifications to
// alphaRoughness as input as originally proposed in [2].
float geometricOcclusion(PBRSurfaceInfo pbrSurface, PBRLightInfo pbrLight, float alphaRoughness)
{
    float NdotL = pbrLight.NdotL;
    float NdotV = pbrSurface.NdotV;
    float r = alphaRoughness;

    float attenuationL = 2.0 * NdotL / (NdotL + sqrt(r * r + (1.0 - r * r) * (NdotL * NdotL)));
    float attenuationV = 2.0 * NdotV / (NdotV + sqrt(r * r + (1.0 - r * r) * (NdotV * NdotV)));
    return attenuationL * attenuationV;
}

// The following equation(s) model the distribution of microfacet normals across the area being drawn (aka D())
// Implementation from "Average Irregularity Representation of a Roughened Surface for Ray Reflection" by T. S. Trowbridge, and K. P. Reitz
// Follows the distribution function recommended in the SIGGRAPH 2013 course notes from EPIC Games [1], Equation 3.
float microfacetDistribution(PBRSurfaceInfo pbrSurface, PBRLightInfo pbrLight, float alphaRoughness)
{
    float roughnessSq = alphaRoughness * alphaRoughness;
    float f = (pbrLight.NdotH * roughnessSq - pbrLight.NdotH) * pbrLight.NdotH + 1.0;
    return roughnessSq / (M_PI * f * f);
}

#ifdef volumeFlag

// Compute attenuated light as it travels through a volume.
vec3 applyVolumeAttenuation(vec3 radiance, float transmissionDistance, PBRSurfaceInfo pbrSurface)
{
    if (u_attenuationDistance == 0.0)
    {
        // Attenuation distance is +∞ (which we indicate by zero), i.e. the transmitted color is not attenuated at all.
        return radiance;
    }
    else
    {
        // Compute light attenuation using Beer's law.
        vec3 attenuationCoefficient = -log(u_attenuationColor) / u_attenuationDistance;
        vec3 transmittance = exp(-attenuationCoefficient * transmissionDistance); // Beer's law
        return transmittance * radiance;
    }
}


vec3 getVolumeTransmissionRay(vec3 n, vec3 v, PBRSurfaceInfo pbrSurface)
{
    // Direction of refracted light.
    vec3 refractionVector = refract(-v, n, 1.0 / u_ior);

    // Compute rotation-independant scaling of the model matrix.
    vec3 modelScale;
    modelScale.x = length(vec3(u_worldTrans[0].xyz));
    modelScale.y = length(vec3(u_worldTrans[1].xyz));
    modelScale.z = length(vec3(u_worldTrans[2].xyz));

    // The thickness is specified in local space.
    return normalize(refractionVector) * pbrSurface.thickness * modelScale;
}

#endif

float applyIorToRoughness(float roughness)
{
    // Scale roughness with IOR so that an IOR of 1.0 results in no microfacet refraction and
    // an IOR of 1.5 results in the default amount of microfacet refraction.
    return roughness * clamp(u_ior * 2.0 - 2.0, 0.0, 1.0);
}

vec3 getLightTransmission(PBRSurfaceInfo pbrSurface, vec3 l)
{
	vec3 n = pbrSurface.n;
	vec3 v = pbrSurface.v;

	vec3 l_mirror = normalize(l + 2.0*n*dot(-l, n));     // Mirror light reflection vector on surface
	vec3 h = normalize(l_mirror+v);               // Half vector between both l_mirror and v

	float NdotV = pbrSurface.NdotV;
	float NdotL = clamp(dot(n, l_mirror), 0.001, 1.0);
	float NdotH = clamp(dot(n, h), 0.0, 1.0);
	float LdotH = clamp(dot(l_mirror, h), 0.0, 1.0);
	float VdotH = clamp(dot(v, h), 0.0, 1.0);

	PBRLightInfo pbrLight = PBRLightInfo(
		NdotL,
		NdotH,
		LdotH,
		VdotH
	);

#ifdef iorFlag
	float alphaRoughness = applyIorToRoughness(pbrSurface.alphaRoughness);
#else
	float alphaRoughness = pbrSurface.alphaRoughness;
#endif

	// Calculate the shading terms for the microfacet specular shading model
	vec3 F = specularReflection(pbrSurface, pbrLight);
	float G = geometricOcclusion(pbrSurface, pbrLight, alphaRoughness);
	float D = microfacetDistribution(pbrSurface, pbrLight, alphaRoughness);

	// Calculation of analytical lighting contribution
	return (1.0 - F) * diffuse(pbrSurface) * D * G  / (4.0 * NdotL * NdotV);
}

// Light contribution calculation independent of light type
// l is a unit vector from surface point to light
PBRLightContribs getLightContribution(PBRSurfaceInfo pbrSurface, vec3 l, vec3 color)
{
	vec3 n = pbrSurface.n;
	vec3 v = pbrSurface.v;
	vec3 h = normalize(l+v);               // Half vector between both l and v

	float NdotV = pbrSurface.NdotV;
	float NdotL = clamp(dot(n, l), 0.001, 1.0);
	float NdotH = clamp(dot(n, h), 0.0, 1.0);
	float LdotH = clamp(dot(l, h), 0.0, 1.0);
	float VdotH = clamp(dot(v, h), 0.0, 1.0);

	PBRLightInfo pbrLight = PBRLightInfo(
		NdotL,
		NdotH,
		LdotH,
		VdotH
	);

	// Calculate the shading terms for the microfacet specular shading model
	vec3 F = specularReflection(pbrSurface, pbrLight) * pbrSurface.specularWeight;
	float G = geometricOcclusion(pbrSurface, pbrLight, pbrSurface.alphaRoughness);
	float D = microfacetDistribution(pbrSurface, pbrLight, pbrSurface.alphaRoughness);

	// Calculation of analytical lighting contribution
#ifdef iridescenceFlag
    vec3 iridescenceFresnelMax = vec3(max(max(pbrSurface.iridescenceFresnel.r, pbrSurface.iridescenceFresnel.g), pbrSurface.iridescenceFresnel.b));
    vec3 lam_F = mix(F, iridescenceFresnelMax * pbrSurface.specularWeight, pbrSurface.iridescenceFactor);
    vec3 diffuseContrib = (1.0 - lam_F) * diffuse(pbrSurface);

    vec3 ggx_F = mix(F, pbrSurface.iridescenceFresnel, pbrSurface.iridescenceFactor);
    vec3 specContrib = ggx_F * G * D / (4.0 * NdotL * NdotV);

#else
	vec3 diffuseContrib = (1.0 - F) * diffuse(pbrSurface);
	vec3 specContrib = F * G * D / (4.0 * NdotL * NdotV);
#endif

	// Obtain final intensity as reflectance (BRDF) scaled by the energy of the light (cosine law)
	vec3 factor = color * NdotL;

	// transmission
#ifdef transmissionFlag
	vec3 transmittedLight = getLightTransmission(pbrSurface, l);

#ifdef volumeFlag
    vec3 transmissionRay = getVolumeTransmissionRay(n, v, pbrSurface);
    transmittedLight = applyVolumeAttenuation(transmittedLight, length(transmissionRay), pbrSurface);
#endif


#else
	vec3 transmittedLight = vec3(0.0);
#endif


	return PBRLightContribs(diffuseContrib * factor, specContrib * factor, transmittedLight * factor);
}

#if numDirectionalLights > 0
PBRLightContribs getDirectionalLightContribution(PBRSurfaceInfo pbrSurface, DirectionalLight light)
{
    vec3 l = normalize(-light.direction);  // Vector from surface point to light
    return getLightContribution(pbrSurface, l, light.color);
}
#endif

#if numPointLights > 0
PBRLightContribs getPointLightContribution(PBRSurfaceInfo pbrSurface, PointLight light)
{
	// light direction and distance
	vec3 d = light.position - v_position.xyz;
	float dist2 = dot(d, d);
	d *= inversesqrt(dist2);

	return getLightContribution(pbrSurface, d, light.color / (1.0 + dist2));
}
#endif

#if numSpotLights > 0
PBRLightContribs getSpotLightContribution(PBRSurfaceInfo pbrSurface, SpotLight light)
{
	// light distance
	vec3 d = light.position - v_position.xyz;
	float dist2 = dot(d, d);
	d *= inversesqrt(dist2);

	// light direction
	vec3 l = normalize(-light.direction);  // Vector from surface point to light

	// from https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_lights_punctual/README.md#inner-and-outer-cone-angles
	float lightAngleOffset = light.cutoffAngle;
	float lightAngleScale = light.exponent;

	float cd = dot(l, d);
	float angularAttenuation = saturate(cd * lightAngleScale + lightAngleOffset);
	angularAttenuation *= angularAttenuation;

	return getLightContribution(pbrSurface, d, light.color * (angularAttenuation / (1.0 + dist2)));
}
#endif

