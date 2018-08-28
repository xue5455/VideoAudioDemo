uniform mat4 uTexMatrix;
attribute vec2 aPosition;
attribute vec4 aTextureCoord;
varying vec2 vTextureCoord;
uniform mat4 uMvpMatrix;
void main(){
    gl_Position = uMvpMatrix * vec4(aPosition,0.1,1.0);
    vTextureCoord = (uTexMatrix * aTextureCoord).xy;
}

