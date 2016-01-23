package com.cardbookvr.gallery360;

import android.os.Bundle;
import android.util.Log;

import com.cardbook.renderbox.IRenderBox;
import com.cardbook.renderbox.RenderBox;
import com.cardbook.renderbox.Transform;
import com.cardbook.renderbox.components.Camera;
import com.cardbook.renderbox.components.Sphere;
import com.cardbook.renderbox.materials.UnlitTexMaterial;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends CardboardActivity implements IRenderBox {
    final String TAG = "MainActivity";
    public static boolean cancelUpdate;

    final int DEFAULT_BACKGROUND = R.drawable.bg;
    final String imagesPath = "/storage/emulated/0/DCIM/Camera";

    final List<Image> images = new ArrayList<>();

    CardboardView cardboardView;
    Plane screen;
    Sphere photosphere;
    int bgTextureHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        cancelUpdate = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRestoreGLStateEnabled(false);
        cardboardView.setRenderer(new RenderBox(this, this));
        setCardboardView(cardboardView);
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
        showImage(images.get(0));
        showImage(images.get(images.size()-1));
        showImage(images.get(2));
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
        BorderMaterial screenMaterial = new BorderMaterial();
        //screenMaterial.setTexture(RenderObject.loadTexture(R.drawable.sample360));
        screen = new Plane();
        screen.setupBorderMaterial(screenMaterial);

        new Transform()
                .setLocalScale(4, 4, 1)
                .setLocalPosition(0, 0, 5)
                .setLocalRotation(0, 0, 180)
                .addComponent(screen);
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
            image.show(cardboardView, screen, 4f);
        }
    }


    @Override
    public void preDraw() {

    }

    @Override
    public void postDraw() {

    }

    int loadImageList(String path) {
        File f = new File(path);
        File file[] = f.listFiles();
        Log.d(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Log.d(TAG, "GETTING FILES FROM " + path);
        Log.d(TAG, "Length: "+ file.length);
        if (file==null)
            return 0;
        for (int i = 0; i < file.length; i++) {
            if (Image.isValidImage(file[i].getName())) {
                Image img = new Image(path + "/" + file[i].getName());
                images.add(img);
                Log.d(TAG, file[i].getName());
            }
        }
        return file.length;
    }
}
