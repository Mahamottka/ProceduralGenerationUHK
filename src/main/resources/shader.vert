#version 460 core
layout(location = 0) in vec3 inPosition;

uniform mat4 mat;
out vec3 fragPos;

void main() {
    fragPos = inPosition;
    gl_Position = mat * vec4(inPosition, 1.0);
}
