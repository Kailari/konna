#version 330

in vec2 in_pos;
in float in_progress;
in float in_maxProgress;

out Vertex {
    float progressPercentage;
    float progress;
    float maxProgress;
} vertex;

void main(void) {
    gl_Position = vec4(in_pos.xy, 0.0, 1.0);

    vertex.progressPercentage = in_progress / in_maxProgress;
    vertex.progress = in_progress;
    vertex.maxProgress = in_maxProgress;
}
