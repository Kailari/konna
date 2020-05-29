#version 450
#extension GL_ARB_separate_shader_objects : enable

const uint U0 = 0;
const uint V0 = 1;
const uint U1 = 2;
const uint V1 = 3;

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec2 inTextureCoordinates;
layout(location = 2) in uint inUVIndex;

layout(location = 0) out vec4 vColor;
layout(location = 1) out vec2 vTextureCoordinates;

layout(push_constant) uniform InstanceInfo {
    mat4 model;
    vec4 color;
    vec4 uv;
} instance;

void main() {
    gl_Position = instance.model * vec4(inPosition, 0.0, 1.0);
    vColor = instance.color;

    vec2 actualUVs = vec2(0, 0);
    if (gl_VertexIndex == 0) {
        actualUVs = vec2(instance.uv[U0], instance.uv[V0]);
    } else if (gl_VertexIndex == 1) {
        actualUVs = vec2(instance.uv[U1], instance.uv[V0]);
    } else if (gl_VertexIndex == 2) {
        actualUVs = vec2(instance.uv[U1], instance.uv[V1]);
    } else if (gl_VertexIndex == 3) {
        actualUVs = vec2(instance.uv[U0], instance.uv[V1]);
    } else {
        vColor = vec4(0, 1, 0, 1);
    }
    vTextureCoordinates = actualUVs;
}
