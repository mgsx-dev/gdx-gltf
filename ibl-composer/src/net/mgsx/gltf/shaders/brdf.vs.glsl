#version 330 core
layout (location = 0) in vec2 a_position;

out vec2 TexCoords;

uniform mat4 u_projModelView;

void main()
{
	TexCoords = a_position;
    gl_Position = u_projModelView * vec4(a_position, 0.0, 1.0);
}
