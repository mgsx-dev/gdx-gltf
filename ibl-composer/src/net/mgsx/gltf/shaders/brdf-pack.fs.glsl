#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main() {
	vec2 brdf = texture2D(u_texture, v_texCoords).rg;
    gl_FragColor = vec4(brdf.r, fract(brdf.r * 255.0), brdf.g, fract(brdf.g * 255.0));
}
