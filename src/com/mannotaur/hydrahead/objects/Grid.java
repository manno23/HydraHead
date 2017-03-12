package com.mannotaur.hydrahead.objects;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static android.opengl.GLES20.glLineWidth;
import static com.mannotaur.hydrahead.Constants.BYTES_PER_FLOAT;


public class Grid {

    private final float LINE_WIDTH = 0.01f;  // Define width in terms of -1 <-> 1
    private final int HORIZONTAL_LINES = 4;
    private final int VERTICAL_LINES = 8;

    private int color;
    private float height;
    private float width;

    private ArrayList<GridLine> gridLines;

    public Grid(int rgb) {
        // initialise common color for all lines
        this.color = rgb;
    }


    public void initialise(float width, float height) {
        this.width = width;
        this.height = height;
        gridLines = new ArrayList<GridLine>();

        // Initialise the position of the grid lines
        for(float i = 0; i < width; i = i + width/HORIZONTAL_LINES) {
            gridLines.add(new HorizontalGridLine(i));
        }
        for(float i = 0; i < height; i = i + height/VERTICAL_LINES) {
            gridLines.add(new VerticalGridLine(i));
        }
    }

    public void draw(long time) {
        glLineWidth(LINE_WIDTH);
        for( GridLine line : gridLines )
            line.draw();
    }

    public void handleBallPosition(Point point) {
        // if point moves to within some distance of gridline activate
        //   OR
        // ** handle continuous distance from lines and handle each line seperately
        for( GridLine line : gridLines) {
            line.handleBallPosition(point);
        }
    }


    abstract class GridLine {

        private FloatBuffer vertexBuffer;
        private static final int POSITION_COMPONENT_COUNT = 2;
        private static final int COLOUR_COMPONENT_COUNT = 4;
        private static final int STRIDE =
                (POSITION_COMPONENT_COUNT + COLOUR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

        public GridLine() {
        }

        abstract void draw();

        abstract void handleBallPosition(Point point);

    }

    class HorizontalGridLine extends GridLine {

        private final float y;

        public HorizontalGridLine(float y) {
            this.y = y;
        }

        @Override
        void draw() {

        }

        /**
         * As the powerball moves closer to the line make it more noticeable.
         * What gradient should it effect the intensity? of the line?
         * Change thickness? colour? pulse rate?
         * We'll change just the thickness for now.
         * Why am I thinking of this now when i havent run code for the past 4 hours...
         */
        @Override
        void handleBallPosition(Point point) {
            float val = Math.abs(point.y - y);
        }
    }

    class VerticalGridLine extends GridLine {
        /**
         * The vertical lines represent a row of 8 notes,
         * with each consecutive line changing the timbre or volume, whatever you wish.
         */

        private final float x;

        public VerticalGridLine(float x) {
            this.x = x;
        }

        @Override
        void draw() {

        }

        @Override
        void handleBallPosition(Point point) {

        }
    }
}


