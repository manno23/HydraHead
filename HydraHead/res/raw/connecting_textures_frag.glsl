precision mediump float;

uniform sampler2D usTextureUnit;
uniform float ufAlpha;

varying mediump vec2 vvTextureCoordinates;

void main()
{
    float r = texture2D(usTextureUnit, vvTextureCoordinates).r;
    float g = texture2D(usTextureUnit, vvTextureCoordinates).g;
    float b = texture2D(usTextureUnit, vvTextureCoordinates).b;
    gl_FragColor = vec4(r, g, b, ufAlpha);
}
