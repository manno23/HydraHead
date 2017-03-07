package com.mannotaur.hydrahead.util;

import static android.opengl.GLES20.*;
import static android.opengl.GLUtils.texImage2D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.nio.ByteBuffer;


public class TextureHelper {

    private static final String TAG = "TextureHelper";

    public static int loadTexture(Context context, int resourceID) {
        final int[] textureObjectIDs = new int[1];
        glGenTextures(1, textureObjectIDs, 0);

        if (textureObjectIDs[0] == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not generate a new OpenGL texture object.");
            }
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        final Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(), resourceID, options);
        if (bitmap == null) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Resource ID " + resourceID + " could not be decoded.");
            }
            glDeleteTextures(1, textureObjectIDs, 0);
            return 0;
        }

        glBindTexture(GL_TEXTURE_2D, textureObjectIDs[0]);
            // all future texture calls will be applied to this texture object

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            // assign texture scaling methods for min/mag

        texImage2D(GL_TEXTURE_2D, 0 , GL_RGBA, bitmap, 0);
            // helper method, determines bitmap type and loads pixel data

        bitmap.recycle();
            // remove memory used for the bitmap now it is stored natively within gl server

        glBindTexture(GL_TEXTURE_2D, 0);
            // unbinds the texture object now it has been configured and loaded

        return textureObjectIDs[0];
    }
}
