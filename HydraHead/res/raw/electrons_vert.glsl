attribute vec2 position;
attribute vec2 surfacePosAttrib;
varying vec2 surfacePosition;

void main() {
    surfacePosition = surfacePosAttrib;
    gl_Position = vec4(position, 0.0, 1.0);
}