precision mediump float;

uniform float ufAlpha;
uniform sampler2D usTextureUnit;
varying mediump vec2 vvTextureCoordinates;

void main()
{
    gl_FragColor = texture2D(usTextureUnit, vvTextureCoordinates) * vec4(ufAlpha, 0.2, 0.4, ufAlpha);
}
