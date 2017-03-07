uniform float u_time;

attribute vec2 position;
attribute vec4 colour;
attribute float speed;
attribute float start_time;

varying vec4 v_colour;
varying float v_elapsed_time;

void main() {
    v_colour = colour;
    v_elapsed_time = u_time - start_time;
    float current_y = position.y + (speed * v_elapsed_time);

    gl_Position = vec4(position.x, current_y, 0.0, 1.0);

}
