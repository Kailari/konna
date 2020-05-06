#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inColor;

layout(location = 0) out vec3 fragColor;

layout(set = 0, binding = 0) uniform CameraInfo {
    mat4 view;
    mat4 projection;
} camera;

layout(set = 0, binding = 1) uniform InstanceInfo {
    mat4 model;
} instance;

void main() {
    gl_Position = camera.projection * camera.view * instance.model * vec4(inPosition, 1.0);
    fragColor = inColor;
}
