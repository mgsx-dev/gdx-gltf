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
#define attribute in
#define varying out
#endif

attribute vec3 a_position;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

uniform float u_extrusion;

#ifdef position0Flag
attribute vec3 a_position0;
#endif
#ifdef normal0Flag
attribute vec3 a_normal0;
#endif
#ifdef tangent0Flag
attribute vec3 a_tangent0;
#endif

#ifdef position1Flag
attribute vec3 a_position1;
#endif
#ifdef normal1Flag
attribute vec3 a_normal1;
#endif
#ifdef tangent1Flag
attribute vec3 a_tangent1;
#endif

#ifdef position2Flag
attribute vec3 a_position2;
#endif
#ifdef normal2Flag
attribute vec3 a_normal2;
#endif
#ifdef tangent2Flag
attribute vec3 a_tangent2;
#endif

#ifdef position3Flag
attribute vec3 a_position3;
#endif
#ifdef normal3Flag
attribute vec3 a_normal3;
#endif
#ifdef tangent3Flag
attribute vec3 a_tangent3;
#endif

#ifdef position4Flag
attribute vec3 a_position4;
#endif
#ifdef normal4Flag
attribute vec3 a_normal4;
#endif
#ifdef tangent4Flag
attribute vec3 a_tangent4;
#endif

#ifdef position5Flag
attribute vec3 a_position5;
#endif
#ifdef normal5Flag
attribute vec3 a_normal5;
#endif
#ifdef tangent5Flag
attribute vec3 a_tangent5;
#endif

#ifdef position6Flag
attribute vec3 a_position6;
#endif
#ifdef normal6Flag
attribute vec3 a_normal6;
#endif
#ifdef tangent6Flag
attribute vec3 a_tangent6;
#endif

#ifdef position7Flag
attribute vec3 a_position7;
#endif
#ifdef normal7Flag
attribute vec3 a_normal7;
#endif
#ifdef tangent7Flag
attribute vec3 a_tangent7;
#endif

#ifdef position0Flag
#ifndef morphTargetsFlag
#define morphTargetsFlag
#endif
uniform vec4 u_morphTargets1;
#endif

#ifdef position4Flag
uniform vec4 u_morphTargets2;
#endif


#ifdef normalFlag
attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
#ifdef tangentFlag
varying mat3 v_TBN;
#else
varying vec3 v_normal;
#endif
#endif // normalFlag

#ifdef tangentFlag
attribute vec4 a_tangent;
#endif


#ifdef boneWeight0Flag
#define boneWeightsFlag
attribute vec2 a_boneWeight0;
#endif //boneWeight0Flag

#ifdef boneWeight1Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight1;
#endif //boneWeight1Flag

#ifdef boneWeight2Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight2;
#endif //boneWeight2Flag

#ifdef boneWeight3Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight3;
#endif //boneWeight3Flag

#ifdef boneWeight4Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight4;
#endif //boneWeight4Flag

#ifdef boneWeight5Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight5;
#endif //boneWeight5Flag

#ifdef boneWeight6Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight6;
#endif //boneWeight6Flag

#ifdef boneWeight7Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
attribute vec2 a_boneWeight7;
#endif //boneWeight7Flag

#if defined(numBones) && defined(boneWeightsFlag)
#if (numBones > 0)
#define skinningFlag
#endif
#endif

#if defined(numBones)
#if numBones > 0
uniform mat4 u_bones[numBones];
#endif //numBones
#endif

