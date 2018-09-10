package com.xue.douyin.common.preview;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.xue.douyin.common.util.LogUtil;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

/**
 * Created by 薛贤俊 on 2018/3/7.
 */

public class GLUtils {

    public static int buildProgram(String vertexShader, String fragmentShader) {
        int programId;
        int vertexShaderId = compileShader(GL_VERTEX_SHADER, vertexShader);
        int fragmentShaderId = compileShader(GL_FRAGMENT_SHADER, fragmentShader);
        if (vertexShaderId == 0 || fragmentShaderId == 0) {
            return 0;
        }
        programId = glCreateProgram();
        if (programId == 0) {
            return 0;
        }
        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);
        int[] linkStatus = new int[1];
        glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            //链接失败
            LogUtil.d(glGetProgramInfoLog(programId));
            glDeleteProgram(programId);
            return 0;
        }
        if (!validateProgram(programId)) {
            glDeleteProgram(programId);
            return 0;
        }
        return programId;
    }

    private static boolean validateProgram(int programObjId) {
        glValidateProgram(programObjId);
        int validateResult[] = new int[1];
        glGetProgramiv(programObjId, GL_VALIDATE_STATUS, validateResult, 0);
        return validateResult[0] != 0;
    }

    private static int compileShader(int type, String shaderCode) {
        int shaderObjectId = glCreateShader(type);
        if (shaderObjectId == 0) {
            LogUtil.e("『ShaderUtil-compileShader』创建Shader失败");
            return 0;
        }
        glShaderSource(shaderObjectId, shaderCode);
        glCompileShader(shaderObjectId);
        int[] compileResult = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileResult, 0);
        LogUtil.d("Results of compiling source" + "\n:" + glGetShaderInfoLog(shaderObjectId));
        if (compileResult[0] == 0) {
            glDeleteShader(shaderObjectId);
            LogUtil.d("【ShaderUtil-compileShader】shader编译失败");
            return 0;
        }
        return shaderObjectId;
    }

    public static int createTextureObject(int textureTarget) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int texId = textures[0];
        GLES20.glBindTexture(textureTarget, texId);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        return texId;
    }

    public static int genTexture() {
        return genTexture(GLES20.GL_TEXTURE_2D);
    }

    public static int genTexture(int textureType) {
        int[] genBuf = new int[1];
        GLES20.glGenTextures(1, genBuf, 0);
        GLES20.glBindTexture(textureType, genBuf[0]);

        // Set texture default draw parameters
        if (textureType == GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        } else {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
        }

        return genBuf[0];
    }

    public static int genLutTexture() {
        int[] genBuf = new int[1];
        GLES20.glGenTextures(1, genBuf, 0);
        GLES20.glBindTexture(GL_TEXTURE_2D, genBuf[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
        return genBuf[0];
    }

}
