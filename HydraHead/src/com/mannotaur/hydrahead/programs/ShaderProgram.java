package com.mannotaur.hydrahead.programs;


import android.content.Context;
import com.mannotaur.hydrahead.util.ShaderHelper;
import com.mannotaur.hydrahead.util.TextResourceLoader;

import java.util.HashMap;
import java.util.Map;

import static android.opengl.GLES20.*;

public class ShaderProgram {

    private final int program;
    private Map<String, Integer> attributeLocations;
    private Map<String, Integer> uniformLocations;

    public ShaderProgram(Context context, int vertexShaderResourceID,
                         int fragmentShaderResourceID) {
        program = ShaderHelper.buildProgram(
                TextResourceLoader.readTextFile(
                        context,
                        vertexShaderResourceID),
                TextResourceLoader.readTextFile(
                        context,
                        fragmentShaderResourceID) );

        attributeLocations = new HashMap<String, Integer>();
        uniformLocations = new HashMap<String, Integer>();


    }

    public void use() {
        glUseProgram(program);
    }

    public ShaderProgram addAttribute(String attribute) {
        attributeLocations.put(attribute, glGetAttribLocation(program, attribute));
        return this;
    }
    public void setUniform(String uniform, float[] values) {
        glUniformMatrix4fv(
                glGetUniformLocation(program, uniform),
                1,
                false,
                values,
                0
        );
    }
    public void setUniform(String uniform, float value) {
        glUniform1f(
                glGetUniformLocation(program, uniform),
                value
        );
    }

    public int getAttributeLocation(String attribute) {
        return attributeLocations.get(attribute);
    }
    public int getUniformLocation(String uniform) {
        return uniformLocations.get(uniform);
    }

}
