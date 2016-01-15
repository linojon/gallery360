package com.cardbookvr.gallery360;

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
        if (vertexBuffer != null) return;
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
}
