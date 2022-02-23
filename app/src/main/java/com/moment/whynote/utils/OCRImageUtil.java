package com.moment.whynote.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.drawContours;


public class OCRImageUtil {
    private Context mContext;
    private LinkedList<Mat> matQueue = new LinkedList<>();

    public OCRImageUtil(Context mContext) {
        this.mContext = mContext;
    }

    public void execute(Uri uri){
        new Thread(()->{
            proSrc2Gray(uri);
        }).start();
    }

    public void proSrc2Gray(Uri uri){
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Mat bin = new Mat();
        Bitmap srcBitmap = null;
        try {
            srcBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
            //对图片进行灰度化
            Utils.bitmapToMat(srcBitmap, rgbMat);
            Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
            Mat sobel = new Mat();
            Imgproc.Sobel(grayMat, sobel, CvType.CV_8U, 0, 1, 3, 1, 0, Core.BORDER_REPLICATE);
            Core.bitwise_not(sobel, sobel);
            // 利用OTSU二值化，将文本行与背景分割
            double ret = Imgproc.threshold(sobel, bin, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            //通过形态学腐蚀，将分割出来的文字字符连接在一起
            Mat rec = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 30), new Point(0,0));
            Mat dilate = new Mat();
            Imgproc.erode(bin, dilate, rec);
            Mat erode = new Mat();
            Core.bitwise_not(dilate, erode);
            // 提取文本行所在轮廓
            List<MatOfPoint> counts = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(erode, counts, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_L1, new Point(0, 0));
            drawContours(erode, counts, -1, new Scalar(255, 0, 255), 1);
            for (int i = 0; i < counts.size(); i++) {
                if (Imgproc.contourArea(counts.get(i)) < 500)
                    continue;
                Rect rect = boundingRect(counts.get(i));
                matQueue.add(grayMat.submat(rect));
            }
        } catch (IOException e) {
            Toast.makeText(mContext, "The Uri of image is wrong.", Toast.LENGTH_SHORT).show();
        }

    }
}
