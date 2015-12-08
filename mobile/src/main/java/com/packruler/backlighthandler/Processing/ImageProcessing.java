package com.packruler.backlighthandler.Processing;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

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
    private Rect cropRect;
    private int[] green;


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

    }

    public void process(Image image) {
        Image.Plane[] planes = image.getPlanes();
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);
        bitmap.copyPixelsFromBuffer(planes[0].getBuffer());
        Log.i(TAG, "Bitmap h: " + bitmap.getHeight() + " w: " + bitmap.getWidth());

        cropRect = image.getCropRect();
        int yOffset = -1;
        int xOffset = -1;
        int pixel;
        for (int y = 0; y < bitmap.getHeight() / 2; y++) {
            for (int x = 0; x < bitmap.getHeight() / 2; x++) {
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
        Log.i(TAG, "V: " + yOffset + " X: " + xOffset);
        if (yOffset < 0)
            yOffset = 0;
        if (xOffset < 0)
            xOffset = 0;

        StringBuffer buffer = new StringBuffer("Colors: ");
        for (int y = yOffset; y < bitmap.getHeight() - yOffset; y++) {
            for (int x = xOffset; x < bitmap.getWidth() - xOffset; x++) {
                pixel = bitmap.getPixel(x, bitmap.getHeight() / 2);
                buffer.append('R').append(Color.red(pixel)).append(", ")
                        .append('G').append(Color.green(pixel)).append(", ")
                        .append('B').append(Color.blue(pixel)).append(", ");
                if (!(y < yOffset + yDepth &&
                        y > bitmap.getHeight() - yOffset - yDepth) &&
                        x > xOffset + xDepth && x < bitmap.getWidth() - xOffset - xDepth)
                    x = bitmap.getWidth() - xOffset - xDepth;
            }
            if (y > yOffset + yDepth &&
                    y < bitmap.getHeight() - yOffset - yDepth)
                y = bitmap.getWidth() - yOffset - yDepth;
        }
        buffer.append("\n");


        image.close();
    }
}
