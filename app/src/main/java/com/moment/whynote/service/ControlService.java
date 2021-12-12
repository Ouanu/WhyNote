package com.moment.whynote.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

public class ControlService extends Service {
    private final static String TAG = "ControlService";
    private Bundle bundle;
    private final ControlBinder binder = new ControlBinder();
    private Socket socket;

    /**
     * 提供数据交换接口
     */
    public class ControlBinder extends Binder {
        public ControlService getService() {
            return ControlService.this;
        }
    }


    @Override
    public void onCreate() {
        Log.d(TAG, "we in.........");
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        bundle = intent.getExtras();
        Log.d(TAG, "onBind: ===============");
        new Thread(() -> {
            try {
                Log.d(TAG, "onCreate: ++++++++++++");
                socket = new Socket("172.18.3.5", 7290);
                WNClient client = WNClient.getInstance(socket);
                Log.d(TAG, "onStartCommand: " + client.hashCode());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return binder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: -------------");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        boolean quit = true;
        Log.d(TAG, "onDestroy: service disconnect");
        super.onDestroy();
    }
}