package com.mannotaur.hydrahead.scenes;

import android.content.Context;

public interface Scene {
    public void addShader(Context context);
    public void initialise(int widht, int height);
    public void draw(long globalStartTime);
    public void onTouch(float x);
}
