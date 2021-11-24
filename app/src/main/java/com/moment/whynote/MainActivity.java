package com.moment.whynote;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;
import com.moment.whynote.fragment.ResFragment;

public class MainActivity extends AppCompatActivity {

    private ResRepository repository;
    private FrameLayout flFragment;
    private Button insertBtn;
    private MainHandler handler = new MainHandler();
    private final static String TAG = "MainActivity";
    private final static int DATABASE_IS_ALREADY = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                repository = new ResRepository(MainActivity.this);
//                repository.insertData();
                if (ResRepository.getInstance() != null) {
                    handler.sendEmptyMessage(DATABASE_IS_ALREADY);
                }
//
            }
        }).start();
        initView();


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

    private void initView() {
        flFragment = (FrameLayout) findViewById(R.id.fl_fragment);
        insertBtn = (Button) findViewById(R.id.insert_btn);
        insertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        repository = ResRepository.getInstance();
                        ResData resData = new ResData();
                        resData.title = "SAD";
                        resData.desc = "SAD";
                        resData.uri = "SDADSADA";
                        repository.insertData(resData);
                    }
                }).start();

            }
        });
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
            switch (msg.what) {
                case DATABASE_IS_ALREADY:
                    Log.d(TAG, "handleMessage: -------------");
                    getFragment();
                    break;
                default:
                    break;
            }
        }
    }
}