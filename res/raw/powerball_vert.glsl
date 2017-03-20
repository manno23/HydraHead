uniform mat4 u_MVPMatrix;
attribute vec4 a_Position;
attribute vec3 a_Color;

varying vec3 vvColor;

void main()
{
    vvColor = a_Color;
	gl_Position = u_MVPMatrix * a_Position;
}