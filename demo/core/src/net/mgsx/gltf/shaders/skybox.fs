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

void main() {
    gl_FragColor = vec4(textureCube(u_environmentCubemap, v_position.xyz).rgb, 1.0);
}
