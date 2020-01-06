#version 150

layout (points) in;
layout (triangle_strip, max_vertices = 8) out;

layout (std140) uniform CameraInfo
{
    mat4 view;
    mat4 projection;
} camera_info;

uniform vec2 healthBarSize;
uniform vec2 healthBarOffset;

in Vertex {
    float healthPercentage;
} in_vertices[];

out VertexData {
    float healthy;
} outVertex;

void main(void) {
    mat4 mvp = camera_info.projection * camera_info.view;
    vec4 p0 = gl_in[0].gl_Position;
    float halfWidth = healthBarSize.x / 2.0;
    float height = healthBarSize.y;
    float left = p0.x - halfWidth;
    float right = p0.x + halfWidth;
    float top = p0.y + healthBarOffset.y;
    float bottom = top + height;

    float middle = mix(left, right, in_vertices[0].healthPercentage);

    outVertex.healthy = 1;
    gl_Position = mvp * vec4(left, bottom, 0, 1);
    EmitVertex();
    gl_Position = mvp * vec4(left, top, 0, 1);
    EmitVertex();
    gl_Position = mvp * vec4(middle, bottom, 0, 1);
    EmitVertex();
    gl_Position = mvp * vec4(middle, top, 0, 1);
    EmitVertex();
    EndPrimitive();

    outVertex.healthy = 0;
    gl_Position = mvp * vec4(middle, bottom, 0, 1);
    EmitVertex();
    gl_Position = mvp * vec4(middle, top, 0, 1);
    EmitVertex();
    gl_Position = mvp * vec4(right, bottom, 0, 1);
    EmitVertex();
    gl_Position = mvp * vec4(right, top, 0, 1);
    EmitVertex();
    EndPrimitive();
}
