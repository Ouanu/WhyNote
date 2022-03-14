package com.moment.whynote;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.moment.whynote.database.ResRepository;
import com.moment.whynote.utils.OCRImageUtil;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WhyNoteApplication extends Application {
    private static final String TAG = "WhyNoteApplication";

    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();
        getResRepository();
        initLoadOpenCV();
        getOCRImageUtil();
    }

    /**
     * 获取仓库实例
     */
    private void getResRepository() {
        ResRepository repository = new ResRepository(this);
        Log.i(TAG, "Database is ready." + repository.hashCode());
    }

    /**
     * 初始化opencv
     */
    private void initLoadOpenCV() {
        boolean success = OpenCVLoader.initDebug();
        if (success) {
            Toast.makeText(this.getApplicationContext(), "Loading Opencv Libraries", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.getApplicationContext(), "WARNING：COULD NOT LOAD Opencv Libraries!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 加载OCR模型及图片处理工具
     */
    private void getOCRImageUtil(){
        executor.execute(()->{
            try {
                OCRImageUtil ocrImageUtil = new OCRImageUtil(this);
                Log.i(TAG, "OCR ImageUtil is ready." + ocrImageUtil.hashCode());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
