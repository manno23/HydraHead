package com.mannotaur.hydrahead.objects;

import android.graphics.Color;
import com.mannotaur.hydrahead.programs.ShaderProgram;

import android.util.Log;
import com.mannotaur.hydrahead.scenes.Connecting.Cycle;

import java.util.ArrayList;


public class Grid {

    private final String TAG = "Grid";

    private final float LINE_WIDTH = 0.01f;  // Define width in terms of -1 <-> 1
    private final int HORIZONTAL_LINES = 3;
    private final int VERTICAL_LINES = 5;

    private int color;
    private float height;
    private float width;

    private ArrayList<GridLine> gridLines;


    public Grid(int rgb) {
        // initialise common color for all lines
        this.color = rgb;
    }

    public void initialise(float width, float height) {
        // height and width in context of perspective matrix.
        this.width = width;
        this.height = height;
        gridLines = new ArrayList<GridLine>();

        /* Initialise the position of the grid lines
        for(float i = 0; i < height; i = i + height/HORIZONTAL_LINES) {
            gridLines.add(new HorizontalGridLine(i));
        }
        */
        for(float i = -1f+2f/VERTICAL_LINES; i < 1f; i += 2f/VERTICAL_LINES) {
            Log.d(TAG, "new line @x: "+i);
            gridLines.add(new VerticalGridLine(height, 0f, i, width));
        }
    }

    public void bindData(ShaderProgram program) {
        for( GridLine gridLine : gridLines )
            gridLine.bindData(program);
    }

    public void draw(long time) {
        for( GridLine gridLine : gridLines )
            gridLine.draw();
    }

    public void handleBallPosition(Point point) {
        // if point moves to within some distance of gridline activate
        //   OR
        // ** handle continuous distance from lines and handle each line seperately
        for( GridLine line : gridLines) {
            line.handleBallPosition(point);
        }
    }

    public void handleBallRelease() {
        for( GridLine gridLine: gridLines) {
            gridLine.line.setLineWidth(0.01f);
        }
    }


    abstract class GridLine {

        protected Line line;

        public GridLine(Line line) {
            this.line = line;
        }

        abstract void bindData(ShaderProgram program);
        abstract void draw();
        abstract void handleBallPosition(Point point);
        abstract void handleBallRelase();

    }

    class HorizontalGridLine extends GridLine {

        private final float vertical_range;

        /**
         * Horizontal lines only take a y position (in the context of the perspective space.)
         */
        public HorizontalGridLine(float left, float right, float y, float vertical_range) {
            super(new Line(new Point(left, y, 0f), new Point(right, y, 0f)));
            this.vertical_range = vertical_range;
        }

        @Override
        void bindData(ShaderProgram program) {

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
        }

        @Override
        void handleBallRelase() {

        }
    }

    class VerticalGridLine extends GridLine {

        private final float horizontal_range;

        /**
         * The vertical lines represent a row of 8 notes,
         * with each consecutive line changing the timbre or volume, whatever you wish.
         */
        public VerticalGridLine(float top, float bottom, float x, float horizontal_range) {
            super(new Line(new Point(x, top, 0f), new Point(x, bottom, 0f)));
            line.setColour(0.7f, 0.6f, 0.9f);
            this.horizontal_range = horizontal_range;
        }

        void bindData(ShaderProgram program) {
            this.line.bindData(program);
        }

        @Override
        void draw() {
            this.line.draw();
        }

        @Override
        void handleBallPosition(Point point) {

            float x_val = Math.abs(point.x - line.point1.x);

            if(x_val < 0.10*horizontal_range && x_val > 0.01*horizontal_range) {     // if we're within 5% of the string, respond linearly

                line.setLineWidth(x_val/10);

                float val = (point.y / height)*0.8f + 0.2f;
                line.setColour(0.4f, val, 1.0f-val);

            } else {
                line.setLineWidth(0.01f);
                line.setColour(0.7f, 0.6f, 0.9f);
            }
        }

        @Override
        void handleBallRelase() {
            line.setLineWidth(0.01f);
            line.setColour(0.7f, 0.6f, 0.9f);
        }

    }
}


