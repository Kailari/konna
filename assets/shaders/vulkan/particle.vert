#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) out vec4 vColor;

layout(set = 0, binding = 0) uniform CameraInfo {
    mat4 view;
    mat4 projection;
    vec3 eyePosition;
} camera;

layout(push_constant) uniform InstanceInfo {
    vec3 position;
} instance;

uint hash(uint x) {
    x += (x << 10u);
    x ^= (x >>  6u);
    x += (x <<  3u);
    x ^= (x >> 11u);
    x += (x << 15u);
    return x;
}

float floatConstruct(uint m) {
    const uint ieeeMantissa = 0x007FFFFFu;
    const uint ieeeOne      = 0x3F800000u;

    m &= ieeeMantissa;
    m |= ieeeOne;

    float  f = uintBitsToFloat(m);
    return f - 1.0;
}

float random(float x) {
    return floatConstruct(hash(floatBitsToUint(x)));
}

void main() {
    float x = instance.position.x + random(gl_VertexIndex) * 100 - 50;
    float y = instance.position.y + random(gl_VertexIndex + 1234) * 100 - 50;
    float z = instance.position.z + random(gl_VertexIndex + 4321) * 100 - 50;

    gl_Position = camera.projection * camera.view * vec4(x, y, z, 1.0);

    gl_PointSize = 2.0;
    vColor = vec4(1.0, 0.0, 0.0, 1.0);
}
