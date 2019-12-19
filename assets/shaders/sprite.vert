#version 330

in vec2 in_pos;
in vec2 in_uv;
in vec3 in_tint;

layout (std140) uniform CameraInfo
{
    mat4 view;
    mat4 projection;
} camera_info;

uniform mat4 model;

out vec2 v_uv;
out vec3 v_tint;

void main(void) {
    mat4 mvp = camera_info.projection * camera_info.view * model;
    gl_Position = mvp * vec4(in_pos.x, in_pos.y, 0.0, 1.0);

    v_uv = in_uv;
    v_tint = in_tint;
}
