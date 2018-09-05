#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES uTexture;
uniform vec2 uScanLineJitter;//displacement threshold
uniform float uGlobalTime;
uniform vec2 uColorDrift;
float nrand(in float x, in float y)
{
     return fract(sin(dot(vec2(x, y), vec2(12.9898, 78.233))) * 43758.5453);
}

void main(){
    float y = (nrand(vTextureCoord.x,0.0) + 1.0)/2.0;;
    y = fract(y + vTextureCoord.y);
    gl_FragColor = texture2D(uTexture,vec2(vTextureCoord.x,y));
}