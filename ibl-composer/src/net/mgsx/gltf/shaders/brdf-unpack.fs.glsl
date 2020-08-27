#version 330 core
out vec2 FragColor;

in vec2 v_texCoords;
uniform sampler2D u_texture;

void main() {
	vec4 brdf = texture2D(u_texture, v_texCoords);
	FragColor = vec2(brdf.r + brdf.g / 255.0, brdf.b + brdf.a / 255.0);
}
