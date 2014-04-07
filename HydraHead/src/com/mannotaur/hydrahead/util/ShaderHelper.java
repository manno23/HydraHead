package com.mannotaur.hydrahead.util;
import android.util.Log;

import static android.opengl.GLES20.*;


public class ShaderHelper {

    private static final String TAG = "ShaderHelper";

    public static int compileVertexShader(String shaderSource) {
        return compileShader(GL_VERTEX_SHADER, shaderSource);
    }

    public static int compileFragmentShader(String shaderSource) {
        return compileShader(GL_FRAGMENT_SHADER, shaderSource);
    }

    private static int compileShader(int type, String shaderSource) {
        final int shaderObjectId = glCreateShader(type);
        if (shaderObjectId == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not create a new shader");
            }
            return 0;
        }

        glShaderSource(shaderObjectId, shaderSource);
        glCompileShader(shaderObjectId);

        // Test if we were succesful
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

        if (LoggerConfig.ON) {
            Log.w(TAG, "Results of compiling the source: " + "\n" + shaderSource + "\n:"
                + glGetShaderInfoLog(shaderObjectId));
        }

        // Handle result of compile status
        if (compileStatus[0] == 0) {
            glDeleteShader(shaderObjectId);
            if (LoggerConfig.ON) {
                Log.w(TAG, "Compilation of the shader failed.");
            }
            return 0;
        }

        return shaderObjectId;

    }

    public static int linkProgram(int vertexShaderID, int fragmentShaderID) {

        final int programObjectID = glCreateProgram();
        if (programObjectID == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not create a new program object.");
            }
            return 0;
        }

        glAttachShader(programObjectID, vertexShaderID);
        glAttachShader(programObjectID, fragmentShaderID);
        glLinkProgram(programObjectID);

        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectID, GL_LINK_STATUS, linkStatus, 0);
        if (LoggerConfig.ON) {
            Log.w(TAG, "Results of linking program:\n" +
                    glGetProgramInfoLog(programObjectID));
        }
        if (linkStatus[0] == 0) {
            glDeleteProgram(programObjectID);
            if (LoggerConfig.ON) {
                Log.w(TAG, "Failed to link the program.");
            }
        }

        return programObjectID;
    }

    public static boolean validateProgram(int programObjectID) {
        glValidateProgram(programObjectID);

        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectID, GL_VALIDATE_STATUS, validateStatus, 0);

        if (LoggerConfig.ON) {
            Log.w(TAG, "Results of validating the program: " + validateStatus[0] +
                    "\nLog:" + glGetProgramInfoLog(programObjectID));
        }

        return validateStatus[0] != 0;
    }
}
