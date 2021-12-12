package com.moment.whynote.service;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class WNClient {
    private static final String TAG = "WNClient.class";
    private final Socket socket;
    private DataOutputStream outputStream = null;
    private DataInputStream inputStream = null;
    private static WNClient instance = null;
    private static int result;
    private static boolean prepareFlag = false;

    public WNClient(Socket socket) {
        this.socket = socket;
        PrepareWork();
        PushDatabase();
//        if (prepareFlag && result == 0xb2018) PushDatabase();
//        else Log.d(TAG, "Push file false......");
//        PushImageCache();

    }

    public static WNClient getInstance(Socket socket) {
        if (instance == null) {
            synchronized (WNClient.class) {
                instance = new WNClient(socket);
                return instance;
            }
        }
        return instance;
    }

    /**
     * 准备工作
     */
    public void PrepareWork() {
        try {
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            /*
              通知 Server 准备好接受文件
             */
//            outputStream.writeInt(0x1b859);
//            outputStream.writeUTF("Hello");
//            outputStream.flush();
//            result = inputStream.readInt();
//            Log.d(TAG, "PrepareWork: " + result);
//            prepareFlag = true;
        } catch (IOException e) {
            e.printStackTrace();
            prepareFlag = false;
            Log.d(TAG, "PrepareWork: false");
        }
    }

    /**
     * 传送数据库
     */
    private void PushDatabase() {
        @SuppressLint("SdCardPath")
        File file = new File("/data/data/com.moment.whynote/databases");
        File[] files = file.listFiles();
        for (File file1 : files) {
            try {
            /*
            1.发送接收数据库指令
            2.发送文件名
            3.发送文件大小
            4.等待Server发回接收完成
             */
//            outputStream.writeInt(0x29cdd);
                Log.d(TAG, "PushDatabase: ========" + file1.getName());
                outputStream.writeUTF(file1.getName());
                outputStream.flush();
                FileInputStream fis = new FileInputStream(file1);
                outputStream.writeInt(fis.available());
                byte[] buffer = new byte[1024];
                while (fis.available() > 0) {
                    int len = fis.read(buffer);
                    outputStream.write(buffer, 0, len);
                    outputStream.flush();
                }
//                Log.d(TAG, "PushDatabase: " + inputStream.readUTF());
            } catch (IOException e) {
                Log.d(TAG, "in pushdatabase Push file false......");
            }
        }

    }

    /**
     * 发送图片文件
     */
    private void PushImageCache() {
        File dir = new File("/data/data/com.moment.whynote/files/DCIM");
        File[] fileList = dir.listFiles();
        Queue<File> fileQueue = new LinkedList<>();
        for (File file : fileList) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                fileQueue.offer(file);
                for (File file1 : files) {
                    if(file1.isFile())
                        fileQueue.offer(file1);
                }
            }
        }
        while(fileQueue.size() > 0) {
            try {
                outputStream.writeInt(0x29cdd);
                outputStream.writeUTF(fileQueue.peek().getName());
                FileInputStream fis = new FileInputStream(fileQueue.peek());
                outputStream.write(fis.available());
                byte[] buffer = new byte[1024];
                while (fis.available() > 0) {
                    int len = fis.read(buffer);
                    outputStream.write(buffer, 0, len);
                    outputStream.flush();
                }
                Log.d(TAG, "PushDatabase: " + inputStream.readUTF());
            } catch (IOException e) {
//                e.printStackTrace();
                Log.d(TAG, "PushImageCache: sending images false");
            }
        }
    }



}
