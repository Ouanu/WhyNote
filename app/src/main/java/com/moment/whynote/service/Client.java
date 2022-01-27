package com.moment.whynote.service;

import android.annotation.SuppressLint;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class Client {
    private final static String address = "127.0.0.1";
    private final static int port = 7290;
    private static volatile Client instance;
    private DataInputStream dis;
    private DataOutputStream dos;
    private OutputStream os;
    private InputStream is;
    private final Queue<File> fileQueue = new LinkedList<>();
//    private Socket socket;

    public static Client getInstance() {
        if (instance == null) {
            synchronized (Client.class) {
                if (instance == null) {
                    instance = new Client();
                }
            }
        }
        return instance;
    }

    public Client() {
//        try {
//            socket = new Socket(address, port);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void insertFiles(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        assert null != files;
        fileQueue.addAll(Arrays.asList(files));
    }

    public void upload() {
        try {
            dos.writeInt(fileQueue.size());
            int cnt = 3;
            while (null != fileQueue.peek()) {
                try {
                    // send file's name
                    System.out.println(Objects.requireNonNull(fileQueue.peek()).getName());
                    dos.writeUTF(Objects.requireNonNull(fileQueue.peek()).getName());
                    // get the message from server
                    FileInputStream fis = new FileInputStream(Objects.requireNonNull(fileQueue.peek()).getAbsolutePath());
                    dos.writeInt(fis.available());
                    os.flush();
                    byte[] bytes;
                    int size;
                    while (true) {
                        bytes = new byte[1024];
                        size = fis.read(bytes);
                        if (size == -1) {
                            break;
                        }
                        dos.writeInt(size);
                        os.write(bytes);
                        dis.readUTF();
                        os.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    /*
                      失败重传，少于三次
                     */
                    if (cnt > 0) {
                        fileQueue.add(fileQueue.peek());
                        cnt--;
                    }
                } finally {
                    fileQueue.poll();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    upload folders first, then files
     */
    public void uploadFolder(File[] folder) {
        try {
            dos.writeUTF("#1F33d8k#");
            dos.writeInt(folder.length);
            for (File f : folder) {
                dos.writeUTF(f.getName());
                System.out.println(dis.readUTF());
                insertFiles(f.getAbsolutePath());
                upload();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void synchroServerData() {
        downloadFolders();
    }


    public void executeTask(Socket socket) {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            is = socket.getInputStream();
            os = socket.getOutputStream();
//            dos.writeUTF("#3K55c7v#");
//            synchroServerData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    创建文件夹
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void downloadFolders() {
        try {
            int size = dis.readInt();
            System.out.println(size);
            while (size > 0) {
                @SuppressLint("SdCardPath")
                File folder = new File("/sdcard/Android/data/com.moment.whynote/files/Documents", dis.readUTF());
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                dos.writeUTF(folder.getName() + "已创建");
                downloadFiles(folder);
                size--;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    下载文件夹中的所有文件
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void downloadFiles(File folder) {
        try {
            int sizeOfFiles = dis.readInt();
            while (sizeOfFiles > 0) {
                File file = new File(folder, dis.readUTF());
                FileOutputStream fos = new FileOutputStream(file);
                int size = dis.readInt();
                byte[] bytes;
                while (size > 0) {
                    size -= dis.readInt();
                    bytes = new byte[1024];
                    is.read(bytes);
                    fos.write(bytes);
                    dos.writeUTF("N");
                }
                fos.close();
                dos.flush();
                sizeOfFiles--;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public static void main(String[] args) {
//        Client client = getInstance();
//        try {
//            Socket socket = new Socket(address, port);
//            client.executeTask(socket);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
