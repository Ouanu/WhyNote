package com.moment.whynote.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import android.net.Uri;

import android.provider.MediaStore;
import android.util.Log;


import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
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

import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.rectangle;
import static org.opencv.imgproc.Imgproc.resize;


public class OCRImageUtil {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext = null;
    private final LinkedList<Mat> matQueue = new LinkedList<>();
    private final LinkedList<Bitmap> bitmaps = new LinkedList<>();
    @SuppressLint("StaticFieldLeak")
    private static final OCRImageUtil instance = null;
    private final int[] dims = {1, 30, 30, 1};
    private final float[][] out = new float[1][3755];
    private final Interpreter tfLite;
    private static final String MODEL_PATH = "model2.tflite";
    private static final char[] labels = new char[3755];
    StringBuffer strBuffer = new StringBuffer();

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
    public OCRImageUtil(Context mContext) throws IOException {
        tfLite = new Interpreter(loadModelFile(mContext));
        OCRImageUtil.mContext = mContext;
        getLabels();
    }

    /**
     * 加载模型
     *
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
            TessBaseAPI tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.init("/sdcard/Android/data/com.moment.whynote/files/tesseract/", "chi_sim");
            tessBaseAPI.setImage(bitmap);
            text = tessBaseAPI.getUTF8Text();
//                Log.i("hgfhgfhfghfg", "run: text " + System.currentTimeMillis() + text);

            tessBaseAPI.end();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        new Thread(() -> {
////            proSrc2Gray(contentResolver, uri);
////            ocr();
//
//
//        }).start();
        return text;
    }

    /**
     * OCR识别
     * 获取精度最高的标签
     */
    private void ocr() {

        for (Bitmap bitmap : bitmaps) {
            //把原图缩放成我们需要的图片大小
            Bitmap bm = Bitmap.createScaledBitmap(bitmap, dims[1], dims[2], false);
            float[][][][] inputMat = getMatFloat(bm);
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
            if (max >= 0.5f)
                strBuffer.append(labels[d]);
            else
                strBuffer.append(' ');
        }

        System.out.println(strBuffer.toString());


    }

    /**
     * image util
     *
     * @param uri of image we choose
     */
    public void proSrc2Gray(ContentResolver contentResolver, Uri uri) {
        Mat grayMat;
        Mat bin = new Mat();
        try {
            grayMat = Imgcodecs.imread(uri.getPath(), Imgcodecs.IMREAD_GRAYSCALE);
//            srcBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri).setDensity(MediaStore.EXTRA_OUTPUT);
//            srcBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
//            System.out.println(srcBitmap.getWidth() + " "+ srcBitmap.getHeight() + ";;; " + src2Bitmap.getWidth() + " "+ src2Bitmap.getHeight());

            //对图片进行灰度化
//            Utils.bitmapToMat(srcBitmap, rgbMat);
//            Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
            grayMat = houghLines(grayMat);
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
            int bitmapMax = 0;
            Mat kernel = new Mat(3, 3, CvType.CV_32FC1);
            float[] arrayFloat = new float[]{0F, -1F, 0F, -1F, 5F, -1F, 0F, -1F, 0F};
            kernel.put(0, 0, arrayFloat);
            List<Rect> axisX = new ArrayList<>(counts.size());
            for (MatOfPoint matOfPoint : counts) {
                if (Imgproc.contourArea(matOfPoint) < 200)
                    continue;
                Rect cntRect = Imgproc.boundingRect(matOfPoint);
                int index;
                for (index = 0; index < axisX.size(); index++) {
                    if (axisX.get(index).x > cntRect.x) {
                        break;
                    }
                }
                axisX.add(index, cntRect);
            }
            List<Rect> axisY = new ArrayList<>(axisX.size());
            for (Rect x : axisX) {
                int index;
                for (index = 0; index < axisY.size(); index++) {
                    if (axisY.get(index).y > x.y + 10) {
                        break;
                    }
                }
                axisY.add(index, x);
            }

            for (Rect rect : axisY) {

                matQueue.add(grayMat.submat(rect));
                Mat mat = matQueue.getLast();
                resize(mat, mat, new Size(30, 30));
                Bitmap bm = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888);
                Core.bitwise_not(mat, mat);
                if ((mat.width() / 2) > bitmapMax) {
                    bitmapMax = mat.width() / 2;
                }
                if (mat.width() > bitmapMax && mat.height() > bitmapMax) {
                    Imgproc.filter2D(mat,
                            mat,
                            -1,
                            kernel,
                            new Point(-1, -1),
                            Core.BORDER_CONSTANT
                    );
                    Utils.matToBitmap(mat, bm);
                    rectangle(grayMat, rect, new Scalar(34, 25, 25, 33), 1);
                    bitmaps.add(bm);
                }
//                Bitmap bm = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);

            }
            System.out.println("the image is done" + bitmaps.size());

        } catch (Exception e) {
            Log.d("OCRImageUtil", e.getMessage());
        }
    }

    /**
     * 将 Mat 转换为 float[][][][]数组（输入格式）
     *
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
        String line;
        int i = 0;
        while ((line = bufferedReader.readLine()) != null) {
            //noinspection ResultOfMethodCallIgnored
            line.replace(",", "");
            for (int j = 0; j < line.length(); j++) {
                if (!Character.isDigit(line.charAt(j))) {
                    labels[i] = line.charAt(j);
                }
            }
            i++;
        }
    }

    /**
     * 图形矫正
     *
     * @param mat 传入灰度图 Mat
     * @return 返回矫正后的图片
     */
    public Mat houghLines(Mat mat) {
        Mat backup = mat.clone();
        Mat lines = new Mat();
        Imgproc.Canny(mat, lines, 100, 200, 3);
        Imgproc.HoughLines(lines, lines, 1, Math.PI / 180.0, 200, 0, 0);
        double sum = 0;
        double angle;
        for (int x = 0; x < lines.rows(); x++) {
            double[] vec = lines.get(x, 0);

            double rho = vec[0];
            double theta = vec[1];

            Point pt1 = new Point();
            Point pt2 = new Point();

            double a = Math.cos(theta);
            double b = Math.sin(theta);

            double x0 = a * rho;
            double y0 = b * rho;

            pt1.x = Math.round(x0 + 1000 * (-b));
            pt1.y = Math.round(y0 + 1000 * (a));
            pt2.x = Math.round(x0 - 1000 * (-b));
            pt2.y = Math.round(y0 - 1000 * (a));
            sum += theta;
            Imgproc.line(mat, pt1, pt2, new Scalar(0, 0, 255), 1, Imgproc.LINE_4, 0);
        }

        double average = sum / lines.rows(); //对所有角度求平均，这样做旋转效果会更好
        angle = average / Math.PI * 180 - 90;
        System.out.println("average:" + angle);
        Point center = new Point();
        center.x = mat.cols() / 2.0;
        center.y = mat.rows() / 2.0;

// 得到旋转矩阵算子
        Mat matrix = Imgproc.getRotationMatrix2D(center, angle, 1);
        Imgproc.warpAffine(backup, backup, matrix, backup.size(), 1, 0, new Scalar(0, 0, 0));
        return backup;
    }

}
