// Compatibility copied from PBR shader compat.vs.glsl

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

attribute vec4 a_position;

varying vec3 v_dir;

uniform mat4 u_worldTrans;

void main() {

	v_dir = (u_worldTrans * a_position).xyz;

	gl_Position = vec4(a_position.xy, 1.0, 1.0);
}
