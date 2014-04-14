uniform mat4 umMVP;
attribute vec2 avPosition;
attribute vec4 avColours;
varying vec4 vvColours;

void main()
{
    vvColours = avColours;
    gl_Position = umMVP * vec4(avPosition, 0.0, 1.0);
}
