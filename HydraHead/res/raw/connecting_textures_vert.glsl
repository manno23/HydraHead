uniform mat4 umMVP;

attribute vec2 avPosition;
attribute vec2 avTextureCoordinates;

varying vec2 vvTextureCoordinates;

void main()
{
    vvTextureCoordinates = avTextureCoordinates;
    gl_Position = umMVP * vec4(avPosition, 0.0, 1.0);
}