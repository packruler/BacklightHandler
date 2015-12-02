package com.packruler.backlighthandler;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
    private SurfaceTexture texture;
    private Surface surface;
    private boolean displayActive;
    public static SurfaceView view;
    public static ScreenHandler instance;


    public static class Binder extends android.os.Binder {
        private ScreenHandler service;

        protected Binder(ScreenHandler service) {
            this.service = service;
        }

        public ScreenHandler getService() {
            return service;
        }
    }

    public interface Notifiable {
        void bitmapUpdated();
    }

    private VirtualDisplay.Callback displayCallback = new VirtualDisplay.Callback() {
        @Override
        public void onPaused() {
            super.onPaused();
            displayActive = false;
        }

        @Override
        public void onResumed() {
            super.onResumed();
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
        HandlerThread thread = new HandlerThread("Callback");
        thread.start();
        callbackHandler = new Handler(thread.getLooper());

        thread = new HandlerThread("BackgroundThread");
        thread.start();
        backgroundThread = new Handler(thread.getLooper());

        makeSurface();
        instance = this;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
//        texture.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder(this);
    }

    public void setProjection(MediaProjection projection, SurfaceView surfaceView) {
        this.projection = projection;
        this.projection.registerCallback(projectionCallback, callbackHandler);
        view = surfaceView;
        surface = surfaceView.getHolder().getSurface();
        VirtualDisplay display = projection.createVirtualDisplay("ScreenHandler",
                DISPLAY_WIDTH, DISPLAY_HEIGHT,
                DisplayMetrics.DENSITY_DEFAULT,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, displayCallback, callbackHandler);
        backgroundThread.post(update);
    }

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            long last;
            long now = System.currentTimeMillis();
            long delta;
            long sleepFor;

            int count = 0;
            int max = 50;
            synchronized (this) {
                while (projection != null && count < max) {
                    try {
                        try {
                            Bitmap bitmap = pullBitmap();
                            File folder = new File(Environment.getExternalStorageDirectory(), "back light work/");
                            if (!folder.exists())
                                Log.i("folder", folder.mkdir() + "");
                            else Log.i("folder", "exists");
                            File file = new File(folder.getPath(), "bitmap" + count++ + ".png");
                            Log.i("file", file.toString());
                            if (!file.exists())
                                Log.i("create ", "" + file.createNewFile());
                            else Log.i("file", "exists");
                            FileOutputStream outputStream = new FileOutputStream(file);

                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            Log.i("Service", "Done");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        last = now;
                        now = System.currentTimeMillis();
                        delta = now - last;
                        Log.i("Delta", delta + "");
                        sleepFor = 50 - delta;
                        if (sleepFor > 0)
                            this.wait(sleepFor);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private void makeSurface() {
//        int[] textures = new int[1];
//// generate one texture pointer and bind it as an external texture.
//        GLES20.glGenTextures(1, textures, 0);
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
//// No mip-mapping with camera source.
//        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
//                GL10.GL_TEXTURE_MIN_FILTER,
//                GL10.GL_LINEAR);
//        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
//                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
//// Clamp to edge is only option.
//        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
//                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
//                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
//
//
//        int texture_id = textures[0];
//        texture = new SurfaceTexture(texture_id);
//        surface = new Surface(texture);
    }

    public Bitmap pullBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(DISPLAY_WIDTH, DISPLAY_HEIGHT, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private int count = 0;
    private int max = 50;

    public void bitmapUpdated() {
        if (count <= max) {
            try {
                Bitmap bitmap = pullBitmap();
                File file = new File(Environment.getExternalStorageDirectory() + "/back light work/", "bitmap" + count++ + ".png");
//                    Log.i("make dirs", "" + file.mkdir());
                if (!file.exists())
                    Log.i("create ", "" + file.createNewFile());
                else Log.i("file", "exists");
                FileOutputStream outputStream = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                bitmap.recycle();
                Log.i("Service", "Done");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
