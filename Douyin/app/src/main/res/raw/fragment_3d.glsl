#extension GL_OES_EGL_image_external : require
 precision mediump float;
 varying vec2 vTextureCoord;
 uniform samplerExternalOES uTexture;
 uniform float uAlpha;
 void main(){
      gl_FragColor = vec4(texture2D(uTexture,vTextureCoord).rgb,uAlpha);
 }