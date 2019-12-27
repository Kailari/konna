#version 150

in vec2 in_pos;

layout (std140) uniform CameraInfo
{
    mat4 view;
    mat4 projection;
} camera_info;

uniform mat4 model;

void main(void) {
    mat4 mvp = camera_info.projection * camera_info.view * model;
    gl_Position = mvp * vec4(in_pos.x, in_pos.y, 0.0, 1.0);
}
