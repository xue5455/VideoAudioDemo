attribute vec2 aPosition;
attribute vec4 aTextureCoord;
varying vec2 vTextureCoord;
void main(){
    gl_Position = vec4(aPosition,0.0,1.0);
    vTextureCoord = aTextureCoord.xy;
}