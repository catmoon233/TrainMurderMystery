#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Time; // 游戏时间（秒）

out vec2 texCoord0;
out vec4 vertexColor;
out vec3 normal;
out float time;

// 自定义扭曲函数
float distort(vec3 pos, float t) {
    return sin(pos.x * 0.5 + t * 2.0) *
    sin(pos.y * 0.3 + t * 1.5) *
    sin(pos.z * 0.4 + t * 2.3) * 0.2;
}

void main() {
    vec3 pos = Position;
    float t = Time * 5.0; // 加速时间

    // 添加顶点扭曲
    pos.x += distort(pos, t) * 0.5;
    pos.y += distort(pos.yxz, t + 2.0) * 0.3;
    pos.z += distort(pos.zyx, t + 4.0) * 0.4;

    // 整体摆动
    float swing = sin(t * 0.5) * 0.1;
    mat4 rotation = mat4(
    cos(swing), 0, -sin(swing), 0,
    0, 1, 0, 0,
    sin(swing), 0, cos(swing), 0,
    0, 0, 0, 1
    );

    gl_Position = ProjMat * ModelViewMat * rotation * vec4(pos, 1.0);

    texCoord0 = UV0;
    vertexColor = Color;
    normal = Normal;
    time = t;
}