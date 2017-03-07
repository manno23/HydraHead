precision mediump float;

uniform float time;
uniform vec4 dists;
uniform vec4 colours;
varying vec2 surfacePosition;

float rand(vec2 n) {
	return fract(sin(n.x*2732.7357+n.y*2542.3643)*4365.6247);
}

void main( void ) {

	vec2 p = surfacePosition;
	float len = length(p) * 2.0;
	float circles = 10.0;
	float spot = floor(len*circles)/circles;
    float base = rand(vec2(spot));
    float color = mod(base * time * 0.5, 2.0);
    if (color>1.) color = 2.0 - color;

    float timePerc = mod(time*0.1, 2.0);
    if (timePerc>1.) timePerc = 2.0 - timePerc;
    vec3 lefta = vec3(1.0, 0.5, 1.0);
    vec3 leftb = vec3(0.0, 0.5, 1.0);
    vec3 left = mix(lefta, leftb, timePerc);

    float timePerc2 = mod(time*0.5, 2.0);
    if (timePerc2>1.) timePerc2 = 2.0 - timePerc2;
    vec3 righta = vec3(0.0, 0.0, 1.0);
    vec3 rightb = vec3(1.0, 1.0, 0.0);
    vec3 right = mix(righta, rightb, timePerc2);
    vec3 squareColor = color*mix(left, right, p.x);
    float vignette = smoothstep(0.75, 0.5, len);

    float d0 = floor(dists[0]*circles)/circles;
    float d1 = floor(dists[1]*circles)/circles;
    float d2 = floor(dists[2]*circles)/circles;
    float d3 = floor(dists[3]*circles)/circles;

    float y1 = -20. * pow((spot - d0), 2.) + 0.2;
    if (y1 < 0.) y1 = 0.0;
    float y2 = -20. * pow((spot - d1), 2.) + 0.2;
    if (y2 < 0.) y2 = 0.0;
    float y3 = -20. * pow((spot - d2), 2.) + 0.2;
    if (y3 < 0.) y3 = 0.0;
    float y4 = -20. * pow((spot - d3), 2.) + 0.2;
    if (y4 < 0.) y4 = 0.0;
    vec3 o = mix(squareColor, squareColor * vignette, .8);
    gl_FragColor = vec4(o.r+y1*0.1+y2*0.1+y3*0.1+y4*0.1, o.g+y1+y2+y3+y4, o.b+y1*0.3+y2*0.3+y3*0.3+y4*0.3, 1.0);

}