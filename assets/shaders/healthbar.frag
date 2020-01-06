#version 330

in VertexData {
    float healthy;
} in_vertex;

out vec4 out_fragColor;

void main(void) {
    if (in_vertex.healthy > 0.5){
        out_fragColor = vec4(0.0, 1.0, 0.0, 1.0);
    } else {
        out_fragColor = vec4(1.0, 0.0, 0.0, 1.0);
    }
}
