#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inColor;

layout(location = 0) out vec3 vColor;

layout(set = 0, binding = 0) uniform CameraInfo {
    mat4 view;
    mat4 projection;
    vec3 eyePosition;
} camera;

layout(push_constant) uniform InstanceInfo {
    mat4 model;
} instance;

void main() {
    mat4 mvp = camera.projection * camera.view * instance.model;
    gl_Position = mvp * vec4(inPosition, 1.0);
    vColor = inColor;
}
