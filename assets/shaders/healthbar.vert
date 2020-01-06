#version 330

in vec2 in_pos;
in float in_health;
in float in_maxHealth;

out Vertex {
    float healthPercentage;
} vertex;

void main(void) {
    gl_Position = vec4(in_pos.xy, 0.0, 1.0);

    vertex.healthPercentage = in_health / in_maxHealth;
}
