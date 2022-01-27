package com.moment.whynote.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.moment.whynote.fragment.ConnectFragment;

import org.jetbrains.annotations.NotNull;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class ConnectService extends Service implements ConnectFragment.ConnectListener {

    private String address;
    private int port;
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private class ServiceHandler extends Handler {

        public ServiceHandler(@NonNull @NotNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
//            switch (msg.what) {
//
//            }
            try {
                Thread.sleep(5000);
                Log.d("ConnectService", "handleMessage: ==========");
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnectSelected(Bundle bundle) {
        address = bundle.getString("ip");
        port = bundle.getInt("port");
        Log.d("ConnectService", "onConnectSelected: ==========");
    }
}
