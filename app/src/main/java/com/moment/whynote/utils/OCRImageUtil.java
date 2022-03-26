package com.moment.whynote.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;

import android.net.Uri;

import android.provider.MediaStore;


import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.IOException;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.HoughLines;
import static org.opencv.imgproc.Imgproc.LINE_AA;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
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
//        fastNlMeansDenoisingColored(rgb,mat,3,3,7,21);

        Imgproc.cvtColor(rgb, gray, COLOR_RGB2GRAY);
        Mat can = new Mat();

        //二值化
        threshold(gray, gray, 100, 255, THRESH_BINARY);

        // 边缘提取+增强边缘
        Imgproc.Canny(gray, can, 50, 200, 3);
        Imgproc.Sobel(can, can, CV_8U, 1, 1,7);


        //霍夫直线检测 (图片矫正)
        Mat lines = new Mat();
        HoughLines(can, lines, 1, Math.PI / 180, 200, 0, 0);
        double sum = 0;
        double angle = 0;
        for (int i = 0; i < lines.rows(); i++) {
            double[] vec = lines.get(i, 0);
            double rho = vec[0];
            double theta = vec[1];
            Point pt1 = new Point(), pt2 = new Point();
            double a = Math.cos(rho), b = Math.sin(theta);
            double x = a * rho, y = b * theta;
            pt1.x = Math.round(x + 1000 * (-b));
            pt1.y = Math.round(y + 1000 * (a));
            pt2.x = Math.round(x - 1000 * (-b));
            pt2.y = Math.round(y - 1000 * (a));
            sum += theta;
            line(gray, pt1, pt2, new Scalar(55, 100, 195), 1, LINE_AA);
            double average = sum / lines.rows();
            angle = Math.toDegrees(average) - 90;
        }

        Point center = new Point();
        center.x = rgb.cols() / 2.0;
        center.y = rgb.rows() / 2.0;
        int length = (int) Math.sqrt(rgb.cols()*rgb.cols() + rgb.rows()*rgb.rows());
        System.out.println(angle);
        Mat m = getRotationMatrix2D(center, angle, 1);
        Mat src_rotate = new Mat();
        //仿射变换，背景色填充为白色
        warpAffine(rgb, src_rotate, m, new Size(length, length), 1, 0, new Scalar(255, 255, 255));

        Bitmap finalbitmap = Bitmap.createBitmap(src_rotate.width(), src_rotate.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src_rotate, finalbitmap);
        return finalbitmap;

    }

}
