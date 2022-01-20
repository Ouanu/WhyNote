package com.moment.whynote.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.moment.whynote.R;
import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * 长按自定义弹窗
 * 实现功能：
 * 复制内容、复制链接、分享链接、删除......
 */
public class MenuFragment extends DialogFragment implements View.OnClickListener {


    private final ResRepository repository = ResRepository.getInstance();
    private Bundle bundle;
    private ResData data;


    /**
     * @return 自定义布局
     */
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.long_click_menu, container, false);
        Objects.requireNonNull(getDialog()).getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().getAttributes().gravity = Gravity.CENTER;
        initView(view);
        bundle = getArguments();
        new Thread(() -> data = repository.getResDataByUid(bundle.getInt("primaryKey"))).start();
        return view;
    }

    /*
    初始化控件
     */
    private void initView(View view) {
        Button btnCpAll = view.findViewById(R.id.btn_cp_all);
        Button btnDelete = view.findViewById(R.id.btn_delete);
        Button btnExportFile = view.findViewById(R.id.btn_export_file);
        btnCpAll.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnExportFile.setOnClickListener(this);

    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_delete) {
            new Thread(() -> {
                assert bundle != null;
                repository.deleteResData(data);
            }).start();
        } else if (v.getId() == R.id.btn_cp_all) {
            copyMethod(data.desc);
        } else if (v.getId() == R.id.btn_export_file) {
            File file = new File(data.dirPath, (data.title.equals("")?data.updateDate: data.title) + ".md");
            Log.d("MenuFragment", "onClick: " + data.dirPath);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                OutputStream opt = new FileOutputStream(file, false);
                opt.write(data.desc.getBytes());
                opt.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dismiss();
    }


    //复制选项
    private void copyMethod(String desc) {
        if (null != requireContext().getSystemService(Context.CLIPBOARD_SERVICE)) {
            ClipboardManager manager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", desc);
            manager.setPrimaryClip(mClipData);
            Toast.makeText(getContext(), desc + " 已复制成功", Toast.LENGTH_SHORT).show();
        }

    }


}
