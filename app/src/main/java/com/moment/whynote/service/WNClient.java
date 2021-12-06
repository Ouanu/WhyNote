package com.moment.whynote.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class WNClient {
    private static WNClient mClient = null;
//    private Socket socket;
//    private OutputStream os;


    public WNClient() throws IOException {
        Socket socket = new Socket("", 12345);
        OutputStream os = socket.getOutputStream();
        PrintWriter pw = new PrintWriter(os);
        //获取客户端ip地址
        InetAddress address = InetAddress.getLocalHost();
        String ip = address.getHostAddress();
        pw.write("客户端：" + ip + "接入服务器");
        pw.flush();
        socket.shutdownOutput();
        socket.close();
    }

    public static WNClient getInstance() throws IOException {
        if(mClient == null){
            synchronized (WNClient.class) {
                mClient = new WNClient();
            }
        }
        return mClient;
    }

}
