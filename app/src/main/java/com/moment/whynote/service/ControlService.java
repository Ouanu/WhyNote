package com.moment.whynote.service;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;

public class ControlService extends Service {
    private final static String TAG = "ControlService";
    private Bundle bundle;


    @Override
    public void onCreate() {
        Log.d(TAG, "we in.........");
        super.onCreate();

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
        bundle = intent.getExtras();
        new Thread(() -> {
            try {
                WNClient client = new WNClient(bundle.getString("ip"), bundle.getInt("port"));
//                Toast.makeText(getApplicationContext(), "in thread", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onStartCommand: The service Destroy.");
        super.onDestroy();

    }
}