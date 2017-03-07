precision mediump float;
uniform vec4 u_Colour;
varying vec4 v_Alpha;

void main()
{
    gl_FragColor = vec4(u_Colour.r, u_Colour.g, u_Colour.b, v_Alpha.a);
}