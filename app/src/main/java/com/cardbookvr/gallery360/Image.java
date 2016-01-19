package com.cardbookvr.gallery360;

import android.util.Log;

/**
 * Created by Jonathan on 1/16/2016.
 */
public class Image {
    final static String TAG = "image";
    String path;

    public Image(String path) {
        this.path = path;
    }

    public static boolean isValidImage(String path){
        String extension = getExtension(path);
        Log.d(TAG, extension);
        return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png");
    }

    static String getExtension(String path){
        String[] split = path.split("\\.");
        if(split== null || split.length < 2)
            return null;
        return split[split.length - 1].toLowerCase();
    }
}
