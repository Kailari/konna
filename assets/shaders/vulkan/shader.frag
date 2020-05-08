#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(set = 1, binding = 0) uniform sampler2D textureSampler;

layout(set = 1, binding = 1) uniform Material {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float reflectance;
    int hasTexture;
} material;

layout(location = 0) in vec3 mvPosition;
layout(location = 1) in vec3 mvNormal;
layout(location = 2) in vec2 fragTexCoord;

layout(location = 0) out vec4 outColor;

void main() {
    vec3 color = material.diffuse.rgb;
    if (material.hasTexture == 1) {
        vec4 textureColor = texture(textureSampler, fragTexCoord);
        color = color * textureColor.rgb;
    }

    float lightness = 1.0; // dot(mvNormal, vec3(0, 1, 0));

    outColor = vec4(color * lightness, 1.0);
}
