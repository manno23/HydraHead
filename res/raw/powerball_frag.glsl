precision mediump float;
varying vec3 vvColor;

void main()
{
	gl_FragColor = vec4(vvColor, 1.0);
}
