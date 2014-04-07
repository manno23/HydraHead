package com.mannotaur.hydrahead.util;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextResourceLoader {

    public static String readTextFile(Context context, int resourceID) {

        StringBuilder body = new StringBuilder();
        try {
            InputStream is = context.getResources().openRawResource(resourceID);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String nextLine;
            while ((nextLine = br.readLine()) != null) {
                body.append(nextLine);
                body.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not open resource: " + resourceID, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException(
                    "Resource not found: " + resourceID, nfe);
        }

        return body.toString();
    }
}
