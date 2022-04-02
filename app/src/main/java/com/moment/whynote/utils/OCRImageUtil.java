package com.moment.whynote.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.net.Uri;

import android.provider.MediaStore;


import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.HoughLines;
import static org.opencv.imgproc.Imgproc.LINE_AA;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;
import static org.opencv.imgproc.Imgproc.getRotationMatrix2D;
import static org.opencv.imgproc.Imgproc.line;
import static org.opencv.imgproc.Imgproc.threshold;
import static org.opencv.imgproc.Imgproc.warpAffine;


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


    /**
     * 执行任务
     *
     * @param contentResolver 获取内容解析者
     * @param uri             获取文件链接
     */
    @SuppressLint("SdCardPath")
    public String execute(ContentResolver contentResolver, Uri uri) {
        Bitmap bitmap;
        String text = "";
        try {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
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
//        //降噪
//        fastNlMeansDenoisingColored(rgb,rgb,3,3,7,21);

        // 图片锐化
        List<Mat> rgbList = new ArrayList<>();
        Core.split(rgb, rgbList);

        for (int i = 0; i < rgbList.size(); i++) {
            Mat can = new Mat();
            Mat finalMat = new Mat();
            System.out.println("++++++++++++++++");
            Imgproc.GaussianBlur(rgbList.get(i), can, new Size(3, 3), 0, 0);
            Imgproc.Laplacian(can, finalMat, CV_8U);
            Core.add(rgbList.get(i), finalMat, rgbList.get(i));
        }
        Core.merge(rgbList, rgb);

        Imgproc.cvtColor(rgb, gray, COLOR_RGB2GRAY);
        //二值化
        threshold(gray, gray, 150, 255, THRESH_BINARY + THRESH_OTSU);

        Bitmap finalbitmap = Bitmap.createBitmap(gray.width(), gray.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(gray, finalbitmap);
        return finalbitmap;
    }

}
