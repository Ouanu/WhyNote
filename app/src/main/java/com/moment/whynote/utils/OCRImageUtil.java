package com.moment.whynote.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.moment.whynote.ml.Model;

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
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.drawContours;


public class OCRImageUtil {
    private static Context mContext = null;
    private LinkedList<Mat> matQueue = new LinkedList<>();
    private volatile static OCRImageUtil instance = null;
    private Model model = null;
    private TensorBuffer inputFeature0;
    private Model.Outputs outputs;

    public static OCRImageUtil getInstance() {
        if (instance == null) {
            synchronized (OCRImageUtil.class) {
                if (instance == null) {
                    return new OCRImageUtil(mContext);
                }
            }
        }
        return instance;
    }

    public OCRImageUtil(Context mContext) {
        this.mContext = mContext;
        new Thread(()->{
            try {
                model = Model.newInstance(mContext);

                // Creates inputs for reference.
                inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 30, 30, 1}, DataType.FLOAT32);
//                inputFeature0.loadBuffer(byteBuffer);

                // Runs model inference and gets result.
//                Model.Outputs outputs = model.process(inputFeature0);
//                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                // Releases model resources if no longer used.
//            model.close();
            } catch (IOException e) {
                // TODO Handle the exception
            }
        }).start();

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
