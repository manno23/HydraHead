precision mediump float;

uniform sampler2D usTextureUnit;
uniform float ufAlpha;

varying mediump vec2 vvTextureCoordinates;

void main()
{
    float a = texture2D(usTextureUnit, vvTextureCoordinates).a;
    gl_FragColor = vec4(ufAlpha, ufAlpha, ufAlpha, a);
}
