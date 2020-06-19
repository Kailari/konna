#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec4 vColor;
layout(location = 1) in vec2 vTextureCoordinates;

layout(set = 0, binding = 0) uniform usampler2D textureSampler;


layout(location = 0) out vec4 outColor;

void main() {
    vec4 sampleColor = texture(textureSampler, vTextureCoordinates);
    outColor = vec4(vColor.rgb, sampleColor.r);
}
