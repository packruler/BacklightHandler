package com.packruler.backlighthandler;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.opengl.GLSurfaceView;
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
import java.nio.Buffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

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
    private VirtualDisplay display;


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

//        makeSurface();
//        instance = this;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
//        texture.release();
        Log.i(TAG, "onDestroy");
        backgroundThread.getLooper().quitSafely();
        display.release();
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
        display = projection.createVirtualDisplay("ScreenHandler",
                DISPLAY_WIDTH, DISPLAY_HEIGHT,
                DisplayMetrics.DENSITY_DEFAULT,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                null, displayCallback, null);

        setImageReader();
    }

    private void makeSurface() {
        backgroundThread.post(new Runnable() {
            @Override
            public void run() {
//                int contexts[] = new int[1];
//                texture = new SurfaceTexture(contexts[0]);
//                GLES20.glEnable(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
//                GLES20.glGenTextures(1, contexts, 0);
//                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, contexts[0]);
//                int width = DISPLAY_WIDTH; // size of preview
//                int height = DISPLAY_HEIGHT;  // size of preview
//                GLES20.glTexImage2D(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0, GLES20.GL_RGBA, width,
//                        height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
//                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//
//                Log.i(TAG, "ID: " + contexts[0]);
//
//                texture = new SurfaceTexture(contexts[0]);
//                texture.setDefaultBufferSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
//                texture.setOnFrameAvailableListener(onFrameAvailableListener);
//
////                CreateSurfaceEGL(texture, DISPLAY_WIDTH, DISPLAY_HEIGHT);
//                surface = new Surface(texture);

//                imageReader = ImageReader.newInstance(DISPLAY_WIDTH, DISPLAY_HEIGHT, ImageFormat.RGB_565, 5);
//                surface = imageReader.getSurface();
//                imageReader.setOnImageAvailableListener(, backgroundThread);

//                Log.i(TAG, "Surface valid " + surface.isValid());
            }
        });
    }

    public Bitmap pullBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(DISPLAY_WIDTH, DISPLAY_HEIGHT, Bitmap.Config.RGB_565);
        Canvas canvas = surface.lockCanvas(null);
        canvas.setBitmap(bitmap);
        surface.unlockCanvasAndPost(canvas);
        texture.releaseTexImage();
        return bitmap;
    }

    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            EGLContext.getEGL();
            Log.i(TAG, "Frame available");
            bitmapUpdated();
//            texture.releaseTexImage();
        }
    };

    int count = 0;

    public void bitmapUpdated() {
//        texture.updateTexImage();
        saveBitmap(pullBitmap());
    }

    ImageReader imageReader;

    private void saveBitmap(final Bitmap bitmap) {
        if (queue.remainingCapacity() > 0) {
            backgroundSaveHandler.execute(new SaveImage(bitmap));
        } else {
            Log.i(TAG, "All working");
            bitmap.recycle();
        }
    }

    private GLSurfaceView.Renderer renderer = new GLSurfaceView.Renderer() {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.i(TAG, "onSurfaceCreated");
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.i(TAG, "onSurfaceChanged");

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Log.i(TAG, "onDrawFrame");
        }
    };

    private Bitmap processImage(Image image) {
        Bitmap bitmap = Bitmap.createBitmap(DISPLAY_WIDTH, DISPLAY_HEIGHT, Bitmap.Config.RGB_565);
        if (image != null) {
            try {
                Image.Plane[] planes = image.getPlanes();
                Buffer buffer = planes[0].getBuffer().rewind();
                Log.i(TAG, buffer.toString());
                bitmap.copyPixelsFromBuffer(buffer);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                image.close();
            }
        }
        return bitmap;
    }

    private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.i(TAG, "New Image");
            long start = System.currentTimeMillis();
//            saveBitmap(processImage(reader.acquireLatestImage()), count++ % 3);
            Bitmap bitmap = processImage(imageReader.acquireLatestImage());

            saveBitmap(bitmap);

//            setImageReader();
            Log.i(TAG, "Update took: " + (System.currentTimeMillis() - start));
        }
    };
    ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(4);
    ThreadPoolExecutor backgroundSaveHandler = new ThreadPoolExecutor(4, 4, 1, TimeUnit.HOURS, queue);

    private void setImageReader() {
        imageReader = ImageReader.newInstance(DISPLAY_WIDTH, DISPLAY_HEIGHT, ImageFormat.RGB_565, 5);
        imageReader.setOnImageAvailableListener(imageAvailableListener, backgroundThread);
        display.setSurface(imageReader.getSurface());
    }

    private class SaveImage implements Runnable {
        private final Bitmap bitmap;

        public SaveImage(Bitmap bmp) {
            bitmap = bmp;
        }

        @Override
        public void run() {
            try {
                File folder = new File(Environment.getExternalStorageDirectory(), "Back Light Work/");
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
                Log.i(TAG, file.getName() + " Saved");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                bitmap.recycle();
            }
        }
    }
}