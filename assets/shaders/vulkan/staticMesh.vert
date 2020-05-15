#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec2 inTexCoord;

layout(location = 0) out vec3 vPosition;
layout(location = 1) out vec3 vNormal;
layout(location = 2) out vec2 fragTexCoord;

layout(set = 0, binding = 0) uniform CameraInfo {
    mat4 view;
    mat4 projection;
    vec3 eyePosition;
} camera;

layout(push_constant) uniform InstanceInfo {
    mat4 model;
} instance;

void main() {
    // Calculate non-projected position
    vec4 worldPos = instance.model * vec4(inPosition, 1.0);

    // Set vertex position to the projected position
    gl_Position = camera.projection * camera.view * worldPos;

    // Set other fragment shader inputs
    fragTexCoord = inTexCoord;
    vPosition = worldPos.xyz;

    // Apply rotations only to normals. This is done by setting `w` to zero before multiplying
    // (homogeneous coordinates)
    vNormal = (instance.model * vec4(inNormal, 0.0)).xyz;
}
