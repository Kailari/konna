#version 330

in vec2 frag_uv;

uniform sampler2D in_texture;

out vec4 out_frag_color;

void main(void) {
    out_frag_color = texture(in_texture, frag_uv);
}
