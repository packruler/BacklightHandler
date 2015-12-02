package com.packruler.backlighthandler;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
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
import java.nio.ByteBuffer;

/**
 * Created by packruler on 11/25/15.
 */
public class ScreenHandler extends Service {
    private final String TAG = getClass().getSimpleName();
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
            Log.i(TAG, "onPaused");
            displayActive = false;
        }

        @Override
        public void onResumed() {
            super.onResumed();
            Log.i(TAG, "onResumed");
        }

        @Override
        public void onStopped() {
            Log.i(TAG, "onStopped");
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
//        backgroundThread = new Handler(Looper.getMainLooper());

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
//        view = surfaceView;
//        surface = surfaceView.getHolder().getSurface();
        VirtualDisplay display = projection.createVirtualDisplay("ScreenHandler",
                DISPLAY_WIDTH, DISPLAY_HEIGHT,
                DisplayMetrics.DENSITY_DEFAULT,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, displayCallback, null);
        display.setSurface(surface);

        backgroundThread.post(update);
    }

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            long last;
            long now = System.currentTimeMillis();
            long delta;
            long sleepFor;

            int max = 1;
            synchronized (this) {
                while (projection != null && count < max) {
                    try {
                        saveBitmap(pullBitmap(), count++);

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
        backgroundThread.post(new Runnable() {
            @Override
            public void run() {
                int contexts[] = new int[1];
                texture = new SurfaceTexture(contexts[0]);
                GLES20.glEnable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
                GLES20.glGenTextures(1, contexts, 0);
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, contexts[0]);
                int width = DISPLAY_WIDTH; // size of preview
                int height = DISPLAY_HEIGHT;  // size of preview
//                GLES20.glTexImage2D(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0, GLES20.GL_RGBA, width,
//                        height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
//                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

                Log.i(TAG, "ID: " + contexts[0]);

                texture.setDefaultBufferSize(4, 4);
                texture.setOnFrameAvailableListener(onFrameAvailableListener);
                surface = new Surface(texture);
                Log.i(TAG, "Surface valid " + surface.isValid());
            }
        });
    }

    public Bitmap pullBitmap() {
        ByteBuffer buffer = ByteBuffer.allocate(DISPLAY_HEIGHT * DISPLAY_WIDTH);
        GLES20.glReadPixels(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT, ImageFormat.RGB_565, ImageFormat.JPEG, buffer);
        StringBuilder builder = new StringBuilder();
        for (int x = 0; x < 50; x++) {
            builder.append(buffer.get(x)).append(" | ");
        }
        Log.i(TAG, builder.toString());
        Bitmap bitmap = Bitmap.createBitmap(DISPLAY_WIDTH, DISPLAY_HEIGHT, Bitmap.Config.RGB_565);
        Canvas canvas = surface.lockCanvas(null);
        canvas.setBitmap(bitmap);
        surface.unlockCanvasAndPost(canvas);
        return bitmap;
    }

    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            Log.i(TAG, "Frame available");
            bitmapUpdated();
        }
    };

    int count = 0;

    public void bitmapUpdated() {
        texture.updateTexImage();
        saveBitmap(pullBitmap(), count++);
    }

    private void saveBitmap(final Bitmap bitmap, int count) {
        if (bitmap != null)
            try {
                File folder = new File(Environment.getExternalStorageDirectory(), "Back Light Work/");
                if (!folder.exists())
                    Log.i("folder", folder.mkdir() + "");
                else Log.i("folder", "exists");
                File file = new File(folder.getPath(), "bitmap" + count + ".png");
                Log.i("file", file.toString());
                if (!file.exists())
                    Log.i("create ", "" + file.createNewFile());
                else Log.i("file", "exists");
                FileOutputStream outputStream = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                Log.i("Service", "Done");
                bitmap.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        else {
            Log.e(TAG, "NULL");
        }
    }

}
