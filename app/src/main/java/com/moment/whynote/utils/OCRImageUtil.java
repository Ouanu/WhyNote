package com.moment.whynote.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;

import static org.opencv.imgproc.Imgproc.threshold;


public class OCRImageUtil {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext = null;
    @SuppressLint("StaticFieldLeak")
    private static final OCRImageUtil instance = null;

    /**
     * 获取单例
     *
     * @return 返回实例
     * @throws IOException
     */
    @SuppressWarnings("all")
    public static OCRImageUtil getInstance() throws IOException {
        if (instance == null) {
            synchronized (OCRImageUtil.class) {
                if (instance == null) {
                    return new OCRImageUtil(mContext);
                }
            }
        }
        return instance;
    }

    /**
     * 初始化OCRImageUtil
     */
    public OCRImageUtil(Context mContext) {
        OCRImageUtil.mContext = mContext;
    }


    @SuppressLint("SdCardPath")
    public String execute(Bitmap bitmap) {
        String text = "";
        try {
//            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
            bitmap = pretreatment(bitmap);
            TessBaseAPI tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.init("/sdcard/Android/data/com.moment.whynote/files/tesseract/", "myocr");
            tessBaseAPI.setImage(bitmap);
            text = tessBaseAPI.getUTF8Text();
            tessBaseAPI.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }


    /**
     * 图片预处理
     */
    private Bitmap pretreatment(Bitmap bitmap) {
        Mat rgb = new Mat();
        Mat gray = new Mat();
        Utils.bitmapToMat(bitmap, rgb);

        // 图片锐化
        List<Mat> rgbList = new ArrayList<>();
        Core.split(rgb, rgbList);

        for (int i = 0; i < rgbList.size(); i++) {
            Mat can = new Mat();
            Mat finalMat = new Mat();
            Imgproc.GaussianBlur(rgbList.get(i), can, new Size(3, 3), 0, 0);
            Imgproc.Laplacian(can, finalMat, CV_8U);
            Core.add(rgbList.get(i), finalMat, rgbList.get(i));
        }
        Core.merge(rgbList, rgb);

        Imgproc.cvtColor(rgb, gray, COLOR_RGB2GRAY);
        //二值化
        threshold(gray, gray, 150, 255, THRESH_BINARY + THRESH_OTSU);

        Bitmap finalBitmap = Bitmap.createBitmap(gray.width(), gray.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(gray, finalBitmap);
        return finalBitmap;
    }

}
