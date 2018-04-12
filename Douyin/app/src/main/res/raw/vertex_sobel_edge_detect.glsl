uniform mat4 uTexMatrix;
attribute vec2 aPosition;
attribute vec4 aTextureCoordinate;
uniform highp float texelWidth;
uniform highp float texelHeight;
varying vec2 vTextureCoordinate;
varying vec2 vLeftTextureCoordinate;
varying vec2 vRightTextureCoordinate;
varying vec2 vTopTextureCoordinate;
varying vec2 vTopLeftTextureCoordinate;
varying vec2 vTopRightTextureCoordinate;
varying vec2 vBottomTextureCoordinate;
varying vec2 vBottomLeftTextureCoordinate;
varying vec2 vBottomRightTextureCoordinate;

void main(){
    gl_Position = vec4(aPosition,0.0,1.0);
    vTextureCoordinate = (uTexMatrix * aTextureCoordinate).xy;
    vec2 widthStep = vec2(texelWidth, 0.0);
    vec2 heightStep = vec2(0.0, texelHeight);
    vec2 widthHeightStep = vec2(texelWidth, texelHeight);
    vec2 widthNegativeHeightStep = vec2(texelWidth, -texelHeight);
    vLeftTextureCoordinate = vTextureCoordinate - widthStep;
    vRightTextureCoordinate = vTextureCoordinate + widthStep;
    vTopTextureCoordinate = vTextureCoordinate - heightStep;
    vTopLeftTextureCoordinate = vTextureCoordinate - widthHeightStep;
    vTopRightTextureCoordinate = vTextureCoordinate + widthNegativeHeightStep;
    vBottomTextureCoordinate = vTextureCoordinate + heightStep;
    vBottomLeftTextureCoordinate = vTextureCoordinate - widthNegativeHeightStep;
    vBottomRightTextureCoordinate = vTextureCoordinate + widthHeightStep;
}