package com.moment.whynote.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class PictureFragment extends DialogFragment implements View.OnClickListener {
    private Button btnCamera;
    private Button btnGallery;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final WaitingFragment waitingFragment = new WaitingFragment();

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
            @SuppressLint("SdCardPath")
            File file = new File("/sdcard/Android/data/com.moment.whynote/files/output_image.jpg");
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri imageUri = FileProvider.getUriForFile(requireContext(), "com.moment.whynote.WhyNoteApplication.fileProvider", file);
            mGetPicture.launch(imageUri);
            //            this.dismiss();
        } else if (v == btnGallery) {
            mGetContent.launch("image/*");
        }
    }

    //动态请求权限
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }
    }


    // Activity返回结果
    private final ActivityResultLauncher<Uri> mGetPicture = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
        if (result) {
            showWaiting();
            @SuppressLint("SdCardPath")
            File file = new File("/sdcard/Android/data/com.moment.whynote/files/output_image.jpg");
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                Log.i("BITMAP>SIZE", "onActivityResult: " + bitmap.getWidth() + " " + bitmap.getHeight());
                executor.execute(new OCRTask(bitmap));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("TAKE_PHOTO", "FALSE");
        }

    });

    //显示等待界面
    private void showWaiting() {
        getParentFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .setCustomAnimations(R.anim.no_slide, R.anim.from_bottom);
        waitingFragment.show(getParentFragmentManager(), null);
    }

    // Activity返回结果
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            result -> {
                if (result != null) {
                    showWaiting();
                }
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), result);
                    executor.execute(new OCRTask(bitmap));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

    );


    /**
     * OCR获取结果
     */
    private class OCRTask implements Runnable {

        private final Bitmap result;

        public OCRTask(Bitmap result) {
            this.result = result;
        }

        @Override
        public void run() {

            try {
                String text = OCRImageUtil.getInstance().execute(result);
                Bundle bundle = new Bundle();
                bundle.putString("orc_text", text);
                getParentFragmentManager().setFragmentResult("key", bundle);
            } catch (IOException e) {
                e.printStackTrace();
            }
            waitingFragment.dismiss();
            finished();
        }
    }

    private void finished() {
        this.dismiss();
    }


}
