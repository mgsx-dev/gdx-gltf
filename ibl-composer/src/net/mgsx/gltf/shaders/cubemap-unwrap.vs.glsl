attribute vec3 a_position;
uniform mat4 u_projModelView;
varying vec2 v_position;

void main() {
	gl_Position = u_projModelView * vec4(a_position, 1.0);
	v_position = gl_Position.xy;
}
