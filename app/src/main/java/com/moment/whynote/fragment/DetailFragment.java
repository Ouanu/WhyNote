package com.moment.whynote.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moment.whynote.R;
import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;
import com.moment.whynote.utils.FloatViewUtil;
import com.moment.whynote.view.OTextEditor;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Method;

import static android.app.Activity.RESULT_OK;

public class DetailFragment extends Fragment implements View.OnClickListener {
    //    private final static String TAG = "DetailFragment";
//    private static final String verifyCode = "#w102938m#";
    private OTextEditor etDesc;
    private boolean flag = false;
    private EditText etTitle;
    private Method method;
    private final Class<EditText> cls = EditText.class;
    private final ResRepository repository = ResRepository.getInstance();
    private ResData data = null;
    private LinearLayout llToolbar;
    private InputMethodManager methodManager;
    private static int start;
    private static int end;
    private StringBuffer buffer = new StringBuffer();


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.detail_fragment, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        Bundle bundle = getArguments();
        etTitle = view.findViewById(R.id.et_title);
        etDesc = view.findViewById(R.id.et_desc);
        llToolbar = view.findViewById(R.id.ll_toolbar);
        ImageButton insertImgBtn = view.findViewById(R.id.insert_img_btn);
        ImageButton btnGetUrl = view.findViewById(R.id.btn_get_uri);
        FloatViewUtil util = new FloatViewUtil(getActivity());
        /*
          初始化title、desc的数据
         */
        if (bundle != null) {
            new Thread(() -> {
//                从bundle获取“主键”
                if (null != repository.getResDataByUid(bundle.getInt("primaryKey"))) {
                    data = repository.getResDataByUid(bundle.getInt("primaryKey"));
                } else {
                    data = repository.getResDataByUpdateDate(bundle.getLong("updateDate"));
                }
                System.out.println(data.toString());
//                初始化数据
                etTitle.setText(data.title);
                buffer.append(data.desc);
                etDesc.getEditText().setText(data.desc);
            }).start();

        }
        etSetOnTouchListener();
        btnGetUrl.setOnClickListener(this);
        insertImgBtn.setOnClickListener(this);
        /*
        隐藏软键盘
         */
        try {
            method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
//            method.invoke(etDesc, false);
            method.invoke(etTitle, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        methodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        RelativeLayout rlRes = view.findViewById(R.id.rl_res);
        util.setFloatView(rlRes, llToolbar);
        util.setFloatView(rlRes, btnGetUrl);
    }

    /**
     * 重写OnTouch事件，触摸滑动不弹出键盘输入，点击才可输入
     */
    @SuppressLint("ClickableViewAccessibility")
    private void etSetOnTouchListener() {
//        etDesc.setOnTouchListener((v, event) -> {
//            if (event.getAction() == 0) {
//                flag = true;
//            } else if (event.getAction() == 2) {
//                //触摸事件：不弹出键盘
//                flag = false;
//                try {
//                    etDesc.setCursorVisible(false);
//                    methodManager.hideSoftInputFromWindow(etDesc.getWindowToken(), 0);
//                    llToolbar.setVisibility(View.GONE);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } else if (event.getAction() == 1 && flag) {
//                //点击事件，弹出键盘
//                try {
//                    etDesc.setCursorVisible(true);
//                    methodManager.showSoftInput(etDesc, InputMethodManager.RESULT_SHOWN);
//                    methodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,
//                            InputMethodManager.HIDE_IMPLICIT_ONLY);
//                    llToolbar.setVisibility(View.VISIBLE);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            return false;
//        });


        //同上
        etTitle.setOnTouchListener((v, event) -> {
            if (event.getAction() == 0) {
                flag = true;
            } else if (event.getAction() == 2) {
                flag = false;
                try {
                    method.invoke(etTitle, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (event.getAction() == 1 && flag) {
                try {
                    method.invoke(etTitle, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        });

    }


    @SuppressWarnings("deprecation")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_get_uri) {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            start = etDesc.getEditText().getSelectionStart();
            end = etDesc.getEditText().getText().length() - start;
            startActivityForResult(intent, 1000);
            methodManager.hideSoftInputFromWindow(etDesc.getWindowToken(), 0);
            System.out.println(getActivity().getPackageName());
        } else if (v.getId() == R.id.insert_img_btn) {

        }
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            buffer.insert(start, "<" + uri + ">");
            etDesc.getEditText().setText(buffer);
            etDesc.insertImage(buffer, start, end, getContext().getContentResolver());
        }
    }


    /**
     * 返回保存已修改的数据
     */
    @Override
    public void onStop() {
        super.onStop();
        System.out.println(data.title);
        //判断标题、内容是否同时为空，若为空则删除该data
        if (etTitle.getText().toString().equals("") && etDesc.getEditText().getText().toString().equals("")) {
            new Thread(() -> repository.deleteResData(data)).start();
            //否则，检查data的数据与textview中数据是否相同，不相同则更新
        } else if (!data.title.equals(etTitle.getText().toString()) ||
                !data.desc.equals(etDesc.getEditText().getText().toString())) {
            data.title = etTitle.getText().toString();
            data.desc = etDesc.getEditText().getText().toString();
            data.updateDate = System.currentTimeMillis();
            new Thread(() -> repository.upResData(data)).start();
        }
    }

}
