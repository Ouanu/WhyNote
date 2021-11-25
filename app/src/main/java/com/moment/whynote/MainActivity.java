package com.moment.whynote;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;
import com.moment.whynote.fragment.InsertFragment;
import com.moment.whynote.fragment.ResFragment;
import com.moment.whynote.utils.DataUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements InsertFragment.DialogListener{

    private ResRepository repository;
    private final MainHandler handler = new MainHandler();
    private final static String TAG = "MainActivity";
    private final static int DATABASE_IS_ALREADY = 10000;
    private DataUtils utils = new DataUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        getResRepository();
    }

    /**
     * 获取仓库实例
     */
    private void getResRepository() {
        new Thread(() -> {
            repository = new ResRepository(MainActivity.this);
            if (ResRepository.getInstance() != null) {
                handler.sendEmptyMessage(DATABASE_IS_ALREADY);
            }
        }).start();
    }

    /**
     * 获得fragment布局
     */
    private void getFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fl_fragment);
        if (currentFragment == null) {
            ResFragment fragment = new ResFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_fragment, fragment)
                    .commit();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        FrameLayout flFragment = (FrameLayout) findViewById(R.id.fl_fragment);
        ImageButton insertBtn =  findViewById(R.id.insert_btn);
        insertBtn.setOnClickListener(v -> new Thread(() -> {
            InsertFragment fragment = new InsertFragment();
            fragment.show(getSupportFragmentManager(), "INSERT_FRAGMENT");
        }).start());
    }

    /**
     * 线程间通信，按指定顺序完成作业
     */
    @SuppressLint("HandlerLeak")
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == DATABASE_IS_ALREADY) {
                Log.d(TAG, "handleMessage: -------------");
                getFragment();
            }
        }
    }

    @Override
    public void sendValue(String str) {
        Log.d(TAG, "sendValue: " + str);
        List<String> res = utils.getUris(str);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ResData data = new ResData();
                repository = ResRepository.getInstance();
                data.title = utils.getNowDateDefault();
                for (String re : res) {
                    Log.d(TAG, "run: ---------" + re);
                    data.desc += re + "\r\n";
                }
                repository.insertData(data);
            }
        }).start();

    }
}