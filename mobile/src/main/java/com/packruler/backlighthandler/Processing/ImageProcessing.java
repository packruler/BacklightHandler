package com.packruler.backlighthandler.Processing;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by packruler on 12/7/15.
 */
public class ImageProcessing {
    private final String TAG = getClass().getSimpleName();

    private int verticalCount;
    private int horizontalCount;
    private int bottomGab;
    private int xDepth;
    private int yDepth;
    private int[] hR, hG, hB;
    private int[] vR, vG, vB;
    private int hSpacing, vSpacing;


    public ImageProcessing() {
        xDepth = 20;
        yDepth = 20;
    }

//    public ImageProcessing(int verticalCount, int horizontalCount, int bottomGab, int xDepth, int yDepth) {
//        this.verticalCount = verticalCount;
//        this.horizontalCount = horizontalCount;
//        this.bottomGab = bottomGab;
//        this.xDepth = xDepth;
//        this.yDepth = yDepth;
//    }
//
//    private void setup() {
//
//
//    }

    public void process(Image image) {
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());
        image.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
    }


}
