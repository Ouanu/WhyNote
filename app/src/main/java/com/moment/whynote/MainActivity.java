package com.moment.whynote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.moment.whynote.fragment.ConnectFragment;
import com.moment.whynote.fragment.DetailFragment;
import com.moment.whynote.fragment.ResFragment;
import com.moment.whynote.service.ControlService;


import java.io.File;


public class MainActivity extends AppCompatActivity implements ResFragment.ResListener, ConnectFragment.ConnectListener {

    // 设置文件
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 222);
        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        PrepareWork();
        getFragment();


    }



    /**
     * 创建资源文件夹
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void PrepareWork() {
        new Thread(()->{
            @SuppressLint("SdCardPath") File dcimDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
            if (!dcimDir.exists() || !dcimDir.isDirectory())
                dcimDir.mkdirs();
            if(!dcimDir.exists()){
                Toast.makeText(this, "读写权限获取失败，授权后才可正常使用。", Toast.LENGTH_SHORT).show();
            }
            // 若是没有获取布局key， 则创建并设置值为0（默认线性布局）
            if(sharedPreferences.getInt("LayoutManager", -1) == -1) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("LayoutManager", 0);
                editor.putString("updateTime", String.valueOf(System.currentTimeMillis()));
                editor.apply();
            }
        }).start();

    }





    /**
     * 获得fragment布局
     */
    private void getFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fl_fragment);
        Bundle bundle = new Bundle();
        bundle.putInt("LayoutManager", sharedPreferences.getInt("LayoutManager", 0));
        if (currentFragment == null) {
            ResFragment fragment = new ResFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_fragment, fragment)
                    .commit();
        }
    }

    @Override
    public void onFragmentSelected(Bundle bundle) {
        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setCustomAnimations(R.anim.show_slide, R.anim.no_slide, R.anim.show_slide, R.anim.no_slide)
                .replace(R.id.fl_fragment, detailFragment)
                .addToBackStack("detailFragment")
                .commit();
    }

    @Override
    public void onConnectSelected(Bundle bundle) {
        if (bundle.getString("command").equals("start")) {
            Intent start = new Intent(this, ControlService.class);
            start.putExtra("ip", bundle.getString("ip"));
            start.putExtra("port", bundle.getInt("port"));
            start.putExtra("updateTime", bundle.getString("updateTime"));
            startService(start);
        }
//         else if (bundle.getString("command").equals("finish")) {
//
//        }


    }


}