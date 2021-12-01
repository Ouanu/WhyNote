package com.moment.whynote;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;
import com.moment.whynote.fragment.DetailFragment;
import com.moment.whynote.fragment.InsertFragment;
import com.moment.whynote.fragment.ResFragment;
import com.moment.whynote.utils.DataUtils;

import org.jetbrains.annotations.NotNull;


public class MainActivity extends AppCompatActivity implements InsertFragment.DialogListener, ResFragment.ResListener {

    private ResRepository repository;
    private MainHandler handler;
    private final static String TAG = "MainActivity";
    private final static int DATABASE_IS_ALREADY = 10000;
    private final DataUtils utils = new DataUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new MainHandler(this.getMainLooper());
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
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setCustomAnimations(R.anim.from_right, R.anim.from_left)
                    .add(R.id.fl_fragment, fragment)
                    .commit();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
//        ImageButton insertBtn = findViewById(R.id.insert_btn);
//        insertBtn.setOnClickListener(v -> new Thread(() -> {
//            InsertFragment fragment = new InsertFragment();
//            fragment.show(getSupportFragmentManager(), "INSERT_FRAGMENT");
//        }).start());

    }

    @Override
    public void onFragmentSelected(Bundle bundle) {
        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setCustomAnimations(R.anim.from_right, R.anim.from_left,R.anim.to_left, R.anim.to_right)
                .replace(R.id.fl_fragment, detailFragment)
                .addToBackStack("detailFragment")
                .commit();
    }

    /**
     * 线程间通信，按指定顺序完成作业
     */
    @SuppressLint("HandlerLeak")
    private class MainHandler extends Handler {
        public MainHandler(@NonNull @NotNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == DATABASE_IS_ALREADY) {
                Log.d(TAG, "handleMessage: -------------");
                getFragment();
            }
        }
    }

    @Override
    public void sendValue(String title, String str) {
        new Thread(() -> {
            ResData data = new ResData();
            repository = ResRepository.getInstance();
            if(title == null || title.equals("") || title.isEmpty()) {
                data.updateDate = System.currentTimeMillis();
                data.title = utils.getNowDateDefault(data.updateDate);
            } else {
                data.title = title;
            }
            data.desc = str;
            repository.insertData(data);
        }).start();

    }
}