package com.moment.whynote.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.Socket;

public class ControlService extends Service {
    private final static String TAG = "ControlService";
    private static final int BUNDLE_IS_NULL = 200001;
    private static final int CONNECT_SERVER = 200002;
    private static final int DISCONNECT_SERVER = 200003;
    private static final int SOCKET_CONNECT = 200011;
    private Bundle bundle;
    private final ControlBinder binder = new ControlBinder();
    private Socket socket;
    private final ServerHandler handler = new ServerHandler();
    private WNClient client;
    private static String updateTime;

    /**
     * 提供数据交换接口
     */
    public class ControlBinder extends Binder {
        public ControlService getService() {
            return ControlService.this;
        }
    }


    @Override
    public void onCreate() {
        Log.d(TAG, "we in.........");
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        bundle = intent.getExtras();
        Log.d(TAG, "onBind: ===============");
        if (!bundle.isEmpty()) {
            if (bundle.getString("command").equals("start")) {
                updateTime = bundle.getString("updateTime");
                handler.sendEmptyMessage(CONNECT_SERVER);
            } else if (bundle.getString("command").equals("quit")) {
                handler.sendEmptyMessage(DISCONNECT_SERVER);
            }
        } else {
            handler.sendEmptyMessage(BUNDLE_IS_NULL);
        }
        return binder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: -------------");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: service disconnect");
        super.onDestroy();
    }

    /**
     * 控制客户端的所有操作
     * （包括但不限于连接、传输数据库、传输图片等）
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("HandlerLeak")
    private class ServerHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case CONNECT_SERVER:
                    connectServer();    // 连接server
                    break;
                case DISCONNECT_SERVER:
                    disconnectServer(); // 断开server
                    break;
                case BUNDLE_IS_NULL:
                    Toast.makeText(ControlService.this, "连接错误", Toast.LENGTH_SHORT).show();
                    break;
                case SOCKET_CONNECT:
                    checkUpdate(updateTime);    // 检查是否需要更新
                default:
                    break;
            }
        }
    }

    /**
     * 检查修改数据库修改时间，一致则不更新，节省资源
     * @param updateTime 更新时间
     */
    private void checkUpdate(String updateTime) {
        if (!bundle.getString("updateTime").equals(updateTime)) {
            if(null != client && null != socket && socket.isConnected()) {
                new Thread(()->{
                    if (client.PrepareWork()) {
                        client.PushDatabase();
                        client.PushImageCache();
                    }
                }).start();
            }
        }
    }

    /**
     * 断开连接
     */
    private void disconnectServer() {
        if (null != socket && socket.isConnected()) {
            try {
                client = null;
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建线程连接
     */
    private void connectServer() {
        new Thread(() -> {
            try {
                socket = new Socket(bundle.getString("ip"), 7290);
                client = WNClient.getInstance(socket);
                if (socket.isConnected()) {
                    Log.d(TAG, "connectServer: socket 已连接");
                    handler.sendEmptyMessage(SOCKET_CONNECT);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}