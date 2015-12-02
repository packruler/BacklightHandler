package com.packruler.backlighthandler;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BacklightHandlerSetup extends AppCompatActivity {
    private static final int MEDIA_PROJECTION_SETUP = 1;

    ScreenHandler screenHandler;
    MediaProjectionManager mediaProjectionManager;
    SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backlight_handler_setup);
        surfaceView = new SurfaceView(this);
        surfaceView.setDrawingCacheEnabled(true);
        bindService(new Intent(this, ScreenHandler.class), connection, BIND_AUTO_CREATE);

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupScreenHandler();
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScreenHandler();
            }
        });

        findViewById(R.id.pull).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (screenHandler != null) {
                    Bitmap bitmap = screenHandler.pullBitmap();
                    try {
                        File file = new File(Environment.getExternalStorageDirectory(), "bitmap.png");
//                    Log.i("make dirs", "" + file.mkdir());
                        if (!file.exists())
                            Log.i("create ", "" + file.createNewFile());
                        else Log.i("file", "exists");
                        FileOutputStream outputStream = new FileOutputStream(file);

                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        Log.i("Service", "Done");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MEDIA_PROJECTION_SETUP)
            screenHandler.setProjection(mediaProjectionManager.getMediaProjection(resultCode, data), surfaceView);

    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("Service", "Connected");
            screenHandler = ((ScreenHandler.Binder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("Service", "Disconnected");
            screenHandler = null;
        }
    };

    private void setupScreenHandler() {
        if (mediaProjectionManager == null)
            mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), MEDIA_PROJECTION_SETUP);
    }

    private void stopScreenHandler() {
        unbindService(connection);
    }
}
