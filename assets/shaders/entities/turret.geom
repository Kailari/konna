#version 150

layout (points) in;
layout (points, max_vertices = 1) out;

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

    out_vertex.uv = vec2(0.0, 0.0);
    gl_Position = mvp * vec4(p0.xy, 0.0, 1.0);
    EmitVertex();
    EndPrimitive();
}
