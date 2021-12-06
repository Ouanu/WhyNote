package com.moment.whynote.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class ControlService extends Service {
    private final static String TAG = "ControlService";
    private WNClient client;

    public ControlService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(() -> {
            try {
                client = new WNClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: The service on.");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onStartCommand: The service Destroy.");
        super.onDestroy();

    }
}