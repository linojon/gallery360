package com.cardbookvr.gallery360;

import android.opengl.Matrix;

import com.cardbook.renderbox.RenderBox;
import com.cardbook.renderbox.components.RenderObject;
import com.cardbook.renderbox.materials.DiffuseLightingMaterial;
import com.cardbook.renderbox.materials.UnlitTexMaterial;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Jonathan on 1/14/2016.
 */
public class Plane extends RenderObject {
    static String TAG = "RenderObject.Plane";

    public static final float[] COORDS = new float[] {
            -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f
    };
    public static final float[] TEX_COORDS = new float[] {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0f, 0f,
            1.0f, 0f,
    };
    public static final float[] COLORS = new float[] {
            0.5f, 0.5f, 0.5f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f,
            0.5f, 0.5f, 0.5f, 1.0f
    };
    public static final float[] NORMALS = new float[] {
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f
    };
    public static final short[] INDICES = new short[] {
            0, 1, 2,
            1, 3, 2
    };

    private static FloatBuffer vertexBuffer;
    private static FloatBuffer colorBuffer;
    private static FloatBuffer normalBuffer;
    private static FloatBuffer texCoordBuffer;
    private static ShortBuffer indexBuffer;
    static final int numIndices = 6;

    static boolean setup;

    private static final float YAW_LIMIT = 0.15f;
    private static final float PITCH_LIMIT = 0.15f;
    public boolean isLooking;

    public Plane(){
        super();
        allocateBuffers();
    }

    public Plane(int textureId, boolean lighting) {
        super();
        allocateBuffers();
        if (lighting) {
            createDiffuseMaterial(textureId);
        } else {
            createUnlitTexMaterial(textureId);
        }
    }

    public static void allocateBuffers(){
        if (setup) return;
        setup = true;
        vertexBuffer = allocateFloatBuffer(COORDS);
        texCoordBuffer = allocateFloatBuffer(TEX_COORDS);
        colorBuffer = allocateFloatBuffer(COLORS);
        normalBuffer = allocateFloatBuffer(NORMALS);
        indexBuffer = allocateShortBuffer(INDICES);
    }

    public Plane createDiffuseMaterial(int textureId) {
        DiffuseLightingMaterial mat = new DiffuseLightingMaterial(textureId);
        mat.setBuffers(vertexBuffer, normalBuffer, texCoordBuffer, indexBuffer, numIndices);
        material = mat;
        return this;
    }

    public Plane createUnlitTexMaterial(int textureId) {
        UnlitTexMaterial mat = new UnlitTexMaterial(textureId);
        mat.setBuffers(vertexBuffer, texCoordBuffer, indexBuffer, numIndices);
        material = mat;
        return this;
    }

    public void setupUnlitTexMaterial(UnlitTexMaterial material){
        this.material = material;
        material.setBuffers(vertexBuffer, texCoordBuffer, indexBuffer, numIndices);
    }

    public void setupBorderMaterial(BorderMaterial material){
        this.material = material;
        material.setBuffers(vertexBuffer, texCoordBuffer, indexBuffer, numIndices);
    }

    @Override
    public void draw(float[] view, float[] perspective) {
        super.draw(view, perspective);
        if(!enabled)
            return;
        isLooking = isLookingAtObject();
    }

    private boolean isLookingAtObject() {
        float[] initVec = { 0, 0, 0, 1.0f };
        float[] objPositionVec = new float[4];
        float[] modelView = new float[16];

        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, RenderBox.headView, 0, model, 0);
        Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);

        float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
        float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }
}
