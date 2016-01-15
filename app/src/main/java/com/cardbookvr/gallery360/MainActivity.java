package com.cardbookvr.gallery360;

import android.os.Bundle;

import com.cardbook.renderbox.IRenderBox;
import com.cardbook.renderbox.RenderBox;
import com.cardbook.renderbox.Transform;
import com.cardbook.renderbox.components.Camera;
import com.cardbook.renderbox.components.Sphere;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;

public class MainActivity extends CardboardActivity implements IRenderBox {
    final String TAG = "MainActivity";

    final int DEFAULT_PHOTO = R.drawable.bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRestoreGLStateEnabled(false);
        cardboardView.setRenderer(new RenderBox(this, this));
        setCardboardView(cardboardView);
    }

    @Override
    public void setup() {
        Transform background = new Transform()
                .setLocalScale(-Camera.Z_FAR, -Camera.Z_FAR, -Camera.Z_FAR)
                .addComponent(new Sphere(DEFAULT_PHOTO, false));

        Transform screen = new Transform()
                .setLocalScale(4, 4, 1)
                .setLocalPosition(0, 0, 5)
                .setLocalRotation(0, 0, 180)
                .addComponent(new Plane(R.drawable.sample360, false));
    }

    @Override
    public void preDraw() {

    }

    @Override
    public void postDraw() {

    }

}
