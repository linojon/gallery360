package com.cardbookvr.gallery360;

import android.os.Bundle;
import android.util.Log;

import com.cardbook.renderbox.IRenderBox;
import com.cardbook.renderbox.RenderBox;
import com.cardbook.renderbox.Transform;
import com.cardbook.renderbox.components.Camera;
import com.cardbook.renderbox.components.Sphere;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends CardboardActivity implements IRenderBox {
    final String TAG = "MainActivity";

    final int DEFAULT_PHOTO = R.drawable.bg;
    final String imagesPath = "/storage/emulated/0/DCIM/Camera";

    final List<Image> images = new ArrayList<>();

    CardboardView cardboardView;
    Plane screen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRestoreGLStateEnabled(false);
        cardboardView.setRenderer(new RenderBox(this, this));
        setCardboardView(cardboardView);
    }

    @Override
    public void setup() {
        setupBackground();
        setupScreen();
        loadImageList(imagesPath);
        showImage(images.get(0));
    }

    void setupBackground() {
        new Transform()
                .setLocalScale(-Camera.Z_FAR, -Camera.Z_FAR, -Camera.Z_FAR)
                .addComponent(new Sphere(DEFAULT_PHOTO, false));
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
        image.show(cardboardView, screen);
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
