#version 330

in vec2 v_uv;
in vec3 v_tint;

uniform sampler2D in_texture;

out vec4 out_fragColor;

void main(void) {
    vec4 texSample = texture(in_texture, v_uv);
    out_fragColor = vec4(1, 1, 1, texSample.r);
    out_fragColor.rgb = mix(out_fragColor.rgb, v_tint.rgb, 1.0 /* v_tint.a */);
}
