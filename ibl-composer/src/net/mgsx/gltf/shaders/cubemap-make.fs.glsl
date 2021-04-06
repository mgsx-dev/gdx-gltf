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

#define PI 3.1415926535897932384626433832795

varying vec2 v_position;

uniform mat4 u_mat;

uniform sampler2D u_hdr;

uniform float u_exposure;

void main() {
	vec3 dir = (u_mat * vec4(0.5 - v_position.x, 0.5 - v_position.y, 0.5, 1.0)).xyz;
	dir = normalize(dir);
	vec2 v_uv = vec2(0.0, 0.0);
	v_uv.x = 0.5 * atan(dir.z, dir.x) / PI + 0.5;
	v_uv.y = asin(dir.y) / PI + 0.5;
    vec4 color = texture2D(u_hdr, v_uv);

    // gamma correction
    vec3 envColor = color.rgb;
#define GAMMA_CORRECTION
#ifdef GAMMA_CORRECTION
	// envColor = envColor / (envColor + vec3(1.0));
	// envColor = pow(envColor, vec3(1.0/2.2));
    envColor = vec3(pow(envColor.r, u_exposure), pow(envColor.g, u_exposure), pow(envColor.b, u_exposure));
#endif

    gl_FragColor = vec4(envColor, 1.0);
}
