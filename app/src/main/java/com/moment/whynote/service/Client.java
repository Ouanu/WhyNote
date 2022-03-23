package com.moment.whynote.service;



import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;


public class Client {

    private int port = 9250;
    private String address = "192.168.137.1";
    private Socket socket;
    private ResRepository repository = ResRepository.getInstance();

    public Client() {
        try {
            socket = new Socket(address, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute() throws IOException {
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        DirAndFileUtil util = new DirAndFileUtil(2);
        HashMap<Long, ResData> idList = new HashMap<>();
        for (int i = 0; i < repository.getAllResData().getValue().size(); i++) {
            idList.put(Long.valueOf(repository.getAllResData().getValue().get(i).uid), repository.getAllResData().getValue().get(i));
        }


        if (socket.isConnected()) {
            outputStream.writeUTF("Hello there!!");
            String s = inputStream.readUTF();
            System.out.println(s);
            outputStream.writeInt(34567);
            s = inputStream.readUTF();
            System.out.println(s);

        }


        if (inputStream.readLong() == 1000l) {
            System.out.println("有数据库");
            boolean isChange = false;
            outputStream.writeInt(idList.size());
            for (Long aLong : idList.keySet()) {
                outputStream.writeLong(aLong);
                outputStream.writeLong(idList.get(aLong).updateDate);
                if (inputStream.readBoolean()) {
                    outputStream.writeUTF(idList.get(aLong).dirName);
                    if (inputStream.readUTF().equals("文件夹创建失败")) {
                        continue;
                    }
                    File dir = new File("C:\\Users\\Linkdamo\\Desktop\\client\\" + idList.get(aLong).dirName);
                    String[] list = dir.list();
                    outputStream.writeInt(list.length);
                    for (File file : dir.listFiles()) {
                        util.sendFiles(outputStream, file);
                    }
                    isChange = true;
                } else {
                    continue;
                }
            }
            if (isChange) {
                System.out.println("数据库需要更新");
                System.out.println(inputStream.readUTF());
                File sql = new File("C:\\Users\\Linkdamo\\Desktop\\client\\database\\RES_DATABASE.db");
                util.sendFiles(outputStream, sql);
            } else {
                System.out.println("数据库不需要更新");
            }

        } else {
            util.synchronizeFiles(inputStream, outputStream);
            System.out.println(inputStream.readUTF());
        }



//        util.downloadFiles(outputStream, inputStream);
        outputStream.writeUTF("BYE");
        outputStream.close();
        inputStream.close();
        socket.close();
    }

}
