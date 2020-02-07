#version 150

layout (points) in;
layout (triangle_strip, max_vertices = 8) out;

layout (std140) uniform CameraInfo
{
    mat4 view;
    mat4 projection;
} camera_info;

uniform int time;
uniform int timestep;

in Vertex {
    float alert_status;
    float shooting_status;
    float base_rotation;
    vec2 target_pos;
} in_vertices[];

out VertexData {
    vec2 uv;
} out_vertex;

vec2 transform_coord(vec2 origin, vec2 coord, float rotation) {
    vec2 tmp = coord - origin;
    return vec2(tmp.x * cos(rotation) - tmp.y * sin(rotation), tmp.x * sin(rotation) + tmp.y * cos(rotation)) + origin;
}

void create_quad
(
mat4 mvp,
vec2 size,
vec2 position,
float rotation,
float u0,
float v0,
float u1,
float v1
) {
    vec2 origin = size * 0.5;

    vec2 bottom_left = position.xy + transform_coord(origin, vec2(0, 0), rotation);
    vec2 top_left = position.xy + transform_coord(origin, vec2(0, size.y), rotation);
    vec2 bottom_right = position.xy + transform_coord(origin, vec2(size.x, 0), rotation);
    vec2 top_right = position.xy + transform_coord(origin, size, rotation);

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

void create_quad_from_frame(
mat4 mvp,
vec2 size,
vec2 position,
float rotation,
int columns,
int rows,
int frame
) {
    float frame_width = 1.0 / columns;
    float frame_height = 1.0 / rows;

    int column = frame % columns;
    int row = frame / columns;

    float u0 = column * frame_width;
    float u1 = (column + 1) * frame_width;
    float v0 = row * frame_height;
    float v1 = (row + 1) * frame_height;
    create_quad(mvp, size, position, rotation, u0, v0, u1, v1);
}

void create_base(mat4 mvp, vec2 size, vec2 position, float rotation, bool alert) {
    int columns = 3;
    int rows = 2;

    int frames_idle[2];
    frames_idle[0] = 0;
    int frames_alert[2];
    frames_alert[0] = 1;
    frames_alert[1] = 2;

    float frameDuration = 0.25;
    float t = time / float(timestep);

    int frames[2];
    int animation_length;
    if (alert) {
        animation_length = 2;
        frames = frames_alert;
    } else {
        animation_length = 1;
        frames = frames_idle;
    }

    int frame_index = int(floor(t / frameDuration)) % animation_length;
    int frame = frames[frame_index];

    create_quad_from_frame(mvp, size, position, rotation, columns, rows, frame);
}

void create_gun(mat4 mvp, vec2 size, vec2 position, bool shooting, vec2 target_pos) {
    int columns = 3;
    int rows = 2;

    int frames_idle[2];
    frames_idle[0] = 3;
    int frames_shoot[2];
    frames_shoot[0] = 4;
    frames_shoot[1] = 5;

    float frameDuration = 0.25;
    float t = time / float(timestep);

    int frames[2];
    int animation_length;
    if (shooting) {
        animation_length = 2;
        frames = frames_shoot;
    } else {
        animation_length = 1;
        frames = frames_idle;
    }

    int frame_index = int(floor(t / frameDuration)) % animation_length;
    int frame = frames[frame_index];

    float angle = radians((time / 2) % 360);//acos(dot(normalize(position), normalize(target_pos)));

    create_quad_from_frame(mvp, size, position, angle, columns, rows, frame);
}

void main(void) {
    mat4 mvp = camera_info.projection * camera_info.view;
    vec4 position = gl_in[0].gl_Position;

    vec2 base_size = vec2(2.0, 2.0);
    vec2 gun_size = vec2(2.0, 2.0);

    bool alert = in_vertices[0].alert_status > 0.5;
    bool shooting = in_vertices[0].shooting_status > 0.5;
    vec2 target_pos = in_vertices[0].target_pos;

    create_base(mvp, base_size, position.xy, 0, alert);
    create_gun(mvp, gun_size, position.xy, shooting, target_pos);
}
