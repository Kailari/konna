#version 150

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

layout (std140) uniform CameraInfo
{
    mat4 view;
    mat4 projection;
} camera_info;

in Vertex {
    float alert_status;
    float shooting_status;
    float base_rotation;
    float target_pos;
} in_vertices[];

out VertexData {
    vec2 uv;
} out_vertex;

void main(void) {
    mat4 mvp = camera_info.projection * camera_info.view;
    vec4 p0 = gl_in[0].gl_Position;

    vec2 size = vec2(2.0, 2.0);
    vec2 origin = vec2(1.0, 1.0);

    vec2 bottom_left = p0.xy -origin;
    vec2 top_left = p0.xy - origin + vec2(0, size.y);
    vec2 bottom_right = p0.xy - origin + vec2(size.x, 0);
    vec2 top_right = p0.xy - origin + size;

    float u0 = 0.0;
    float u1 = 1.0 / 3.0;
    float v0 = 0.0;
    float v1 = 1.0 / 2.0;

    // Bottom left
    out_vertex.uv = vec2(u0, v0);
    gl_Position = mvp * vec4(bottom_left, 0.0, 1.0);
    EmitVertex();

    // Top left
    out_vertex.uv = vec2(u0, v1);
    gl_Position = mvp * vec4(top_left, 0.0, 1.0);
    EmitVertex();

    // Bottom right
    out_vertex.uv = vec2(u1, v0);
    gl_Position = mvp * vec4(bottom_right, 0.0, 1.0);
    EmitVertex();

    // Top right
    out_vertex.uv = vec2(u1, v1);
    gl_Position = mvp * vec4(top_right, 0.0, 1.0);
    EmitVertex();

    EndPrimitive();
}
