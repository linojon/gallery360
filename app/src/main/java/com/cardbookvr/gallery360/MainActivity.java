package com.cardbookvr.gallery360;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

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

    public static boolean cancelUpdate;

    final int DEFAULT_BACKGROUND = R.drawable.bg;
    final String imagesPath = "/storage/emulated/0/DCIM/Camera";

    final List<Image> images = new ArrayList<>();
    final List<Plane> thumbnails = new ArrayList<>();
    static int thumbnailsStartOffset = 0;

    CardboardView cardboardView;
    Plane screen;
    Sphere photosphere;
    int bgTextureHandle;

    final float[] selectedColor = new float[]{0, 0.5f, 0.5f, 1};
    final float[] invalidColor = new float[]{0.5f, 0, 0, 1};
    final float[] normalColor = new float[]{0, 0, 0, 1};
    int selectedThumbnail = -1;

    private Vibrator vibrator;

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
    public void setup() {
        setupBackground();
        setupScreen();
        loadImageList(imagesPath);
        setupThumbnailGrid();
        updateThumbnails();
//        showImage(images.get(0));
//        showImage(images.get(images.size()-1));
        showImage(images.get(3));
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

    void updateThumbnails() {
        int count = 0;
        for (int i = 0; i < GRID_Y; i++) {
            for (int j = 0; j < GRID_X; j++) {
                if(count < thumbnails.size() && count < images.size()) {
                    Plane imgPlane = thumbnails.get(count);
                    Image image = images.get(count);
                    image.showThumbnail(cardboardView, imgPlane);
                }
                count++;
            }
        }
    }

    void showImage(Image image) {
        UnlitTexMaterial bgMaterial = (UnlitTexMaterial) photosphere.getMaterial();
        Log.d(TAG, "!!! "+bgMaterial.name);
//        image.clear();
        image.loadFullTexture(cardboardView);
        if (image.isPhotosphere()) {
            Log.d(TAG,"!!! is photosphere");
            bgMaterial.setTexture(image.textureHandle);
            screen.enabled = false;
        } else {
            bgMaterial.setTexture(bgTextureHandle);
            screen.enabled = true;
            image.show(cardboardView, screen);
        }
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
//                if(gridUpdateLock)
//                    material.borderColor = invalidColor;
            } else {
                material.borderColor = normalColor;
            }
            iThumbnail++;
        }
    }

    @Override
    public void onCardboardTrigger() {
        String TAG = "onCardoboardTrigger";
        Log.d(TAG, ""+selectedThumbnail);
        if (selectedThumbnail > -1) {
            showImage(images.get(selectedThumbnail));
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
        for(int j = 0; j < 5; j++) { //Artificially duplicate image list
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
}