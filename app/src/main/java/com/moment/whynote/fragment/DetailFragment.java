package com.moment.whynote.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
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
import com.moment.whynote.view.OEditText;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class DetailFragment extends Fragment implements View.OnClickListener {
    //    private final static String TAG = "DetailFragment";
//    private static final String verifyCode = "#w102938m#";
    private OEditText etDesc;
    private EditText etTitle;
    private boolean flag = false;

    /*
    获取处理软键盘的方法
     */
    private Method method;
    private final Class<EditText> cls = EditText.class;
    private InputMethodManager methodManager;
    /*
    获得数据库的单例
     */
    private final ResRepository repository = ResRepository.getInstance();
    private ResData data = null;
    private static int start;
    private final StringBuffer buffer = new StringBuffer();
    private final StringBuilder path = new StringBuilder();
    private boolean isAddImage = false;
    private LinearLayout toolbar;


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.detail_fragment, container, false);
        initView(view);
        return view;
    }

    @SuppressLint("SdCardPath")
    private void initView(View view) {
        Bundle bundle = getArguments();
        etTitle = view.findViewById(R.id.et_title);
        etDesc = view.findViewById(R.id.et_desc);
        toolbar =view.findViewById(R.id.toolbar);
//        ImageButton btnGetUrl = view.findViewById(R.id.btn_get_uri);
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
//                初始化数据
                path.append("/data/user/0/com.moment.whynote/files/DCIM/").append(data.fileName);
                System.out.println("++++++++++++++++============" + data.fileName);

            }).start();
            etTitle.setText(bundle.getString("title"));
            etDesc.setText(bundle.getString("desc"));
            buffer.append(etDesc.getText());
            etDesc.insertImage(Objects.requireNonNull(etDesc.getText()).toString());

        }

        etSetOnTouchListener();
//        btnGetUrl.setOnClickListener(this);
        toolbar.setVisibility(View.GONE);
        /*
        隐藏软键盘
         */
        try {
            method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(etTitle, false);
            method.invoke(etDesc, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        methodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        RelativeLayout rlRes = view.findViewById(R.id.rl_res);
//        util.setFloatView(rlRes, btnGetUrl);
        util.setFloatView(rlRes, toolbar, 100);
        util.setFloatView(rlRes, etDesc, 500);
    }

    /**
     * 重写OnTouch事件，触摸滑动不弹出键盘输入，点击才可输入
     */
    @SuppressLint("ClickableViewAccessibility")
    private void etSetOnTouchListener() {
        etDesc.setOnTouchListener((v, event) -> {
            if (event.getAction() == 0) {
                flag = true;
            } else if (event.getAction() == 2) {
                //触摸事件：不弹出键盘
                flag = false;
                try {
                    etDesc.setCursorVisible(false);
                    methodManager.hideSoftInputFromWindow(etDesc.getWindowToken(), 0);
                    toolbar.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (event.getAction() == 1 && flag) {
                //点击事件，弹出键盘
                try {
                    etDesc.setCursorVisible(true);
                    methodManager.showSoftInput(etDesc, InputMethodManager.RESULT_SHOWN);
                    methodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                            InputMethodManager.HIDE_IMPLICIT_ONLY);
                    toolbar.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        });


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
            start = etDesc.getSelectionStart();
            startActivityForResult(intent, 1000);
            methodManager.hideSoftInputFromWindow(etDesc.getWindowToken(), 0);
            isAddImage = true;
        }
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            assert data != null;
            Uri uri = data.getData();
            StringBuffer str = new StringBuffer();
            str.append(Objects.requireNonNull(etDesc.getText()).toString());
            str.insert(start, "\n<" + etDesc.saveImage(uri, path.toString()) + ">\n");
            etDesc.setText(str);
            etDesc.insertImage(etDesc.getText().toString());
            isAddImage = false;
        }
    }


    /**
     * 返回保存已修改的数据
     */
    @Override
    public void onStop() {
        super.onStop();
        //判断标题、内容是否同时为空，若为空则删除该data
        if (etTitle.getText().toString().equals("") && Objects.requireNonNull(etDesc.getText()).toString().equals("") && !isAddImage) {
            new Thread(() -> {
                etDesc.deleteImage(path.toString());
                repository.deleteResData(data);
            }).start();
            //否则，检查data的数据与textview中数据是否相同，不相同则更新
        } else if (!data.title.equals(etTitle.getText().toString()) ||
                !data.desc.equals(Objects.requireNonNull(etDesc.getText()).toString())) {
            data.title = etTitle.getText().toString();
            data.desc = Objects.requireNonNull(etDesc.getText()).toString();
            data.updateDate = System.currentTimeMillis();
            new Thread(() -> {
                repository.upResData(data);
                System.out.println("delete_image");
                File dir = new File(String.valueOf(path));
                if (dir.exists()) {
                    String[] fileList = dir.list();
                    assert fileList != null;
                    for (String s : fileList) {
                        if (!etDesc.getText().toString().contains(s)) {
                            etDesc.deleteImage(path + "/" + s);
                        }
                    }
                }
            }).start();
        }
    }
}
