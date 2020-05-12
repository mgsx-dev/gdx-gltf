attribute vec3 a_position;
uniform mat4 u_projViewWorldTrans;


#ifdef position0Flag
attribute vec3 a_position0;
#endif

#ifdef position1Flag
attribute vec3 a_position1;
#endif

#ifdef position2Flag
attribute vec3 a_position2;
#endif

#ifdef position3Flag
attribute vec3 a_position3;
#endif

#ifdef position4Flag
attribute vec3 a_position4;
#endif

#ifdef position5Flag
attribute vec3 a_position5;
#endif

#ifdef position6Flag
attribute vec3 a_position6;
#endif

#ifdef position7Flag
attribute vec3 a_position7;
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


#if defined(diffuseTextureFlag) && defined(blendedFlag)
#define blendedTextureFlag
attribute vec2 a_texCoord0;
varying vec2 v_texCoords0;
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

#ifdef PackedDepthFlag
varying float v_depth;
#endif //PackedDepthFlag

void main() {
	#ifdef blendedTextureFlag
		v_texCoords0 = a_texCoord0;
	#endif // blendedTextureFlag

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

	#ifdef skinningFlag
		vec4 pos = u_projViewWorldTrans * skinning * vec4(morph_pos, 1.0);
	#else
		vec4 pos = u_projViewWorldTrans * vec4(morph_pos, 1.0);
	#endif

	#ifdef PackedDepthFlag
		v_depth = pos.z / pos.w * 0.5 + 0.5;
	#endif //PackedDepthFlag

	gl_Position = pos;
}
