attribute vec3 a_position;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
varying vec3 v_position;

void main() {
	v_position = a_position.xyz;
	vec4 pos = u_worldTrans * vec4(a_position, 1.0);
	gl_Position = u_projViewTrans * pos;
}
