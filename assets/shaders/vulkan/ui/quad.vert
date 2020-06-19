#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec2 inPosition;

layout(location = 0) out vec4 vColor;

layout(push_constant) uniform InstanceInfo {
    mat4 model;
    vec4 color;
} instance;

void main() {
    gl_Position = instance.model * vec4(inPosition, 0.0, 1.0);
    vColor = instance.color;
}
