package com.moment.whynote.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControlService extends Service{

    private Client client;
    private static final String TAG = "ControlService.class";
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<ResData> resDataList;
    // 标识服务被杀死后的行为
    int mStartMode;

    IBinder mBinder;

    boolean mAllowRebind;

    // 服务被创建时调用
    @Override
    public void onCreate() {
        super.onCreate();
    }

    // 调用 startService() 启动服务时调用
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ArrayList list = intent.getExtras().getParcelableArrayList("LIST");
        resDataList = (List<ResData>) list.get(0);
//        executor.execute(()->client = Client.getInstance());
        executor.execute(()->{
            try {
                client = Client.getInstance();
                client.execute(resDataList);
                Log.i(TAG, "onStartCommand: 正常运行");
//                Toast.makeText(this, "onStartCommand: 正常运行", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "onStartCommand: 运行失败");
//                Toast.makeText(this, "onStartCommand: 运行失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 通过unbindService() 解除所有客户端绑定时调用

    // 服务不再有用且将要被摧毁时调用
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: 结束运行");
    }

}
