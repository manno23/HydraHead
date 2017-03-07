precision mediump float;
varying vec3 v_Colour;
varying float v_ElapsedTime;

void main() {
    gl_FragColor = vec4(v_Colour / v_ElapsedTime, 1.0);
}