#version 450
#extension GL_ARB_separate_shader_objects : enable

#define MAX_BONES 150
#define MAX_WEIGHTS 4

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec2 inTexCoord;
layout(location = 3) in vec4 inBoneWeights;
layout(location = 4) in uvec4 inBoneIds;

layout(location = 0) out vec3 vPosition;
layout(location = 1) out vec3 vNormal;
layout(location = 2) out vec2 fragTexCoord;

layout(set = 0, binding = 0) uniform CameraInfo {
    mat4 view;
    mat4 projection;
    vec3 eyePosition;
} camera;

layout(set = 0, binding = 1) uniform InstanceInfo {
    mat4 model;
} instance;

layout(set = 3, binding = 0) uniform Bones {
    mat4 transforms[MAX_BONES];
} bones;

void main() {
    vec4 position = vec4(0.0, 0.0, 0.0, 0.0);
    vec4 normal = vec4(0.0, 0.0, 0.0, 0.0);
    int count = 0;
    for (int i = 0; i < MAX_WEIGHTS; ++i) {
        float weight = inBoneWeights[i];
        if (weight > 0.0) {
            ++count;
            uint boneIndex = inBoneIds[i];
            vec4 tmpPos = bones.transforms[boneIndex] * vec4(inPosition, 1.0);
            position += weight * tmpPos;
            vec4 tmpNormal = bones.transforms[boneIndex] * vec4(inNormal, 0.0);
            normal += weight * tmpNormal;
        }
    }

    if (count == 0) {
        position = vec4(inPosition, 1.0);
        normal = vec4(inNormal, 0.0);
    }

    // Calculate non-projected position
    vec4 worldPos = instance.model * position;

    // Set vertex position to the projected position
    gl_Position = camera.projection * camera.view * worldPos;

    // Set other fragment shader inputs
    fragTexCoord = inTexCoord;
    vPosition = worldPos.xyz;

    // Apply rotations only to normals. This is done by setting `w` to zero before multiplying
    // (homogeneous coordinates)
    vNormal = (instance.model * normal).xyz;
}
