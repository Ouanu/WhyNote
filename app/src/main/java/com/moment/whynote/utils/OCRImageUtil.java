package com.moment.whynote.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

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
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.resize;


public class OCRImageUtil {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext = null;
    private LinkedList<Mat> matQueue = new LinkedList<>();
    private LinkedList<Bitmap> bitmaps = new LinkedList<>();
    private static boolean workState = true;
    private volatile static OCRImageUtil instance = null;
    private Model model = null;
    private MappedByteBuffer tfliteModel = null;
    private TensorBuffer inputFeature0;
    private Model.Outputs outputs;
    private static final float[][][][] imageMat = new float[1][30][30][1];
    private ImageProcessor processor = null;
    private TensorImage tImage = null;
    private Interpreter tflite;

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
        try {
            model = Model.newInstance(mContext);

            // Creates inputs for reference.
            inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 30, 30, 1}, DataType.FLOAT32);

//            inputFeature0.loadBuffer(byteBuffer);
            // Runs model inference and gets result.
//                Model.Outputs outputs = model.process(inputFeature0);
//                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
//            model.close();

            Log.d("OCRImageUtil:", "The image util is ready. " + inputFeature0.getShape().length);
        } catch (IOException e) {
            // TODO Handle the exception
            System.out.println(e);
        }
    }



    public void execute(ContentResolver contentResolver, Uri uri){
        new Thread(()->{
            proSrc2Gray(contentResolver, uri);
            ocr();
        }).start();
//        new Thread(()->{
//            ocr();
//        }).start();
    }

    private void ocr() {
        inputFeature0.loadBuffer(getMatFloat());
        Model.Outputs outputs = model.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
        System.out.println("result=====" + outputFeature0.getIntValue(0));
    }

    /**
     * image util
     * @param uri of image we choose
     */
    public void proSrc2Gray(ContentResolver contentResolver, Uri uri){
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Mat bin = new Mat();
        Bitmap srcBitmap = null;
        try {
            srcBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
            //对图片进行灰度化
            Utils.bitmapToMat(srcBitmap, rgbMat);
            Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
//            Mat sobel = new Mat();
//            Imgproc.Sobel(grayMat, sobel, CvType.CV_8U, 0, 1, 3, 1, 0, Core.BORDER_REPLICATE);
//            Core.bitwise_not(sobel, sobel);
            // 利用OTSU二值化，将文本行与背景分割
            double ret = Imgproc.threshold(grayMat, bin, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            //通过形态学腐蚀，将分割出来的文字字符连接在一起
            Mat rec = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 20), new Point(-1,-1));
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
                if (Imgproc.contourArea(counts.get(i)) < 300)
                    continue;
                Rect rect = boundingRect(counts.get(i));
                matQueue.add(grayMat.submat(rect));
//                System.out.println("mat===" + ROI.get(i, i));
            }

            System.out.println("the image is done" + matQueue.size());

            workState = false;
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    private ByteBuffer getMatFloat() {
        Mat example = new Mat();
        example = matQueue.get(30);
        resize(example, example, new Size(30, 30));
        int width = example.width();
        int height = example.height();
        int channel = example.channels();
        float g;

        byte[] data = new byte[channel];

        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                example.get(i, j, data);
                g = data[0]&0xff;
                g = g / 128.0f;
                imageMat[0][i][j][0] = g;
            }
        }
        return ByteBuffer.wrap(floatArrayToByteArray(imageMat));
    }

    private static byte[] floatArrayToByteArray(float[][][][] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                try {
                    dataOutputStream.writeFloat(data[0][i][j][0]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return out.toByteArray();
    }
}
