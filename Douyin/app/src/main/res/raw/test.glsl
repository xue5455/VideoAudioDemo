#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES uTexture;
uniform float uAdditionalColor;
void main(){
    vec4 color = texture2D(uTexture,vTextureCoord);
    gl_FragColor = vec4(color.r + uAdditionalColor,color.g + uAdditionalColor,color.b + uAdditionalColor,color.a);
}