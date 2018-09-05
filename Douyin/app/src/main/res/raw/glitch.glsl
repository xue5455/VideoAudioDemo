#extension GL_OES_EGL_image_external : require
 precision highp float;
 varying vec2 vTextureCoord;
 uniform samplerExternalOES uTexture;
 uniform vec2 uScanLineJitter;//displacement threshold
 uniform float uColorDrift;
 uniform float uGlobalTime;
float nrand(in float x, in float y)
{
     return fract(sin(dot(vec2(x, y), vec2(12.9898, 78.233))) * 43758.5453);
}
void main(){
    float u = vTextureCoord.x;
    float v = vTextureCoord.y;
    float jitter = nrand(u,0.0) * 2.0 - 1.0;
    float drift = uColorDrift;
    float offsetParam = step(uScanLineJitter.y,abs(jitter));
    jitter = jitter * offsetParam * uScanLineJitter.x;
    vec4 color1 = texture2D(uTexture,fract(vec2( u,v + jitter)));
    vec4 color2 = texture2D(uTexture,fract(vec2(u,v+jitter-u*drift)));
    gl_FragColor = vec4(color1.r,color2.g,color1.b,1.0);
}