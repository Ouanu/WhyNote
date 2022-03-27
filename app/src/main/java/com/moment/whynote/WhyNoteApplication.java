package com.moment.whynote;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.moment.whynote.database.ResRepository;
import com.moment.whynote.utils.OCRImageUtil;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void getOCRImageUtil(){
        executor.execute(()->{
            // 将模型加载到目录中
            try {
                InputStream inputStream = getAssets().open("myocr.traineddata");
                @SuppressLint("SdCardPath") File dir = new File(getString(R.string.tesseract));
                if (!dir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdirs();
                }
                File file = new File(dir, "myocr.traineddata");
                if (!file.exists()) {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    byte[] b = new byte[inputStream.available()];
                    inputStream.read(b);
                    fileOutputStream.write(b);
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            OCRImageUtil ocrImageUtil = new OCRImageUtil(this);
            Log.i(TAG, "OCR ImageUtil is ready." + ocrImageUtil.hashCode());
        });
    }
}
