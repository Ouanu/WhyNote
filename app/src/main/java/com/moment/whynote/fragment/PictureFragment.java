package com.moment.whynote.fragment;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.moment.whynote.R;
import com.moment.whynote.utils.OCRImageUtil;

import org.opencv.android.Utils;
import org.tensorflow.lite.support.metadata.schema.Content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class PictureFragment extends DialogFragment implements View.OnClickListener {
    private Button btnCamera;
    private Button btnGallery;
    private String text;
    private final String filePath = "/sdcard/Android/data/com.moment.whynote/files/output_image.jpg";
    private final Executor executor = Executors.newSingleThreadExecutor();
    private WaitingFragment waitingFragment = new WaitingFragment();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.picture_fragment, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        btnCamera = view.findViewById(R.id.btn_camera);
        btnGallery = view.findViewById(R.id.btn_gallery);
        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v == btnCamera) {
            requestPermission();
            File file = new File("/sdcard/Android/data/com.moment.whynote/files/output_image.jpg");
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri imageUri = FileProvider.getUriForFile(getContext(), "com.moment.whynote.WhyNoteApplication.fileProvider", file);
            mGetPicture.launch(imageUri);
            //            this.dismiss();
        }
    }

    //动态请求权限
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }
    }



    // Activity返回结果
    private final ActivityResultLauncher<Uri> mGetPicture = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
//            getParentFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                    .setCustomAnimations(R.anim.no_slide, R.anim.from_bottom);
//            waitingFragment.show(getParentFragmentManager(), null);
        if (result) {
            File file = new File("/sdcard/Android/data/com.moment.whynote/files/output_image.jpg");
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                Log.i("BITMAP>SIZE", "onActivityResult: " + bitmap.getWidth() + " " + bitmap.getHeight());
//                executor.execute(new OCRTask(bitmap));
                new Thread(new OCRTask(bitmap)).start();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("TAKE_PHOTO=====", "FALSE");
        }

//            executor.execute(new OCRTask(result));
    });


    /**
     * OCR获取结果
     */
    private class OCRTask implements Runnable {

        private Bitmap result;

        public OCRTask(Bitmap result) {
            this.result = result;
        }

        @Override
        public void run() {

            try {
                text = OCRImageUtil.getInstance().execute(result);
                Log.i("PictureFragment", "run: =========" + text);
                Bundle bundle = new Bundle();
                bundle.putString("ocr_text", text);
                getParentFragmentManager().setFragmentResult("key", bundle);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            waitingFragment.dismiss();
        }
    }


}
