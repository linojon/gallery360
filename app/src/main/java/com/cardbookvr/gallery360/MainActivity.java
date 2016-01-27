package com.cardbookvr.gallery360;

import android.content.Context;
import android.database.Cursor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.OrientationEventListener;

import com.cardbook.renderbox.IRenderBox;
import com.cardbook.renderbox.RenderBox;
import com.cardbook.renderbox.Transform;
import com.cardbook.renderbox.components.Camera;
import com.cardbook.renderbox.components.RenderObject;
import com.cardbook.renderbox.components.Sphere;
import com.cardbook.renderbox.materials.UnlitTexMaterial;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends CardboardActivity implements IRenderBox {
    final String TAG = "MainActivity";

    final int GRID_X = 5;
    final int GRID_Y = 3;

    public static boolean cancelUpdate = false;
    static boolean gridUpdateLock = false;

    final int DEFAULT_BACKGROUND = R.drawable.bg;
    final String imagesPath = "/storage/emulated/0/DCIM/Camera";

    final List<Image> images = new ArrayList<>();
    final List<Plane> thumbnails = new ArrayList<>();
    static int thumbOffset = 0;

    CardboardView cardboardView;
    Plane screen;
    Sphere photosphere;
    int bgTextureHandle;

    final float[] selectedColor = new float[]{0, 0.5f, 0.5f, 1};
    final float[] invalidColor = new float[]{0.5f, 0, 0, 1};
    final float[] normalColor = new float[]{0, 0, 0, 1};
    int selectedThumbnail = -1;

    private Vibrator vibrator;

    Triangle up, down;
    BorderMaterial upMaterial, downMaterial;
    boolean upSelected, downSelected;

    OrientationEventListener orientationEventListener;
    long tiltTime;
    int tiltDamper = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        cancelUpdate = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRestoreGLStateEnabled(false);
        cardboardView.setRenderer(new RenderBox(this, this));
        setCardboardView(cardboardView);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        setupOrientationListener();
    }

    @Override
    protected void onStart(){
        super.onStart();
        cancelUpdate = true;
    }
    @Override
    protected void onResume(){
        super.onResume();
        cancelUpdate = false;
    }
    @Override
    protected void onPause(){
        super.onPause();
        cancelUpdate = true;
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        orientationEventListener.disable();
    }

    @Override
    public void setup() {
        BorderMaterial.destroy();
        setupMaxTextureSize();
        setupBackground();
        setupScreen();
        loadImageList(imagesPath);
        setupThumbnailGrid();
        setupScrollButtons();
        Uri intentUri = getIntent().getData();
        if (intentUri != null) {
            showUriImage(intentUri);
        }
        updateThumbnails();

//        showImage(images.get(0));
//        showImage(images.get(images.size()-1));
//        showImage(images.get(3));
    }

    void setupBackground() {
        photosphere = new Sphere(DEFAULT_BACKGROUND, false);
        new Transform()
                .setLocalScale(-Camera.Z_FAR, -Camera.Z_FAR, -Camera.Z_FAR)
                .addComponent(photosphere);
        UnlitTexMaterial mat = (UnlitTexMaterial) photosphere.getMaterial();
        bgTextureHandle = mat.getTexture();
    }

    void setupScreen() {
        Transform screenRoot = new Transform()
                .setLocalScale(4, 4, 1)
                .setLocalPosition(-5, 0, 0)
                .setLocalRotation(0, -90, 0);

        screen = new Plane(R.drawable.sample360, false);
        new Transform()
                .setParent(screenRoot, false)
                .setLocalRotation(0, 0, 180)
                .addComponent(screen);

        BorderMaterial screenMaterial = new BorderMaterial();
        screenMaterial.setTexture(RenderObject.loadTexture(R.drawable.sample360));
        screen.setupBorderMaterial(screenMaterial);
    }

    void setupThumbnailGrid() {
        int count = 0;
        for (int i = 0; i < GRID_Y; i++) {
            for (int j = 0; j < GRID_X; j++) {
                if (count < images.size()) {
                    Transform image = new Transform();
                    image.setLocalPosition(-4 + j * 2, 3 - i * 3, -5);
                    Plane imgPlane = new Plane();
                    thumbnails.add(imgPlane);
                    BorderMaterial material = new BorderMaterial();
                    imgPlane.setupBorderMaterial(material);
                    image.addComponent(imgPlane);
                }
                count++;
            }
        }
    }

    void setupScrollButtons() {
        up = new Triangle();
        upMaterial = new BorderMaterial();
        up.setupBorderMaterial(upMaterial);
        new Transform()
                .setLocalPosition(0, 6, -5)
                .addComponent(up);

        down = new Triangle();
        downMaterial = new BorderMaterial();
        down.setupBorderMaterial(downMaterial);
        new Transform()
                .setLocalPosition(0, -6, -5)
                .setLocalRotation(0, 0, 180)
                .addComponent(down);
    }

    void setupOrientationListener() {
        orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if(System.currentTimeMillis() - tiltTime > tiltDamper) {
                    if (orientation == 0 || orientation == 180) {
                        Log.d(TAG, "tilt up!");
                        toggleGridMenu();
                    }
                    tiltTime = System.currentTimeMillis();
                }
            }
        };
        if(orientationEventListener.canDetectOrientation())
            orientationEventListener.enable();
    }

    void toggleGridMenu() {
        if (up != null)
            up.enabled = !up.enabled;
        if (down != null)
            down.enabled = !down.enabled;
        int texCount = thumbOffset;
        for (Plane plane : thumbnails) {
            if (texCount < images.size()) {
                plane.enabled = !plane.enabled;
            }
            texCount++;
        }
    }

    void updateThumbnails() {
        gridUpdateLock = true;
        new Thread() {
            @Override
            public void run() {

                int count = 0;
                int texCount = thumbOffset;
                for (int i = 0; i < GRID_Y; i++) {
                    for (int j = 0; j < GRID_X; j++) {
                        if (cancelUpdate)
                            return;
                        if (count < thumbnails.size()) {
                            Plane imgPlane = thumbnails.get(count);
                            if (texCount < images.size()) {
                                Image image = images.get(texCount);
                                image.showThumbnail(cardboardView, imgPlane);
                                imgPlane.enabled = true;
                            } else {
                                imgPlane.enabled = false;
                            }
                        }
                        count++;
                        texCount++;
                    }
                }
                cancelUpdate = false;
                gridUpdateLock = false;
            }
        }.start();
    }

    void showUriImage(final Uri uri) {
        Log.d(TAG, "intent data " + uri.getPath());
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String yourRealPath = cursor.getString(columnIndex);
            final Image img = new Image(yourRealPath);
            showImage(img);
        }
        // else report image not found error?
        cursor.close();
    }

    void showImage(final Image image) {
        new Thread() {
            @Override
            public void run() {

                UnlitTexMaterial bgMaterial = (UnlitTexMaterial) photosphere.getMaterial();
                Log.d(TAG, "!!! " + bgMaterial.name);
                image.loadFullTexture(cardboardView);
                if (image.isPhotosphere) {
                    Log.d(TAG, "!!! is photosphere");
                    bgMaterial.setTexture(image.textureHandle);
                    screen.enabled = false;
                } else {
                    bgMaterial.setTexture(bgTextureHandle);
                    screen.enabled = true;
                    image.show(cardboardView, screen);
                }
            }
        }.start();
    }

    @Override
    public void preDraw() {

    }

    @Override
    public void postDraw() {
        selectObject();
    }

    void selectObject() {
        selectedThumbnail = -1;
        int iThumbnail = 0;
        for (Plane plane : thumbnails) {
            BorderMaterial material = (BorderMaterial) plane.getMaterial();
            if (plane.isLooking) {
                selectedThumbnail = iThumbnail;
                material.borderColor = selectedColor;
                if(gridUpdateLock)
                    material.borderColor = invalidColor;
            } else {
                material.borderColor = normalColor;
            }
            iThumbnail++;
        }

        if (up.isLooking) {
            upSelected = true;
            upMaterial.borderColor = selectedColor;
        } else {
            upSelected = false;
            upMaterial.borderColor = normalColor;
        }

        if (down.isLooking) {
            downSelected = true;
            downMaterial.borderColor = selectedColor;
        } else {
            downSelected = false;
            downMaterial.borderColor = normalColor;
        }
    }

    @Override
    public void onCardboardTrigger() {
        String TAG = "onCardoboardTrigger";
        Log.d(TAG, ""+selectedThumbnail);

        if (gridUpdateLock) {
            vibrator.vibrate(new long[]{0,50,30,50}, -1);
            return;
        }

        if (selectedThumbnail > -1) {
            Image image = images.get(selectedThumbnail + thumbOffset);
            showImage(image);
        }
        if (upSelected) {
            // scroll up
            thumbOffset -= GRID_X;
            if (thumbOffset < 0) {
                thumbOffset = images.size() - GRID_X;
            }
            updateThumbnails();
        }
        if (downSelected) {
            // scroll down
            if (thumbOffset < images.size()) {
                thumbOffset += GRID_X;
            } else {
                thumbOffset = 0;
            }
            updateThumbnails();
        }
        vibrator.vibrate(25);
    }

    int loadImageList(String path) {
        File f = new File(path);
        File file[] = f.listFiles();
        Log.d(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Log.d(TAG, "GETTING FILES FROM " + path);
        Log.d(TAG, "Length: "+ file.length);
        if (file==null)
            return 0;
        for(int j = 0; j < 2; j++) { //Artificially duplicate image list
            for (int i = 0; i < file.length; i++) {
                if (Image.isValidImage(file[i].getName())) {
                    Image img = new Image(path + "/" + file[i].getName());
                    images.add(img);
                    Log.d(TAG, file[i].getName());
                }
            }
        }
        return file.length;
    }

    //TODO: put in RenderBox
    static int MAX_TEXTURE_SIZE = 2048;

    void setupMaxTextureSize() {
        //get max texture size
        int[] maxTextureSize = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        MAX_TEXTURE_SIZE = maxTextureSize[0];
        Log.i(TAG, "Max texture size = " + MAX_TEXTURE_SIZE);
    }
}