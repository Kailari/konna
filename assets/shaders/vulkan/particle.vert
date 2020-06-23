#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 inPosition;

layout(location = 0) out vec4 vColor;

layout(set = 0, binding = 0) uniform CameraInfo {
    mat4 view;
    mat4 projection;
    vec3 eyePosition;
} camera;

layout(push_constant) uniform InstanceInfo {
    vec3 position;
} instance;

void main() {
    gl_Position = camera.projection * camera.view * vec4(instance.position, 1.0);
    gl_PointSize = 2.0;
    vColor = vec4(1.0, 0.0, 0.0, 1.0);
}
