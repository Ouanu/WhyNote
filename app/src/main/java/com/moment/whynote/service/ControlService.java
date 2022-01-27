package com.moment.whynote.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class ControlService extends Service {

    private String address;
    private int port;
    private Looper serviceLooper;
//    private ServiceHandler serviceHandler;
    private Client client;

    public ControlService() {
    }

//    private class ServiceHandler extends Handler {
//
//        public ServiceHandler(@NonNull @NotNull Looper looper) {
//            super(looper);
//        }
//
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//
//        }
//    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        client = Client.getInstance();
//        HandlerThread thread = new HandlerThread("ServiceStartArguments",
//                THREAD_PRIORITY_BACKGROUND);
//        thread.start();
//
//        serviceLooper = thread.getLooper();
//        serviceHandler = new ServiceHandler(serviceLooper);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        System.out.println(intent.getStringExtra("ip"));
        address = intent.getStringExtra("ip");
        port = intent.getIntExtra("port", -1);
        new Thread(()->{
            try {
                Socket socket = new Socket(address, port);
                client.executeTask(socket);
                File[] files = new File("/sdcard/Android/data/com.moment.whynote/files/Documents").listFiles();
                client.uploadFolder(files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();



        return super.onStartCommand(intent, flags, startId);
    }
}