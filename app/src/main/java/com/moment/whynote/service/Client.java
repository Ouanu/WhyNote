package com.moment.whynote.service;



import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewTreeLifecycleOwner;

import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;
import com.moment.whynote.viewmodel.ResViewModel;

import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Client {

    private int port = 9250;
    private String address = "192.168.137.1";
    private Socket socket;
    private ResRepository repository = ResRepository.getInstance();
    private static volatile Client instance = null;
    private List<ResData> resDataList;

    public Client() {
        try {
            socket = new Socket(address, port);
            Log.i("Client", "Client: socket=============");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Client getInstance() {
        if (instance == null) {
            synchronized (Client.class) {
                if (instance == null) {
                    return new Client();
                }
            }
        }
        return instance;
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
                outputStream.writeLong(idList.get(aLong).updateDate);
                if (inputStream.readInt() == 999) {
                    if (inputStream.readBoolean()) {
                        outputStream.writeUTF(idList.get(aLong).dirName);
                        if (inputStream.readUTF().equals("文件夹创建失败")) {
                            continue;
                        }
                        File dir = new File("/sdcard/Android/data/com.moment.whynote/files/Documents/" + idList.get(aLong).dirName);
                        String[] list = dir.list();
                        outputStream.writeInt(list.length);
                        for (File file : dir.listFiles()) {
                            util.sendFiles(outputStream, file);
                        }
                        isChange = true;
                    } else {
                        continue;
                    }
                } else {
                    outputStream.writeUTF(idList.get(aLong).dirName);
                    if (inputStream.readUTF().equals("文件夹创建失败")) {
                        continue;
                    }
                    File dir = new File("/sdcard/Android/data/com.moment.whynote/files/Documents/" + idList.get(aLong).dirName);
                    String[] list = dir.list();
                    outputStream.writeInt(list.length);
                    for (File file : dir.listFiles()) {
                        util.sendFiles(outputStream, file);
                    }
                    isChange = true;
                }

            }
            if (isChange) {
                System.out.println("数据库需要更新");
                System.out.println(inputStream.readUTF());
                File sqlDir = new File("/data/data/com.moment.whynote/databases/");
                outputStream.writeInt(sqlDir.list().length);

                if (sqlDir.exists()) {
                    File[] sqlFiles = sqlDir.listFiles();
                    for (File file : sqlFiles) {
//                    File sql = new File("/data/data/com.moment.whynote/databases/");
                        Log.d("Client", "execute: " + file.getName());;
                        util.sendFiles(outputStream, file);
                    }
                }
//                System.out.println(inputStream.readUTF());
            } else {
                System.out.println("数据库不需要更新");
            }

        } else {
            util.synchronizeFiles(inputStream, outputStream);
            File sqlDir = new File("/data/data/com.moment.whynote/databases/");
            outputStream.writeInt(sqlDir.listFiles().length);

            if (sqlDir.exists()) {
                File[] sqlFiles = sqlDir.listFiles();
                for (File file : sqlFiles) {
//                    File sql = new File("/data/data/com.moment.whynote/databases/");
                    Log.d("Client", "execute: " + file.getName());;
                    util.sendFiles(outputStream, file);
                }
            }
            System.out.println(inputStream.readUTF());
        }

//        util.downloadFiles(outputStream, inputStream);
//        outputStream.writeUTF("BYE");
        outputStream.close();
        inputStream.close();
        socket.close();
    }

}
