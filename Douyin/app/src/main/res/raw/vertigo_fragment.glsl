precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D uTexture;
uniform sampler2D uTexture1;
uniform sampler2D uLookTexture;

void lookup(out vec4 fragColor,in vec4 textureColor){
    float strength = 0.5;
    mediump float blueColor = textureColor.b * 63.0;
    mediump vec2 quad1;
    quad1.y = floor(blueColor/8.0);
    quad1.x = floor(blueColor) - quad1.y*8.0;
    mediump vec2 quad2;
    quad2.y = floor(ceil(blueColor)/7.999);
    quad2.x = ceil(blueColor) - quad2.y * 8.0;
    highp vec2 texPos1;
    texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);
    texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);
    highp vec2 texPos2;
    texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);
    texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);
    lowp vec4 newColor1 = texture2D(uLookTexture, texPos1);
    lowp vec4 newColor2 = texture2D(uLookTexture, texPos2);
    lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));
    fragColor = mix(textureColor, vec4(newColor.rgb, textureColor.w), strength);
}

void main(){
    vec4 origin = texture2D(uTexture,vTextureCoord);
    vec4 last = texture2D(uTexture1,vTextureCoord);
    gl_FragColor = vec4(origin.r * 0.7 + last.r * 0.3,origin.g * 0.7 + last.g * 0.3,origin.b * 0.8 + last.b * 0.2,1.0);
}