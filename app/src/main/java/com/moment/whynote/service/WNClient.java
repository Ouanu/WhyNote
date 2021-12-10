package com.moment.whynote.service;


import android.annotation.SuppressLint;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class WNClient {
    private static WNClient mClient = null;
//    private static final String TAG = "WNClient.class";
//    private Socket socket;
//    private OutputStream os;

    public WNClient(String ipAddress, int port) throws IOException {
        Socket socket = new Socket(ipAddress, port);
        OutputStream os = socket.getOutputStream();
        PrintWriter pw = new PrintWriter(os);
//        获取客户端ip地址
        InetAddress address = InetAddress.getLocalHost();
        String ip = address.getHostAddress();
        pw.write("客户端：" + ip + "接入服务器");
        pw.flush();
//        socket.shutdownOutput();
        sendFile(socket);
        socket.close();
    }

    public static WNClient getInstance(String ipAddress, int port) throws IOException {
        if (mClient == null) {
            synchronized (WNClient.class) {
                mClient = new WNClient(ipAddress, port);
            }
        }
        return mClient;
    }

    public void sendFile(Socket socket) {
//        File[] fileList = getFileList();
//        Log.d(TAG, "sendFile: " + fileList.length);
        try {
//            for (File file : fileList) {
            // 获得需要传输的文件
            @SuppressLint("SdCardPath")
            FileInputStream fs = new FileInputStream(new File("/data/data/com.moment.whynote/databases/RES_DATABASE.db"));
            // 创建本地流
            BufferedInputStream bi = new BufferedInputStream(fs);

            // 网络上的流
            OutputStream os = socket.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);

            // 传到服务区
            int data;
            while ((data = bi.read()) != -1) {
                bos.write(data);
            }
            bos.flush();

            // 给服务器一个结束标志，表示数据已经传输完毕
            socket.shutdownOutput();

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String str;
            while ((str = br.readLine()) != null) {
                System.out.println(str);
            }
            // 释放资源
            os.close();
            bi.close();

            socket.close();
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public File[] getFileList() {
        @SuppressLint("SdCardPath")
        File file = new File("/data/data/com.moment.whynote/databases");        //获取其file对象
        File[] fs = file.listFiles();    //遍历path下的文件和目录，放在File数组中
        assert fs != null;
        for (File f : fs) {                    //遍历File[]数组
            if (!f.isDirectory())        //若非目录(即文件)，则打印
                System.out.println(f);
        }
        return fs;
    }

}
