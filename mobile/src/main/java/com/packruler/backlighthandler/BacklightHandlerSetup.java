package com.packruler.backlighthandler;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MEDIA_PROJECTION_SETUP)
            screenHandler.setProjection(mediaProjectionManager.getMediaProjection(resultCode, data));

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
        try {
            unbindService(connection);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "No bound service", Toast.LENGTH_SHORT).show();
        }
    }
}
