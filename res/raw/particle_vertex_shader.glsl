uniform mat4 u_Matrix;
uniform float u_Time;

attribute vec3 a_Position;
attribute vec3 a_Colour;
attribute vec3 a_DirectionVector;
attribute float a_ParticleStartTime;

varying vec3 v_Colour;
varying float v_ElapsedTime;

void main() {
    v_Colour = a_Colour;
    v_ElapsedTime = u_Time - a_ParticleStartTime;
    vec3 currentPosition = a_Position + (a_DirectionVector * v_ElapsedTime);

    float gravityFactor = v_ElapsedTime * v_ElapsedTime / 8.0;
    currentPosition.y -= gravityFactor;

    gl_Position = u_Matrix * vec4(currentPosition, 1.0);
    gl_PointSize = 5.0;
}