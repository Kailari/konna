#version 330

layout (std140) uniform CameraInfo
{
    mat4 view;
    mat4 projection;
} camera_info;

uniform vec2 frame_size;
uniform int rows;
uniform int columns;

layout(location = 0) in vec2 in_pos;
layout(location = 1) in vec2 in_uv;
layout(location = 2) in mat4 in_model;
layout(location = 6) in float in_frame;

out vec2 frag_uv;

void main(void) {
    mat4 mvp = camera_info.projection * camera_info.view * in_model;
    gl_Position = mvp * vec4(in_pos.xy, 0.0, 1.0);

    float row = floor(in_frame / columns);
    float column = mod(in_frame, columns);
    frag_uv = in_uv * frame_size + vec2(frame_size.x * column, frame_size.y * row);
}