void main() {

	#ifdef skinningFlag
		mat4 skinning = mat4(0.0);
		#ifdef boneWeight0Flag
			skinning += (a_boneWeight0.y) * u_bones[int(a_boneWeight0.x)];
		#endif //boneWeight0Flag
		#ifdef boneWeight1Flag
			skinning += (a_boneWeight1.y) * u_bones[int(a_boneWeight1.x)];
		#endif //boneWeight1Flag
		#ifdef boneWeight2Flag
			skinning += (a_boneWeight2.y) * u_bones[int(a_boneWeight2.x)];
		#endif //boneWeight2Flag
		#ifdef boneWeight3Flag
			skinning += (a_boneWeight3.y) * u_bones[int(a_boneWeight3.x)];
		#endif //boneWeight3Flag
		#ifdef boneWeight4Flag
			skinning += (a_boneWeight4.y) * u_bones[int(a_boneWeight4.x)];
		#endif //boneWeight4Flag
		#ifdef boneWeight5Flag
			skinning += (a_boneWeight5.y) * u_bones[int(a_boneWeight5.x)];
		#endif //boneWeight5Flag
		#ifdef boneWeight6Flag
			skinning += (a_boneWeight6.y) * u_bones[int(a_boneWeight6.x)];
		#endif //boneWeight6Flag
		#ifdef boneWeight7Flag
			skinning += (a_boneWeight7.y) * u_bones[int(a_boneWeight7.x)];
		#endif //boneWeight7Flag
	#endif //skinningFlag

	#ifdef morphTargetsFlag
		vec3 morph_pos = a_position;
		#ifdef position0Flag
			morph_pos += a_position0 * u_morphTargets1.x;
		#endif
		#ifdef position1Flag
			morph_pos += a_position1 * u_morphTargets1.y;
		#endif
		#ifdef position2Flag
			morph_pos += a_position2 * u_morphTargets1.z;
		#endif
		#ifdef position3Flag
			morph_pos += a_position3 * u_morphTargets1.w;
		#endif
		#ifdef position4Flag
			morph_pos += a_position4 * u_morphTargets2.x;
		#endif
		#ifdef position5Flag
			morph_pos += a_position5 * u_morphTargets2.y;
		#endif
		#ifdef position6Flag
			morph_pos += a_position6 * u_morphTargets2.z;
		#endif
		#ifdef position7Flag
			morph_pos += a_position7 * u_morphTargets2.w;
		#endif
	#else
		vec3 morph_pos = a_position;
	#endif

	#if defined(normalFlag)

		vec3 morph_nor = a_normal;
		#ifdef morphTargetsFlag
			#ifdef normal0Flag
				morph_nor += a_normal0 * u_morphTargets1.x;
			#endif
			#ifdef normal1Flag
				morph_nor += a_normal1 * u_morphTargets1.y;
			#endif
			#ifdef normal2Flag
				morph_nor += a_normal2 * u_morphTargets1.z;
			#endif
			#ifdef normal3Flag
				morph_nor += a_normal3 * u_morphTargets1.w;
			#endif
			#ifdef normal4Flag
				morph_nor += a_normal4 * u_morphTargets2.x;
			#endif
			#ifdef normal5Flag
				morph_nor += a_normal5 * u_morphTargets2.y;
			#endif
			#ifdef normal6Flag
				morph_nor += a_normal6 * u_morphTargets2.z;
			#endif
			#ifdef normal7Flag
				morph_nor += a_normal7 * u_morphTargets2.w;
			#endif
		#endif

		#if defined(skinningFlag)
			vec3 normal = (skinning * vec4(morph_nor, 0.0)).xyz;
		#else
			vec3 normal = morph_nor;
		#endif

	#endif // normalFlag

	#ifdef skinningFlag
		vec4 pos = u_worldTrans * skinning * vec4(morph_pos, 1.0);
	#else
		vec4 pos = u_worldTrans * vec4(morph_pos, 1.0);
	#endif

#ifdef WORLD_OUTLINE
	vec3 normalW = normalize(vec3(u_normalMatrix * normal.xyz));

	pos.xyz += normalW * u_extrusion;

	gl_Position = u_projViewTrans * pos;
#else
	gl_Position = u_projViewTrans * pos;

	vec3 normalW = normalize(vec3(u_normalMatrix * normal.xyz));

	vec4 normalS = u_projViewTrans * vec4(normalW, 0.0);

	// vec3 posNDC = gl_Position.xyz / gl_Position.w;

	float distanceFactor = (gl_Position.z + 2.0) * 0.5;

	gl_Position.xy += normalS.xy * u_extrusion * distanceFactor;
#endif
}
