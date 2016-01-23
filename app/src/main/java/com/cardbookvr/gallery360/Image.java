package com.cardbookvr.gallery360;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.cardbook.renderbox.RenderBox;
import com.cardbook.renderbox.math.Quaternion;
import com.google.vrtoolkit.cardboard.CardboardView;

import java.io.IOException;

/**
 * Created by Jonathan on 1/16/2016.
 */
public class Image {
    final static String TAG = "image";
    public static boolean loadLock = false;
    String path;
    int textureHandle;
    Quaternion rotation;
    int height, width;

    static int MAX_TEXTURE_SIZE = 2048;

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

    public void loadFullTexture(CardboardView cardboardView) {
        // search for best size
        int sampleSize = 1;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        do {
            options.inSampleSize = sampleSize;
            BitmapFactory.decodeFile(path, options);
            sampleSize *= 2;
        } while (options.outWidth > MAX_TEXTURE_SIZE || options.outHeight > MAX_TEXTURE_SIZE);
        sampleSize /= 2;
        loadTexture(cardboardView, sampleSize);
        while (loadLock){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadTexture(CardboardView cardboardView, int sampleSize){
//        loadLock = true;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        final Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if(bitmap == null){
            throw new RuntimeException("Error loading bitmap.");
        }
        width = options.outWidth;
        height = options.outHeight;
//        cardboardView.queueEvent(new Runnable() {
//                                     @Override
//                                     public void run() {
//                                         if (MainActivity.cancelUpdate)
//                                             return;
                                         textureHandle = bitmapToTexture(bitmap);
//                                         bitmap.recycle();
//                                         loadLock = false;
//                                     }
//                                 }
//        );
    }

    public static int bitmapToTexture(Bitmap bitmap){
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);
        RenderBox.checkGLError("Bitmap GenTexture");

        if (textureHandle[0] != 0) {
            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        }
        if (textureHandle[0] == 0){
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public void show(CardboardView cardboardView, Plane screen, float scaleFactor) {
        BorderMaterial material = (BorderMaterial) screen.getMaterial();
        loadFullTexture(cardboardView);
        material.setTexture(textureHandle);
        calcRotation();
        if (rotation != null) {
            screen.transform.setLocalRotation(new Quaternion(rotation));
        }
        if (width > 0 && width > height) {
            screen.transform.setLocalScale(scaleFactor, (scaleFactor * height / width), 1);
        } else if(height > 0) {
            screen.transform.setLocalScale((scaleFactor * width / height), scaleFactor, 1);
        }
    }

    void calcRotation(){
        rotation = null;

        // use Exif tags to determine orientation, only available in jpg (and jpeg)
        String ext = getExtension(path);
        if (!ext.equals("jpg") && !ext.equals("jpeg"))
            return;

        try {
            ExifInterface exif = new ExifInterface(path);
            //height = exif.getAttribute(ExifInterface.TAG_I);
            //rotation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            switch (exif.getAttribute(ExifInterface.TAG_ORIENTATION)){
                // Correct orientation, but flipped on the horizontal axis
                case "2":
                    rotation = new Quaternion().setEulerAngles(180,0,0);
                    break;
                // Upside-down
                case "3":
                    rotation = new Quaternion().setEulerAngles(0,0,180);
                    break;
                // Upside-Down & Flipped along horizontal axis
                case "4":
                    rotation = new Quaternion().setEulerAngles(180,0,180);
                    break;
                // Turned 90 deg to the left and flipped
                case "5":
                    rotation = new Quaternion().setEulerAngles(0,180,90);
                    break;
                // Turned 90 deg to the left
                case "6":
                    rotation = new Quaternion().setEulerAngles(0,0,-90);
                    break;
                // Turned 90 deg to the right and flipped
                case "7":
                    rotation = new Quaternion().setEulerAngles(0,180,90);
                    break;
                // Turned 90 deg to the right
                case "8":
                    rotation = new Quaternion().setEulerAngles(0,0,90);
                    break;
                //Correct orientation--do nothing
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
