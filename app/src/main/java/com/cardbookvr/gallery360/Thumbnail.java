package com.cardbookvr.gallery360;

import com.cardbook.renderbox.components.*;
import com.cardbook.renderbox.materials.UnlitTexMaterial;
import com.cardbook.renderbox.math.Quaternion;
import com.google.vrtoolkit.cardboard.CardboardView;

/**
 * Created by mtsch on 3/12/2016.
 */
public class Thumbnail {
    public Plane plane;
    public Sphere sphere;
    public Image image;
    CardboardView cardboardView;

    public Thumbnail(CardboardView cardboardView){
        this.cardboardView = cardboardView;
    }

    public void setVisible(boolean visible){
//		if(plane == null || sphere == null)
//			return;     //This should never happen, but will avoid nullpointer exceptions
        if(visible) {
            if(image.isPhotosphere){
                plane.enabled = false;
                sphere.enabled = true;
            } else{
                plane.enabled = true;
                sphere.enabled = false;
            }
        } else {
            plane.enabled = false;
            sphere.enabled = false;
        }
    }
    public void setImage(Image image){
//		if(plane == null || sphere == null)
//			return;     //This should never happen, but will avoid nullpointer exceptions
        this.image = image;
        //Turn the image into a GPU texture
        image.loadTexture(cardboardView, 4);
        //wait until texture binding is done
        try {
            while (Image.loadLock) {
                if (MainActivity.cancelUpdate)
                    return;
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(image.isPhotosphere){
            UnlitTexMaterial material = (UnlitTexMaterial) sphere.getMaterial();
            material.setTexture(image.textureHandle);
        } else {
//            BorderMaterial material = (BorderMaterial) plane.getMaterial();
//
//            if (image == null) {
//                material.setTexture(0);
//            } else {
//                material.setTexture(image.textureHandle);
//
//                if (image.width > 0)
//                    plane.transform.setLocalScale(1, (float) image.height / image.width, 1);
//                if (image.rotation != null) {
//                    plane.transform.setLocalRotation(new Quaternion(image.rotation));
//                }
//            }
            image.showThumbnail(cardboardView, plane);
        }
    }
}
