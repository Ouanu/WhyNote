package com.moment.whynote.service;

import android.icu.util.Output;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class WNClient {
    private static WNClient mClient = null;
//    private Socket socket;
//    private OutputStream os;

    public WNClient(String ipAddress, int port) throws IOException {
        Socket socket = new Socket(ipAddress, port);
//        OutputStream os = socket.getOutputStream();
//        PrintWriter pw = new PrintWriter(os);
        //获取客户端ip地址
//        InetAddress address = InetAddress.getLocalHost();
//        String ip = address.getHostAddress();
//        pw.write("客户端：" + ip + "接入服务器");
//        pw.flush();
//        socket.shutdownOutput();
        sendFile(socket);
        socket.close();
    }

    @SuppressWarnings("All")
    public static WNClient getInstance(String ipAddress, int port) throws IOException {
        if (mClient == null) {
            synchronized (WNClient.class) {
                mClient = new WNClient(ipAddress, port);
            }
        }
        return mClient;
    }

    public void sendFile(Socket socket) {
        try {
            // 获得需要传输的文件
            FileInputStream fs = new FileInputStream("/data/data/com.moment.whynote/databases");
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
            while ((str = br.readLine()) != null){
                System.out.println(str);
            }

            // 释放资源
            socket.close();
            os.close();
            bi.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
