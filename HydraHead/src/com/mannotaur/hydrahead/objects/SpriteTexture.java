package com.mannotaur.hydrahead.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.mannotaur.hydrahead.util.LoggerConfig;

import java.nio.ByteBuffer;

import static android.opengl.GLES20.*;
import static android.opengl.GLUtils.texImage2D;


/**
 * User: Jason Manning
 * Date: 18/06/14
 */
public class SpriteTexture {
 private final static String TAG = "SpriteTexture";

    private int textureObjectID;
    private float[] verticeArray;

    private float alpha;


    public SpriteTexture(Context context, int resourceID, float scale, float posX, float posY, int textureObjectID) throws Exception {

        this.textureObjectID = textureObjectID;
        this.alpha = 1f;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        final Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(), resourceID, options);
        if (bitmap == null) {
            Log.w(TAG, "Resource ID " + resourceID + " could not be decoded.");
            throw new Exception();
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float w = 0.5f * scale;
        float h = (float) height / (float) width / 2f * scale;

        verticeArray = new float[]
                {
                        -w + posX,  h + posY, 0.0f, 0.0f,
                        -w + posX, -h + posY, 0.0f, 1.0f,
                         w + posX,  h + posY, 1.0f, 0.0f,
                        -w + posX, -h + posY, 0.0f, 1.0f,
                         w + posX, -h + posY, 1.0f, 1.0f,
                         w + posX,  h + posY, 1.0f, 0.0f
                };

        glBindTexture(GL_TEXTURE_2D, textureObjectID);
        // all future texture calls will be applied to this texture object

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        // assign texture scaling methods for min/mag

        texImage2D(GL_TEXTURE_2D, 0 , GL_RGBA, bitmap, 0);
        // helper method, determines bitmap type and loads pixel data


        bitmap.recycle();
        // remove memory used for the bitmap now it is stored natively within gl server

        glBindTexture(GL_TEXTURE_2D, 0);

    }

    public float[] getVertices() {
       return verticeArray;
    }

    public int getTextureObjectID() {
        return textureObjectID;
    }

    public void setAlpha(float value) {
        alpha = value;
    }

    public float getAlpha() {
        return alpha;
    }

}

