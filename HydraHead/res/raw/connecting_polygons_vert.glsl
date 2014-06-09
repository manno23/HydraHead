attribute vec2 a_Position;
attribute vec4 a_Colour;

uniform mat4 umMVP;
varying vec4 v_Alpha;

void main() {

    v_Alpha = a_Colour;
    gl_Position = umMVP * vec4(a_Position, 0.0, 1.0);
}