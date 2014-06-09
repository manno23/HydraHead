package com.mannotaur.hydrahead.scenes.Connecting;

/**
 * User: Jason Manning
 * Date: 28/05/14
 */
public class Cycle {

    private double smoothValue;
    private double smoothChangeRate;
    private float upperLimit;
    private float lowerLimit;

    public Cycle(float changeRate, float upperLimit, float lowerLimit, float initialValue) {
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
        this.smoothValue = (Math.PI / 2.0f * initialValue / (upperLimit - lowerLimit));
        this.smoothChangeRate = (Math.PI / 2.0f * changeRate/ (upperLimit - lowerLimit));
    }

    public float value() {
        smoothValue += smoothChangeRate;
        float val = (((float)Math.sin(smoothValue) + 1.0f) / 2.0f * (upperLimit - lowerLimit)) + lowerLimit;
        return val;
    }
}

