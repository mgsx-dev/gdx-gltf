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
#define varying in
out vec4 out_FragColor;
#else
#define out_FragColor gl_FragColor
#endif

uniform vec4 u_outlineColor;

void main() {
	out_FragColor = u_outlineColor;
}
