package com.cardbookvr.gallery360;

import com.cardbook.renderbox.components.RenderObject;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Jonathan on 1/14/2016.
 */
public class Plane extends RenderObject {
    static String TAG = "RenderObject.Plane";

    private static FloatBuffer vertexBuffer;
    private static FloatBuffer colorBuffer;
    private static FloatBuffer normalBuffer;
    private static FloatBuffer texCoordBuffer;
    private static ShortBuffer indexBuffer;

    public Plane(){
        super();
        allocateBuffers();
    }

}
