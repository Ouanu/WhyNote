package com.moment.whynote.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.resize;


public class OCRImageUtil {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext = null;
    private final LinkedList<Mat> matQueue = new LinkedList<>();
    private final LinkedList<Bitmap> bitmaps = new LinkedList<>();
    @SuppressLint("StaticFieldLeak")
    private static final OCRImageUtil instance = null;
    private final int[] dims = {1, 30, 30, 1};
    private float[][][][] inputMat = new float[1][30][30][1];
    private final float[][] out = new float[1][3755];
    private final Interpreter tfLite;
    private static final String MODEL_PATH = "model.tflite";
    private static final char[] labels = new char[3755];
    StringBuffer strBuffer = new StringBuffer();

    /**
     * 获取单例
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
    public OCRImageUtil(Context mContext) throws IOException {
        tfLite = new Interpreter(loadModelFile(mContext));
        OCRImageUtil.mContext = mContext;
        getLabels();
    }

    /**
     * 加载模型
     * @return ByteBuffer
     */
    private ByteBuffer loadModelFile(Context mContext) throws IOException {
        AssetFileDescriptor fileDescriptor = mContext.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    /**
     * 执行任务
     * @param contentResolver 获取内容解析者
     * @param uri 获取文件链接
     */
    public void execute(ContentResolver contentResolver, Uri uri) {
        new Thread(() -> {
            proSrc2Gray(contentResolver, uri);
            ocr();
        }).start();
    }

    /**
     * OCR识别
     * 获取精度最高的标签
     */
    private void ocr() {

        for (Bitmap bitmap : bitmaps) {
            //把原图缩放成我们需要的图片大小
            Bitmap bm = Bitmap.createScaledBitmap(bitmap, dims[1], dims[2], false);
            inputMat = getMatFloat(bm);

            tfLite.run(inputMat, out);
            float max = 0;
            int cnt = 0;
            int d = 0;
            for (int i = 0; i < out[0].length; i++) {
                if (max < out[0][i]) {
                    max = out[0][i];
                    d = cnt;
                }
                cnt++;
            }
            if(max >= 0f)
                strBuffer.append(labels[d]);
        }

        System.out.println(strBuffer.toString());


    }

    /**
     * image util
     *
     * @param uri of image we choose
     */
    public void proSrc2Gray(ContentResolver contentResolver, Uri uri) {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Mat bin = new Mat();
        Bitmap srcBitmap;
        try {
            srcBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
            //对图片进行灰度化
            Utils.bitmapToMat(srcBitmap, rgbMat);
            Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
            // 利用OTSU二值化，将文本行与背景分割
            Imgproc.threshold(grayMat, bin, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            //通过形态学腐蚀，将分割出来的文字字符连接在一起
            Mat rec = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 20), new Point(-1, -1));
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
                if (Imgproc.contourArea(counts.get(i)) < 100)
                    continue;
                Rect rect = boundingRect(counts.get(i));
                matQueue.add(grayMat.submat(rect));
                Mat mat = matQueue.getLast();
                Bitmap bm = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat, bm);
                bitmaps.add(bm);
            }
            System.out.println("the image is done" + matQueue.size());

        } catch (IOException e) {
            Log.d("OCRImageUtil", e.getMessage());
        }
    }

    /**
     * 将 Mat 转换为 float[][][][]数组（输入格式）
     * @return 返回数组
     */
    private float[][][][] getMatFloat(Bitmap bm) {
        float[][][][] inFloat = new float[dims[0]][dims[1]][dims[2]][dims[3]];
        //新建一个一维数组，长度是图片像素点的数量
        int[] pixels = new int[dims[1] * dims[2]];

        //把图片的每个像素点的值放到我们前面新建的一维数组中
        bm.getPixels(pixels, 0, bm.getWidth(), 0, 0, dims[1], dims[2]);
        int pixel = 0;
        for (int i = 0; i < dims[1]; ++i) {
            for (int j = 0; j < dims[2]; ++j) {
                final int val = pixels[pixel++];
                float gray = (val & 0xFF);
                gray = gray / 255.0f;
                float[] arr = {gray};
                inFloat[0][i][j] = arr;
            }
        }
        if (bm.isRecycled()) {
            bm.recycle();
        }
        return inFloat;
    }

    public void getLabels() throws IOException {
        InputStream ins = mContext.getAssets().open("labels.txt");
        InputStreamReader reader = new InputStreamReader(ins, "GBK");
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = " ";
        int i=0;
        while ((line = bufferedReader.readLine()) != null) {
            line.replace(",","");
            for (int j = 0; j < line.length(); j++) {
                if (Character.isDigit(line.charAt(j))) {
                    continue;
                } else {
                    labels[i] = line.charAt(j);
                }
            }
            i++;
        }
    }

}
