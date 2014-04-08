uniform mat4 umMVP;
attribute vec4 avPosition;
attribute vec4 avColour;
varying vec4 vvColour;

void main()
{
    vvColour = avColour;
    gl_Position = umMVP * avPosition;
}