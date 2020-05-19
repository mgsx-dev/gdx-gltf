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

varying vec2 v_position;

uniform vec3 u_direction;
uniform float u_exponent;
uniform vec4 u_ambient;
uniform vec4 u_diffuse;

void main() {
    vec3 worldVec = normalize(vec3(v_position, 1.0));
    float rate = clamp(dot(worldVec, u_direction), 0.0, 1.0);
    rate = pow(rate, u_exponent);
    gl_FragColor = mix(u_ambient, u_diffuse, rate);
}
