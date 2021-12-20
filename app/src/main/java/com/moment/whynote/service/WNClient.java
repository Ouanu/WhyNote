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
import java.util.Objects;
import java.util.Queue;

public class WNClient {
    private static final String TAG = "WNClient.class";
    private final Socket socket;
    private DataOutputStream outputStream = null;
    private DataInputStream inputStream = null;
    private static WNClient instance = null;


    public WNClient(Socket socket) {
        this.socket = socket;
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
    public boolean PrepareWork() {
        try {
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            /*
              检查是否连接Server
             */
            outputStream.writeInt(0xe25eb);
            outputStream.flush();
            int result = inputStream.readInt();
            Log.d(TAG, "PrepareWork: " + result);
            return result == 0xdeb2c;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 传送数据库
     */
    public void PushDatabase() {
        @SuppressLint("SdCardPath")
        File file = new File("/data/data/com.moment.whynote/databases");
        File[] files = file.listFiles();
        assert files != null;
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
                byte[] buffer = new byte[1000];
                while (fis.available() > 0) {
                    int len = fis.read(buffer);
                    outputStream.write(buffer, 0, len);
                    outputStream.flush();
                }
                Log.d(TAG, "PushDatabase: " + inputStream.readUTF());
            } catch (IOException e) {
                Log.d(TAG, "in pushDatabase Push file false......");
            }
        }

    }

    /**
     * 发送图片文件
     */
    public void PushImageCache() {
        @SuppressLint("SdCardPath")
        File dir = new File("/data/data/com.moment.whynote/files/DCIM");
        File[] fileList = dir.listFiles();
        Queue<File> fileQueue = new LinkedList<>();
        assert fileList != null;
        for (File file : fileList)
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                fileQueue.offer(file);
                assert files != null;
                for (File file1 : files) {
                    if (file1.isFile())
                        fileQueue.offer(file1);
                }
            }
        while(fileQueue.size() > 0) {
            try {
                outputStream.writeInt(0x29cdd);
                outputStream.writeUTF(Objects.requireNonNull(fileQueue.peek()).getName());
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
