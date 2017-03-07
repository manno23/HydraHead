package com.mannotaur.hydrahead.scenes.InstrumentKeys;

import android.graphics.Color;
import com.mannotaur.hydrahead.data.VertexArray;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import java.util.Random;

import static android.opengl.GLES20.*;
import static com.mannotaur.hydrahead.Constants.BYTES_PER_FLOAT;

/**
 * User: Jason Manning
 * Date: 11/06/14
 */
public class Starfall {

    private class StarSystem {

        private static final int POSITION_COMPONENT_COUNT = 2;
        private static final int COLOUR_COMPONENT_COUNT = 4;
        private static final int SPEED_COMPONENT_COUNT = 1;
        private static final int PARTICLE_START_TIME_COMPONENT_COUNT = 1;
        private static final int TOTAL_COMPONENT_COUNT =
                POSITION_COMPONENT_COUNT
                    + COLOUR_COMPONENT_COUNT
                    + SPEED_COMPONENT_COUNT
                    + PARTICLE_START_TIME_COMPONENT_COUNT;
        private static final int STAR_COMPONENT_COUNT = TOTAL_COMPONENT_COUNT * 6;
        private static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;

        private final float[] stars;
        private final VertexArray vertexArray;
        private final int maxStarCount;
        private int screenHeight;

        public StarSystem(int maxStarCount) {
            this.maxStarCount = maxStarCount;
            stars = new float[maxStarCount * STAR_COMPONENT_COUNT];
            vertexArray = new VertexArray(stars);
        }

        public void initialise(int width, int height) {
            screenHeight = height;
        }

        private int currentStarCount;
        private int nextStar;
        public void addStar(float x_position, int colour, float speed,
                            float width, float length, float starStartTime) {

            final int positionOffset = nextStar * STAR_COMPONENT_COUNT;
            int currentOffset = positionOffset;
            nextStar++;
            if (currentStarCount < maxStarCount) {
                currentStarCount++;
            }
            if (nextStar == maxStarCount) {
                nextStar = 0;
            }

            float l = x_position - width/2.0f;
            float r = x_position + width/2.0f;
            float t = 1.5f;
            float b = 0.5f;

            stars[currentOffset++] = l;
            stars[currentOffset++] = t;
            stars[currentOffset++] = Color.red(colour) / 255f;
            stars[currentOffset++] = Color.green(colour) / 255f;
            stars[currentOffset++] = Color.blue(colour) / 255f;
            stars[currentOffset++] = 0.0f;
            stars[currentOffset++] = speed;
            stars[currentOffset++] = starStartTime;

            stars[currentOffset++] = l;
            stars[currentOffset++] = b;
            stars[currentOffset++] = Color.red(colour) / 255f;
            stars[currentOffset++] = Color.green(colour) / 255f;
            stars[currentOffset++] = Color.blue(colour) / 255f;
            stars[currentOffset++] = 1.0f;
            stars[currentOffset++] = speed;
            stars[currentOffset++] = starStartTime;

            stars[currentOffset++] = r;
            stars[currentOffset++] = t;
            stars[currentOffset++] = Color.red(colour) / 255f;
            stars[currentOffset++] = Color.green(colour) / 255f;
            stars[currentOffset++] = Color.blue(colour) / 255f;
            stars[currentOffset++] = 0.0f;
            stars[currentOffset++] = speed;
            stars[currentOffset++] = starStartTime;

            stars[currentOffset++] = l;
            stars[currentOffset++] = b;
            stars[currentOffset++] = Color.red(colour) / 255f;
            stars[currentOffset++] = Color.green(colour) / 255f;
            stars[currentOffset++] = Color.blue(colour) / 255f;
            stars[currentOffset++] = 1.0f;
            stars[currentOffset++] = speed;
            stars[currentOffset++] = starStartTime;

            stars[currentOffset++] = r;
            stars[currentOffset++] = b;
            stars[currentOffset++] = Color.red(colour) / 255f;
            stars[currentOffset++] = Color.green(colour) / 255f;
            stars[currentOffset++] = Color.blue(colour) / 255f;
            stars[currentOffset++] = 1.0f;
            stars[currentOffset++] = speed;
            stars[currentOffset++] = starStartTime;

            stars[currentOffset++] = r;
            stars[currentOffset++] = t;
            stars[currentOffset++] = Color.red(colour) / 255f;
            stars[currentOffset++] = Color.green(colour) / 255f;
            stars[currentOffset++] = Color.blue(colour) / 255f;
            stars[currentOffset++] = 0.0f;
            stars[currentOffset++] = speed;
            stars[currentOffset++] = starStartTime;

            vertexArray.updateBuffer(stars, positionOffset, STAR_COMPONENT_COUNT);
        }

        public void bindData(ShaderProgram program) {
            int dataOffset = 0;
            vertexArray.setVertexAttribPointer(dataOffset,
                    program.getAttributeLocation("position"),
                    POSITION_COMPONENT_COUNT, STRIDE);
            dataOffset += POSITION_COMPONENT_COUNT;
            vertexArray.setVertexAttribPointer(dataOffset,
                    program.getAttributeLocation("colour"),
                    COLOUR_COMPONENT_COUNT, STRIDE);
            dataOffset += COLOUR_COMPONENT_COUNT;
            vertexArray.setVertexAttribPointer(dataOffset,
                    program.getAttributeLocation("speed"),
                    SPEED_COMPONENT_COUNT, STRIDE);
            dataOffset += SPEED_COMPONENT_COUNT;
            vertexArray.setVertexAttribPointer(dataOffset,
                    program.getAttributeLocation("start_time"),
                    PARTICLE_START_TIME_COMPONENT_COUNT, STRIDE);
        }

        public void draw() {
            glDrawArrays(GL_TRIANGLES, 0, currentStarCount * 6);
        }

    }

    private final StarSystem starSystem;
    private final float speedVariance;
    private final float lengthVariance;
    private final float widthVariance;

    private final Random random = new Random();

    public Starfall() {
        starSystem = new StarSystem(100);
        speedVariance = 2.0f;
        lengthVariance = 0.2f;
        widthVariance = 0.07f;

    }

    public void addStars(float currentTime, float starProbability) {

        if (random.nextFloat() < starProbability) {
            float x_position = -1f + random.nextFloat() * 2f;
            int colour = random.nextInt();
            float speed = -0.1f - random.nextFloat() * 2f;
            float length = 0.5f + random.nextFloat() * 3f;
            float width = 0.01f + random.nextFloat() * widthVariance;

            starSystem.addStar(x_position, colour, speed, width, length, currentTime);
        }
    }

    public void bindData(ShaderProgram shaderProgram) {
        starSystem.bindData(shaderProgram);
    }

    public void draw() {
        starSystem.draw();
    }

}

