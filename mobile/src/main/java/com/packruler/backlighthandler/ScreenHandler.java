package com.packruler.backlighthandler;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Surface;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by packruler on 11/25/15.
 */
public class ScreenHandler extends Service {
    private final static int DISPLAY_PLAYING = 0;
    private final static int DISPLAY_PAUSED = 1;
    private final static int DISPLAY_STOPPED = 2;
    private final static int DISPLAY_WIDTH = 1280;
    private final static int DISPLAY_HEIGHT = 720;
    private MediaProjection projection;
    private VirtualDisplay display;
    private SurfaceTexture texture;
    private Surface surface;
    private boolean displayActive;
    private Canvas canvas;
    private Bitmap bitmap = Bitmap.createBitmap(DISPLAY_WIDTH, DISPLAY_HEIGHT, Bitmap.Config.RGB_565);

    private VirtualDisplay.Callback displayCallback = new VirtualDisplay.Callback() {
        @Override
        public void onPaused() {
            super.onPaused();
            displayActive = false;
            try {
                update.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onResumed() {
            super.onResumed();
            if (!displayActive)
                update.notifyAll();

            displayActive = true;
        }

        @Override
        public void onStopped() {
            super.onStopped();
        }
    };

    private MediaProjection.Callback projectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            backgroundThread.getLooper().quitSafely();
            callbackHandler.getLooper().quitSafely();
            ScreenHandler.this.stopSelf();
        }
    };
    private final Handler callbackHandler;
    private final Handler backgroundThread;

    public ScreenHandler() {
        HandlerThread thread = new HandlerThread(getPackageName() + ".callback");
        thread.run();
        callbackHandler = new Handler(thread.getLooper());

        thread = new HandlerThread(getPackageName() + ".backgroundThread");
        thread.run();
        backgroundThread = new Handler(thread.getLooper());

        makeSurface();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setProjection(MediaProjection projection) {
        this.projection = projection;
        this.projection.registerCallback(projectionCallback, callbackHandler);
        display = projection.createVirtualDisplay("ScreenHandler",
                DISPLAY_WIDTH, DISPLAY_HEIGHT,
                DisplayMetrics.DENSITY_DEFAULT,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, displayCallback, callbackHandler);
    }

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            long last;
            long now = System.currentTimeMillis();
            long delta;
            while (projection != null) {


                last = now;
                now = System.currentTimeMillis();
                delta = now - last;
            }
        }
    };

    private void makeSurface() {
        int[] textures = new int[1];
// generate one texture pointer and bind it as an external texture.
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
// No mip-mapping with camera source.
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
// Clamp to edge is only option.
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);


        int texture_id = textures[0];
        texture = new SurfaceTexture(texture_id);
        surface = new Surface(texture);
        canvas = surface.lockHardwareCanvas();
        canvas.setBitmap(bitmap);

    }
}
