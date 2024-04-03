#line 1

#include <compat.fs.glsl>
#include <functions.glsl>
#ifdef iridescenceFlag
#include <iridescence.glsl>
#endif
#include <material.glsl>
#include <env.glsl>
#ifndef unlitFlag
#include <lights.glsl>
#include <shadows.glsl>
#endif
#ifdef USE_IBL
#include <ibl.glsl>
#endif

#ifdef unlitFlag

void main() {
	vec4 baseColor = getBaseColor();
    
    vec3 color = baseColor.rgb;

    // final frag color
#ifdef GAMMA_CORRECTION
    out_FragColor = vec4(pow(color,vec3(1.0/GAMMA_CORRECTION)), baseColor.a);
#else
    out_FragColor = vec4(color, baseColor.a);
#endif

	// Blending and Alpha Test
#ifdef blendedFlag
	out_FragColor.a = baseColor.a * u_opacity;
#ifdef alphaTestFlag
	if (out_FragColor.a <= u_alphaTest)
		discard;
#endif
#else
	out_FragColor.a = 1.0;
#endif
	applyClippingPlane();
}

#else

void main() {
	
    // Metallic and Roughness material properties are packed together
    // In glTF, these factors can be specified by fixed scalar values
    // or from a metallic-roughness map
    float perceptualRoughness = u_MetallicRoughnessValues.y;
    float metallic = u_MetallicRoughnessValues.x;
#ifdef metallicRoughnessTextureFlag
    // Roughness is stored in the 'g' channel, metallic is stored in the 'b' channel.
    // This layout intentionally reserves the 'r' channel for (optional) occlusion map data
    vec4 mrSample = texture2D(u_MetallicRoughnessSampler, v_metallicRoughnessUV);
    perceptualRoughness = mrSample.g * perceptualRoughness;
    metallic = mrSample.b * metallic;
#endif
    perceptualRoughness = clamp(perceptualRoughness, c_MinRoughness, 1.0);
    metallic = clamp(metallic, 0.0, 1.0);
    // Roughness is authored as perceptual roughness; as is convention,
    // convert to material roughness by squaring the perceptual roughness [2].
    float alphaRoughness = perceptualRoughness * perceptualRoughness;

    vec4 baseColor = getBaseColor();
    
#ifdef iorFlag
    vec3 f0 = vec3(pow(( u_ior - 1.0) /  (u_ior + 1.0), 2.0));
#else
    vec3 f0 = vec3(0.04); // from ior 1.5 value
#endif

    // Specular
#ifdef specularFlag
    float specularFactor = u_specularFactor;
#ifdef specularFactorTextureFlag
    specularFactor *= texture2D(u_specularFactorSampler, v_specularFactorUV).a;
#endif
#ifdef specularColorFlag
    vec3 specularColorFactor = u_specularColorFactor;
#else
    vec3 specularColorFactor = vec3(1.0);
#endif
#ifdef specularTextureFlag
    specularColorFactor *= SRGBtoLINEAR(texture2D(u_specularColorSampler, v_specularColorUV)).rgb;
#endif
    // Compute specular
    vec3 dielectricSpecularF0 = min(f0 * specularColorFactor, vec3(1.0));
    f0 = mix(dielectricSpecularF0, baseColor.rgb, metallic);
    vec3 specularColor = f0;
    float specularWeight = specularFactor;
    vec3 diffuseColor = mix(baseColor.rgb, vec3(0), metallic);
#else
    float specularWeight = 1.0;
    vec3 diffuseColor = baseColor.rgb * (vec3(1.0) - f0);
    diffuseColor *= 1.0 - metallic;
    vec3 specularColor = mix(f0, baseColor.rgb, metallic);
#endif


    // Compute reflectance.
    float reflectance = max(max(specularColor.r, specularColor.g), specularColor.b);

    // For typical incident reflectance range (between 4% to 100%) set the grazing reflectance to 100% for typical fresnel effect.
    // For very low reflectance range on highly diffuse objects (below 4%), incrementally reduce grazing reflecance to 0%.
    float reflectance90 = clamp(reflectance * 25.0, 0.0, 1.0);
    vec3 specularEnvironmentR0 = specularColor.rgb;
    vec3 specularEnvironmentR90 = vec3(1.0, 1.0, 1.0) * reflectance90;

    vec3 surfaceToCamera = u_cameraPosition.xyz - v_position;
    float eyeDistance = length(surfaceToCamera);

    vec3 n = getNormal();                             // normal at surface point
    vec3 v = surfaceToCamera / eyeDistance;        // Vector from surface point to camera
    vec3 reflection = -normalize(reflect(v, n));

    float NdotV = clamp(abs(dot(n, v)), 0.001, 1.0);

    PBRSurfaceInfo pbrSurface = PBRSurfaceInfo(
    	n,
		v,
		NdotV,
		perceptualRoughness,
		metallic,
		specularEnvironmentR0,
		specularEnvironmentR90,
		alphaRoughness,
		diffuseColor,
		specularColor,
		getThickness(),
		specularWeight
#ifdef iridescenceFlag
		, 0.0, 0.0, 0.0, vec3(0.0), vec3(0.0)
#endif
    );

#ifdef iridescenceFlag
    pbrSurface = getIridescenceInfo(pbrSurface);
#endif

    vec3 f_diffuse = vec3(0.0);
    vec3 f_specular = vec3(0.0);
    vec3 f_transmission = vec3(0.0);

    // Calculate lighting contribution from image based lighting source (IBL)

#if defined(USE_IBL) && defined(ambientLightFlag)
    PBRLightContribs contribIBL = getIBLContribution(pbrSurface, n, reflection);
    f_diffuse += contribIBL.diffuse * u_ambientLight;
    f_specular += contribIBL.specular * u_ambientLight;
    f_transmission += contribIBL.transmission * u_ambientLight;
    vec3 ambientColor = vec3(0.0, 0.0, 0.0);
#elif defined(USE_IBL)
    PBRLightContribs contribIBL = getIBLContribution(pbrSurface, n, reflection);
    f_diffuse += contribIBL.diffuse;
    f_specular += contribIBL.specular;
    f_transmission += contribIBL.transmission;
    vec3 ambientColor = vec3(0.0, 0.0, 0.0);
#elif defined(ambientLightFlag)
    vec3 ambientColor = u_ambientLight;
#else
    vec3 ambientColor = vec3(0.0, 0.0, 0.0);
#endif

    // Apply ambient occlusion only to ambient light
#ifdef occlusionTextureFlag
    float ao = texture2D(u_OcclusionSampler, v_occlusionUV).r;
    f_diffuse = mix(f_diffuse, f_diffuse * ao, u_OcclusionStrength);
    f_specular = mix(f_specular, f_specular * ao, u_OcclusionStrength);
#endif


#if (numDirectionalLights > 0)
    // Directional lights calculation
    PBRLightContribs contrib0 = getDirectionalLightContribution(pbrSurface, u_dirLights[0]);
#ifdef shadowMapFlag
    float shadows = getShadow();
    f_diffuse += contrib0.diffuse * shadows;
    f_specular += contrib0.specular * shadows;
    f_transmission += contrib0.transmission * shadows; // TODO does transmission affected by shadows ?
#else
    f_diffuse += contrib0.diffuse;
    f_specular += contrib0.specular;
    f_transmission += contrib0.transmission;
#endif

    for(int i=1 ; i<numDirectionalLights ; i++){
    	PBRLightContribs contrib = getDirectionalLightContribution(pbrSurface, u_dirLights[i]);
        f_diffuse += contrib.diffuse;
        f_specular += contrib.specular;
        f_transmission += contrib.transmission;
    }
#endif

#if (numPointLights > 0)
    // Point lights calculation
    for(int i=0 ; i<numPointLights ; i++){
    	PBRLightContribs contrib = getPointLightContribution(pbrSurface, u_pointLights[i]);
    	f_diffuse += contrib.diffuse;
    	f_specular += contrib.specular;
    	f_transmission += contrib.transmission;
    }
#endif // numPointLights

#if (numSpotLights > 0)
    // Spot lights calculation
    for(int i=0 ; i<numSpotLights ; i++){
    	PBRLightContribs contrib = getSpotLightContribution(pbrSurface, u_spotLights[i]);
    	f_diffuse += contrib.diffuse;
    	f_specular += contrib.specular;
    	f_transmission += contrib.transmission;
    }
#endif // numSpotLights

    // mix diffuse with transmission
#ifdef transmissionFlag
    f_diffuse = mix(f_diffuse, f_transmission, getTransmissionFactor());
#endif

    vec3 color = ambientColor + f_diffuse + f_specular;

    // Add emissive
#if defined(emissiveTextureFlag) && defined(emissiveColorFlag)
    vec3 emissive = SRGBtoLINEAR(texture2D(u_emissiveTexture, v_emissiveUV)).rgb * u_emissiveColor.rgb;
#elif defined(emissiveTextureFlag)
    vec3 emissive = SRGBtoLINEAR(texture2D(u_emissiveTexture, v_emissiveUV)).rgb;
#elif defined(emissiveColorFlag)
    vec3 emissive = u_emissiveColor.rgb;
#endif

#if defined(emissiveTextureFlag) || defined(emissiveColorFlag)
    color += emissive;
#endif

    
    // final frag color
#ifdef GAMMA_CORRECTION
    out_FragColor = vec4(pow(color,vec3(1.0/GAMMA_CORRECTION)), baseColor.a);
#else
    out_FragColor = vec4(color, baseColor.a);
#endif
    
#ifdef fogFlag
#ifdef fogEquationFlag
    float fog = (eyeDistance - u_fogEquation.x) / (u_fogEquation.y - u_fogEquation.x);
    fog = clamp(fog, 0.0, 1.0);
    fog = pow(fog, u_fogEquation.z);
#else
	float fog = min(1.0, eyeDistance * eyeDistance * u_cameraPosition.w);
#endif
	out_FragColor.rgb = mix(out_FragColor.rgb, u_fogColor.rgb, fog * u_fogColor.a);
#endif

    // Blending and Alpha Test
#ifdef blendedFlag
	out_FragColor.a = baseColor.a * u_opacity;
	#ifdef alphaTestFlag
		if (out_FragColor.a <= u_alphaTest)
			discard;
	#endif
#else
	out_FragColor.a = 1.0;
#endif

	applyClippingPlane();

}

#endif
