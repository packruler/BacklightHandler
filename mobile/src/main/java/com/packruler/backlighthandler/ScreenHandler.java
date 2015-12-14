package com.packruler.backlighthandler;

import android.app.Service;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.packruler.backlighthandler.Processing.ImageProcessing;

/**
 * Created by packruler on 11/25/15.
 */
public class ScreenHandler extends Service {
    private final String TAG = getClass().getSimpleName();
    private final static int DISPLAY_PLAYING = 0;
    private final static int DISPLAY_PAUSED = 1;
    private final static int DISPLAY_STOPPED = 2;
    public final static int DISPLAY_WIDTH = 320;
    public final static int DISPLAY_HEIGHT = 180;
    private MediaProjection projection;
    private SurfaceTexture texture;
    private Surface surface;
    private boolean displayActive;
    public static SurfaceView view;
    public static ScreenHandler instance;
    private VirtualDisplay display;
    private ImageProcessing imageProcessing = new ImageProcessing();


    public static class Binder extends android.os.Binder {
        private ScreenHandler service;

        protected Binder(ScreenHandler service) {
            this.service = service;
        }

        public ScreenHandler getService() {
            return service;
        }
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

    //    private MediaProjection.Callback projectionCallback = new MediaProjection.Callback() {
//        @Override
//        public void onStop() {
//            backgroundThread.getLooper().quitSafely();
//            callbackHandler.getLooper().quitSafely();
//            ScreenHandler.this.stopSelf();
//        }
//    };
//    private Handler callbackHandler;
    private Handler backgroundThread;

    @Override
    public void onCreate() {
//        HandlerThread thread = new HandlerThread("Callback");
//        thread.start();
//        callbackHandler = new Handler(thread.getLooper());

        HandlerThread thread = new HandlerThread("BackgroundThread");
        thread.start();
        backgroundThread = new Handler(thread.getLooper());
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        backgroundThread.getLooper().quitSafely();
        display.release();
        imageReader.close();
        try {
            Hyperion hyperion = new Hyperion("192.168.86.153", 19445);
            hyperion.clearall();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder(this);
    }

    public void setProjection(MediaProjection projection) {
        this.projection = projection;
//        this.projection.registerCallback(projectionCallback, callbackHandler);
//        view = surfaceView;
//        surface = surfaceView.getHolder().getSurface();
        display = projection.createVirtualDisplay("ScreenHandler",
                DISPLAY_WIDTH, DISPLAY_HEIGHT,
                DisplayMetrics.DENSITY_DEFAULT,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                null, displayCallback, null);

        setImageReader();
    }

    ImageReader imageReader;

    private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.i(TAG, "New Image");
            long start = System.currentTimeMillis();
            if (imageProcessing == null)
                imageProcessing = new ImageProcessing();
            try {
                imageProcessing.process(reader.acquireLatestImage(), 100);
            } catch (NullPointerException e) {
                Log.e(TAG, e.getMessage());
            }

            Log.i(TAG, "Update took: " + (System.currentTimeMillis() - start));
        }
    };

    private void setImageReader() {
        imageReader = ImageReader.newInstance(DISPLAY_WIDTH, DISPLAY_HEIGHT, ImageFormat.RGB_565, 5);
        imageReader.setOnImageAvailableListener(imageAvailableListener, backgroundThread);
        display.setSurface(imageReader.getSurface());
    }
}