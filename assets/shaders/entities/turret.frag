#version 330

in VertexData {
    vec2 uv;
} in_vertex;

uniform sampler2D in_texture;

out vec4 out_frag_color;

void main(void) {
    out_frag_color = vec4(1.0, 0.0, 0.0, 1.0); //texture(in_texture, v_uv);
}
