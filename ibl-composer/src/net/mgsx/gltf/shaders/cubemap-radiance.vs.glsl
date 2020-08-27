// from https://learnopengl.com/PBR/IBL/Diffuse-irradiance
#version 330 core
layout (location = 0) in vec3 a_position;

uniform mat4 projection;
uniform mat4 view;

out vec3 localPos;

void main()
{
    localPos = a_position;

    mat4 rotView = mat4(mat3(view)); // remove translation from the view matrix
    vec4 clipPos = projection * rotView * vec4(localPos, 1.0);

    gl_Position = clipPos.xyzw;
}
