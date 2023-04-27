#ifdef shadowMapFlag
uniform float u_shadowBias;
uniform sampler2D u_shadowTexture;
uniform float u_shadowPCFOffset;
varying vec3 v_shadowMapUv;

#ifdef numCSM

uniform sampler2D u_csmSamplers[numCSM];
uniform vec2 u_csmPCFClip[numCSM];
varying vec3 v_csmUVs[numCSM];

float getCSMShadowness(sampler2D sampler, vec3 uv, vec2 offset){
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
    return step(uv.z, dot(texture2D(sampler, uv.xy + offset), bitShifts) + u_shadowBias); // (1.0/255.0)
}

float getCSMShadow(sampler2D sampler, vec3 uv, float pcf){
	return (
			getCSMShadowness(sampler, uv, vec2(pcf,pcf)) +
			getCSMShadowness(sampler, uv, vec2(-pcf,pcf)) +
			getCSMShadowness(sampler, uv, vec2(pcf,-pcf)) +
			getCSMShadowness(sampler, uv, vec2(-pcf,-pcf)) ) * 0.25;
}
float getShadow()
{
	for(int i=0 ; i<numCSM ; i++){
		vec2 pcfClip = u_csmPCFClip[i];
		float pcf = pcfClip.x;
		float clip = pcfClip.y;
		vec3 uv = v_csmUVs[i];
		if(uv.x >= clip && uv.x <= 1.0 - clip &&
			uv.y >= clip && uv.y <= 1.0 - clip &&
			uv.z >= 0.0 && uv.z <= 1.0){
			return getCSMShadow(u_csmSamplers[i], uv, pcf);
		}
	}
	// default map
	return getCSMShadow(u_shadowTexture, v_shadowMapUv, u_shadowPCFOffset);
}

#else

float getShadowness(vec2 offset)
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
    return step(v_shadowMapUv.z, dot(texture2D(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts) + u_shadowBias); // (1.0/255.0)
}

float getShadow()
{
	return (//getShadowness(vec2(0,0)) +
			getShadowness(vec2(u_shadowPCFOffset, u_shadowPCFOffset)) +
			getShadowness(vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) +
			getShadowness(vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) +
			getShadowness(vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))) * 0.25;
}

#endif

#endif //shadowMapFlag
