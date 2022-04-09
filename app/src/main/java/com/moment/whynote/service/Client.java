package com.moment.whynote.service;



import android.annotation.SuppressLint;
import android.util.Log;

import com.moment.whynote.data.ResData;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class Client {

    private Socket socket;

    public Client(String address, int port) {
        try {
            socket = new Socket(address, port);
            Log.i("Client", "Client: socket=============");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute(List<ResData> resDataList) throws IOException {
        if (socket == null || !socket.isConnected()) {
            return;
        }
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        DirAndFileUtil util = new DirAndFileUtil(2);
        HashMap<Long, ResData> idList = new HashMap<>();
        Log.e("CLIENT", "execute:====== " + resDataList.get(0).uid);
        for (int i = 0; i < resDataList.size(); i++) {
            idList.put((long) resDataList.get(i).uid, resDataList.get(i));
        }


        if (socket.isConnected()) {
            outputStream.writeUTF("Hello there!!");
            String s = inputStream.readUTF();
            System.out.println(s);
            outputStream.writeInt(34567);
            s = inputStream.readUTF();
            System.out.println(s);

        }


        if (inputStream.readLong() == 1000L) {
            System.out.println("有数据库");
            boolean isChange = false;
            outputStream.writeInt(idList.size());
            for (Long aLong : idList.keySet()) {
                outputStream.writeLong(aLong);
                outputStream.writeLong(Objects.requireNonNull(idList.get(aLong)).updateDate);
                if (inputStream.readInt() == 999) {
                    if (inputStream.readBoolean()) {
                        outputStream.writeUTF(Objects.requireNonNull(idList.get(aLong)).dirName);
                        if (inputStream.readUTF().equals("文件夹创建失败")) {
                            continue;
                        }
                        @SuppressLint("SdCardPath")
                        File dir = new File("/sdcard/Android/data/com.moment.whynote/files/Documents/" + Objects.requireNonNull(idList.get(aLong)).dirName);
                        String[] list = dir.list();
                        assert list != null;
                        outputStream.writeInt(list.length);
                        for (File file : Objects.requireNonNull(dir.listFiles())) {
                            util.sendFiles(outputStream, file);
                        }
                        isChange = true;
                    }
                } else {
                    outputStream.writeUTF(Objects.requireNonNull(idList.get(aLong)).dirName);
                    if (inputStream.readUTF().equals("文件夹创建失败")) {
                        continue;
                    }
                    @SuppressLint("SdCardPath")
                    File dir = new File("/sdcard/Android/data/com.moment.whynote/files/Documents/" + Objects.requireNonNull(idList.get(aLong)).dirName);
                    String[] list = dir.list();
                    assert list != null;
                    outputStream.writeInt(list.length);
                    for (File file : Objects.requireNonNull(dir.listFiles())) {
                        util.sendFiles(outputStream, file);
                    }
                    isChange = true;
                }

            }
            if (isChange) {
                System.out.println("数据库需要更新");
                System.out.println(inputStream.readUTF());
                @SuppressLint("SdCardPath")
                File sqlDir = new File("/data/data/com.moment.whynote/databases/");
                outputStream.writeInt(Objects.requireNonNull(sqlDir.list()).length);

                if (sqlDir.exists()) {
                    File[] sqlFiles = sqlDir.listFiles();
                    assert sqlFiles != null;
                    for (File file : sqlFiles) {
//                    File sql = new File("/data/data/com.moment.whynote/databases/");
                        Log.d("Client", "execute: " + file.getName());
                        util.sendFiles(outputStream, file);
                    }
                }
//                System.out.println(inputStream.readUTF());
            } else {
                System.out.println("数据库不需要更新");
            }

        } else {
            util.synchronizeFiles(inputStream, outputStream);
            @SuppressLint("SdCardPath")
            File sqlDir = new File("/data/data/com.moment.whynote/databases/");
            outputStream.writeInt(Objects.requireNonNull(sqlDir.listFiles()).length);

            if (sqlDir.exists()) {
                File[] sqlFiles = sqlDir.listFiles();
                assert sqlFiles != null;
                for (File file : sqlFiles) {
//                    File sql = new File("/data/data/com.moment.whynote/databases/");
                    Log.d("Client", "execute: " + file.getName());
                    util.sendFiles(outputStream, file);
                }
            }
            System.out.println(inputStream.readUTF());
        }
        outputStream.close();
        inputStream.close();
        socket.close();
    }

}
