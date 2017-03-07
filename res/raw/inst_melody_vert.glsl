attribute vec2 position;
attribute vec4 colour;
varying vec4 v_colour;

void main() {
    v_colour = colour;
    gl_Position = vec4(position, 0.0, 1.0);
}
