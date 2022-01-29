package com.moment.whynote.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.File;
import java.io.IOException;
import java.net.Socket;


public class ControlService extends Service {

    private String address;
    private int port;
    private Client client;

    public ControlService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        client = Client.getInstance();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        address = intent.getStringExtra("ip");
        port = intent.getIntExtra("port", -1);
        new Thread(()->{
            try {
                Socket socket = new Socket(address, port);
                client.executeTask(socket);
                @SuppressLint("SdCardPath")
                File[] files = new File("/sdcard/Android/data/com.moment.whynote/files/Documents").listFiles();
                client.uploadFolder(files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();



        return super.onStartCommand(intent, flags, startId);
    }
}