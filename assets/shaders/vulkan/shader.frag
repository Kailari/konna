#version 450
#extension GL_ARB_separate_shader_objects : enable

// FIXME: Allow injecting this from the engine
#define MAX_LIGHTS 10

layout(set = 0, binding = 0) uniform CameraInfo {
    mat4 view;
    mat4 projection;
    vec3 eyePosition;
} camera;

struct Light {
    vec3 position;
    float maxRadius;
    vec3 color;
};

layout(set = 1, binding = 0) uniform Lights {
    Light lights[MAX_LIGHTS];
};
layout(set = 1, binding = 1) uniform LightCount {
    int lightCount;
};


layout(set = 2, binding = 0) uniform sampler2D textureSampler;

layout(set = 2, binding = 1) uniform Material {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float reflectance;
    int hasTexture;
} material;

layout(location = 0) in vec3 vPosition;
layout(location = 1) in vec3 vNormal;
layout(location = 2) in vec2 fragTexCoord;

layout(location = 0) out vec4 outColor;

vec3 f_lit(vec3 lightDirection, vec3 normal, vec3 viewVector) {
    vec3 color = material.diffuse.rgb;
    if (material.hasTexture == 1) {
        vec4 textureColor = texture(textureSampler, fragTexCoord);
        color = color * textureColor.rgb;
    }

    return color;
}

vec3 f_unlit(vec3 normal, vec3 viewVector) {
    return vec3(0.0, 0.0, 0.0);
}

float f_distance(float r, float maxRadius) {
    float x = max(0, 1 - pow(r / maxRadius, 4));
    return x * x;
}

void main() {
    vec3 normal = normalize(vNormal);
    vec3 viewVector = normalize(camera.eyePosition - vPosition);

    vec3 unlitContribution = f_unlit(normal, viewVector);
    vec3 totalLightContribution = vec3(0, 0, 0);
    for (int i = 0; i < lightCount; ++i) {
        vec3 lightDelta = lights[i].position - vPosition;
        float lightDistance = length(lightDelta);
        vec3 lightDirection = lightDelta / lightDistance;

        vec3 lightColor = lights[i].color * f_distance(lightDistance, lights[i].maxRadius);

        // Clamp light contributions to zero when facing away from the light source
        float directionDot = clamp(dot(lightDirection, normal), 0.0, 1.0);
        vec3 lightContribution = directionDot * lightColor * f_lit(lightDirection, normal, viewVector);

        totalLightContribution += lightContribution;
    }
    vec3 shadedColor = unlitContribution + totalLightContribution;

    outColor = vec4(shadedColor, 1.0);

    //outColor = vec4(material.diffuse.rgb, 1.0);
}
