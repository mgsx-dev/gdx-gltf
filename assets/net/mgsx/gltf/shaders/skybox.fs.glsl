#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

varying vec3 v_position;

uniform samplerCube u_environmentCubemap;

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

void main() {
	vec4 color = textureCube(u_environmentCubemap, v_position.xyz);
#ifdef diffuseColorFlag
	color *= u_diffuseColor;
#endif
    gl_FragColor = vec4(color.rgb, 1.0);
}
