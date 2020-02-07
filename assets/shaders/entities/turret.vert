#version 330

in vec2 in_pos;
in float in_alert_status;
in float in_shooting_status;
in float in_base_rotation;
in vec2 in_target_pos;

out Vertex {
    float alert_status;
    float shooting_status;
    float base_rotation;
    vec2 target_pos;
} vertex;

void main(void) {
    gl_Position = vec4(in_pos.xy, 0.0, 1.0);

    vertex.alert_status = in_alert_status;
    vertex.shooting_status = in_shooting_status;
    vertex.base_rotation = in_base_rotation;
    vertex.target_pos = in_target_pos;
}
