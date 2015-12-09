package com.packruler.backlighthandler.Processing;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;

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

    private native void processNative(Bitmap bitmap, int[] horizontalLED, int[] verticalLED, int xDepth, int yDepth);

    public ImageProcessing() {
        xDepth = 20;
        yDepth = 20;
    }

    public ImageProcessing(int verticalCount, int horizontalCount, int bottomGab, int xDepth, int yDepth) {
        this.verticalCount = verticalCount;
        this.horizontalCount = horizontalCount;
        this.bottomGab = bottomGab;
        this.xDepth = xDepth;
        this.yDepth = yDepth;
    }

    private void setup() {
        hR = new int[horizontalCount];
        hG = new int[horizontalCount];
        hB = new int[horizontalCount];

        vR = new int[verticalCount];
        vG = new int[verticalCount];
        vB = new int[verticalCount];


    }

    public void process(Image image) {
        Image.Plane[] planes = image.getPlanes();
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(planes[0].getBuffer());


        int[] pixelArray = new int[bitmap.getHeight() * bitmap.getWidth()];
        bitmap.getPixels(pixelArray, 0, 0, 0, 0, bitmap.getWidth(), bitmap.getHeight());

        ByteBuffer byteBuffer = planes[0].getBuffer();
        Log.i(TAG, "Position: " + byteBuffer.position());
        byte[] bytes = new byte[image.getHeight() * image.getWidth() * 3];
        byteBuffer.position(0);
        byteBuffer.get(bytes);
        Log.i(TAG, "Size: " + bytes.length);

        int yOffset = -1;
        int xOffset = -1;
        int pixel;
        for (int y = 0; y < bitmap.getHeight() / 2; y++) {
            for (int x = 0; x < bitmap.getWidth() / 2; x++) {
                pixel = bitmap.getPixel(x, y);
                if (Color.red(pixel) > 0 ||
                        Color.green(pixel) > 0 ||
                        Color.blue(pixel) > 0) {
                    yOffset = y;
                    xOffset = x;
                    break;
                }
            }
            if (yOffset != -1 && xOffset != -1)
                break;
        }
        if (yOffset < 0)
            yOffset = 0;
        if (xOffset < 0)
            xOffset = 0;

        StringBuffer buffer = new StringBuffer("Colors: ");
        int currentR, currentG, currentB;
        for (int y = yOffset; y < bitmap.getHeight() - yOffset; y++) {
            for (int x = xOffset; x < bitmap.getWidth() - xOffset; x++) {
                pixel = bitmap.getPixel(x, bitmap.getHeight() / 2);
                currentR = Color.red(pixel);
                currentG = Color.green(pixel);
                currentB = Color.blue(pixel);

                if (!(y < yOffset + yDepth &&
                        y > bitmap.getHeight() - yOffset - yDepth) &&
                        x > xOffset + xDepth && x < bitmap.getWidth() - xOffset - xDepth)
                    x = bitmap.getWidth() - xOffset - xDepth;
            }
        }
        buffer.append("\n");


        image.close();
    }

    private Bitmap removeBlackBars(Image image) {
        return null;
    }
}
