package com.moment.whynote.service;

import java.io.*;
import java.util.*;

public class DirAndFileUtil {

    private static final int serverNum = 1;
    private static final int clientNum = 2;
    private static final String SERVER = "server";
    private static final String CLIENT = "client";
    private final String NAME;




    public DirAndFileUtil(int choice) {
        if (choice == serverNum) NAME = SERVER;
        else if (choice == clientNum) NAME = CLIENT;
        else NAME = null;
    }

    /**
     * 同步文件至客户端
     *
     * @param outputStream 输出流
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void synchronizeFiles(DataInputStream inputStream, DataOutputStream outputStream) {

        int cnt;
        HashMap<String, Integer> map = new HashMap<>();
        HashMap<String, LinkedList<File>> dirAndFiles = new HashMap<>();
        String[] dirNames = new String[0];
        File dir = new File("/sdcard/Android/data/com.moment.whynote/files/Documents/");
        if (!dir.exists()) {
            dir.mkdirs();
        } else {

            dirNames = dir.list();
            assert dirNames != null;
            System.out.println("准备传送文件。。。。。" + dirNames.length);
            for (File d : Objects.requireNonNull(dir.listFiles())) {

                // 判断是否有该文件夹， 没有则创建
                if (dirAndFiles.getOrDefault(d.getName(), null) == null) {
                    dirAndFiles.put(d.getName(), new LinkedList<>());
                }
                // 读取所有文件到该Key的队列当中
                for (File file : Objects.requireNonNull(d.listFiles())) {
                    dirAndFiles.get(d.getName()).add(file);
                }
            }
        }

        try {
            // 要处理的文件夹数量
            outputStream.writeInt(dirNames.length);
            System.out.println("文件夹数量：" + dirNames.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String dirName : dirNames) {
            try {
                // 要创建的文件夹名
                outputStream.writeUTF(dirName);
                // 等待创建文件夹的信息
                String re = inputStream.readUTF();
                if (re.equals("文件夹创建失败")) continue;
                System.out.println(re);
                //要传送的文件数量
                outputStream.writeInt(dirAndFiles.get(dirName).size());

                while (dirAndFiles.get(dirName).peek() != null) {
                    if (sendFiles(outputStream, dirAndFiles.get(dirName).peek())) {
                        if (map.getOrDefault(Objects.requireNonNull(dirAndFiles.get(dirName).peek()).getName(), -1) != -1) {
                            map.remove(Objects.requireNonNull(dirAndFiles.get(dirName).peek()).getName());
                        }
                        dirAndFiles.get(dirName).poll();
                    } else {
                        // 失败再重试（不超过2次）
                        if (map.getOrDefault(Objects.requireNonNull(dirAndFiles.get(dirName).peek()).getName(), -1) == -1) {
                            map.put(Objects.requireNonNull(dirAndFiles.get(dirName).peek()).getName(), 2);
                            File f = dirAndFiles.get(dirName).poll();
                            dirAndFiles.get(dirName).offer(f);
                        } else if (map.get(Objects.requireNonNull(dirAndFiles.get(dirName).peek()).getName()) > 0) {
                            cnt = map.get(Objects.requireNonNull(dirAndFiles.get(dirName).peek()).getName());
                            map.put(Objects.requireNonNull(dirAndFiles.get(dirName).peek()).getName(), --cnt);
                            File f = dirAndFiles.get(dirName).poll();
                            dirAndFiles.get(dirName).offer(f);
                        } else {
                            File f = dirAndFiles.get(dirName).poll();
                            map.remove(Objects.requireNonNull(f).getName());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 批量处理
     *
     * @param inputStream 输入流
     */
    public void downloadFiles(DataOutputStream outputStream, DataInputStream inputStream) {
        try {
            int dirSum = inputStream.readInt(); // 获取要下载的文件夹数量
            System.out.println("文件夹数量：" + dirSum);
            for (int i = 0; i < dirSum; i++) {
                receiveFiles(outputStream, inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件接收
     *
     * @param inputStream 输入流
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void receiveFiles(DataOutputStream outputStream, DataInputStream inputStream) {
        try {
            //创建文件夹
            String dirName = inputStream.readUTF();
            File dir = new File("C:\\Users\\Linkdamo\\Desktop\\" + NAME + "\\", dirName);
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
                System.out.println(dirName);
            }
            if (dir.exists()) {
                outputStream.writeUTF("文件夹已创建");
            } else {
                outputStream.writeUTF("文件夹创建失败");
            }

            // 要处理的文件夹里文件的数量
            int fileSum = inputStream.readInt();
            System.out.println("文件数量：" + fileSum);
            for (int i = 0; i < fileSum; i++) {
                byte[] bytes = new byte[512]; // 缓冲池
                int len = inputStream.readInt(); // 文件长度
                System.out.println("real size = " + len);
                String fileName = inputStream.readUTF(); // 文件名称
                File file = new File("C:\\Users\\Linkdamo\\Desktop\\" + NAME + "\\" + dirName + "\\", fileName); //保存路径
                if (!file.exists()) {
                    file.createNewFile(); // 为接收文件创建文件
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                while (len > 0) { // 检测是否传输完成
                    int temp = inputStream.read(bytes);
                    fileOutputStream.write(bytes);
                    len -= temp;
                }
                fileOutputStream.close(); // 关闭文件输出流，结束该文件传输
            }
        } catch (Exception e) {
            System.out.println("Failed to receive files" + e);
        }

    }

    /*
send files
*/
    public boolean sendFiles(DataOutputStream outputStream, File file) {
        try {
//            File file = new File("C:\\Users\\Linkdamo\\Desktop\\证明2.jpg");
            if (file.exists()) {
                System.out.println("文件存在！");
                byte[] bytes = new byte[512];
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

                int len = bufferedInputStream.available();
                outputStream.writeInt(len);
                outputStream.writeUTF(file.getName());
                while (len > 0) {
                    int temp = bufferedInputStream.read(bytes);
                    len -= temp;
                    outputStream.write(bytes);
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("Failed to send files" + e);
            return false;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean getSQL(DataInputStream inputStream) {
        try {
            byte[] bytes = new byte[512]; // 缓冲池
            int len = inputStream.readInt(); // 文件长度
            System.out.println("real size = " + len);
            String fileName = inputStream.readUTF(); // 文件名称
            File file = new File("C:\\Users\\Linkdamo\\Desktop\\" + NAME + "\\database\\", fileName); //保存路径
            if (!file.exists()) {
                file.createNewFile(); // 为接收文件创建文件
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            while (len > 0) { // 检测是否传输完成
                int temp = inputStream.read(bytes);
                fileOutputStream.write(bytes);
                len -= temp;
            }
            fileOutputStream.close(); // 关闭文件输出流，结束该文件传输
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
