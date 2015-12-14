package com.packruler.backlighthandler.Processing;

import android.media.Image;
import android.util.Log;

import com.packruler.backlighthandler.Hyperion;

import java.io.IOException;
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

    private Hyperion hyperion;

    public ImageProcessing() {
        try {
            hyperion = new Hyperion("192.168.86.153", 19445);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void process(Image image, int priority) {
        if (hyperion == null)
            try {
                hyperion = new Hyperion("192.168.86.153", 19445);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                Log.e(TAG, "Not setup");
            }

        int width = image.getWidth();
        int height = image.getHeight();

        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        int pixelCount = width * height;
        byte[] bytes = new byte[pixelCount * 2];
        byteBuffer.get(bytes);

        byte[] out = new byte[pixelCount * 3];
        int z = 0;
        for (int i = 0; i < bytes.length; i += 2) {
            // Reconstruct 16 bit rgb565 value from two bytes
            int rgb565 = (bytes[i] & 255) | ((bytes[i + 1] & 255) << 8);

            // Extract raw component values (range 0..31 for g and b, 0..63 for g)
            int b5 = rgb565 & 0x1f;
            int g6 = (rgb565 >> 5) & 0x3f;
            int r5 = (rgb565 >> 11) & 0x1f;

            // Scale components up to 8 bit:
            // Shift left and fill empty bits at the end with the highest bits,
            // so 00000 is extended to 000000000 but 11111 is extended to 11111111
            out[z++] = (byte) ((r5 << 3) | (r5 >> 2));
            out[z++] = (byte) ((g6 << 2) | (g6 >> 4));
            out[z++] = (byte) ((b5 << 3) | (b5 >> 2));
        }

        image.close();
        try {
            hyperion.setImage(out, width, height, priority);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            Log.e(TAG, "isHyperion null " + (hyperion == null));
        }
    }
}
