#version 150

layout (points) in;
layout (triangle_strip, max_vertices = 8) out;

layout (std140) uniform CameraInfo
{
    mat4 view;
    mat4 projection;
} camera_info;

uniform vec2 progressBarSize;

in Vertex {
    float progressPercentage;
    float progress;
    float maxProgress;
} in_vertices[];

out VertexData {
    float done;
} outVertex;

void main(void) {
    mat4 mvp = camera_info.projection * camera_info.view;
    vec4 p0 = gl_in[0].gl_Position;

    float width = progressBarSize.x;
    float halfWidth = width / 2.0;
    float height = progressBarSize.y;
    float left = p0.x;
    float right = p0.x + width;
    float top = p0.y;
    float bottom = p0.y + height;

    float middle = mix(left, right, in_vertices[0].progressPercentage);

    outVertex.done = 1.0;
    gl_Position = mvp * vec4(left, bottom, 0.0, 1.0);
    EmitVertex();
    gl_Position = mvp * vec4(left, top, 0.0, 1.0);
    EmitVertex();
    gl_Position = mvp * vec4(middle, bottom, 0.0, 1.0);
    EmitVertex();
    gl_Position = mvp * vec4(middle, top, 0.0, 1.0);
    EmitVertex();
    EndPrimitive();

    outVertex.done = 0.0;
    gl_Position = mvp * vec4(middle, bottom, 0.0, 1.0);
    EmitVertex();
    gl_Position = mvp * vec4(middle, top, 0.0, 1.0);
    EmitVertex();
    gl_Position = mvp * vec4(right, bottom, 0.0, 1.0);
    EmitVertex();
    gl_Position = mvp * vec4(right, top, 0.0, 1.0);
    EmitVertex();
    EndPrimitive();
}
