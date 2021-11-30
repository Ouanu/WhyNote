package com.moment.whynote.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.moment.whynote.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 添加文本弹窗，一键粘贴复制
 * 自定义弹窗
 */
public class InsertFragment extends DialogFragment implements View.OnClickListener {

    private EditText etString;
    private EditText etTitle;

    /**
     * 回调接口
     */
    public interface DialogListener {
        void sendValue(String title, String str);
    }

    private DialogListener dialogListener;


    /**
     * 将fragment与activity建立关联
     */
    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        dialogListener = (DialogListener) getActivity();
    }

    /**
     * @return 自定义布局
     */
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.insert_fragment, container, false);
        Objects.requireNonNull(getDialog()).getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        etString = view.findViewById(R.id.et_string);
        etTitle = view.findViewById(R.id.et_title);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            String str = etString.getText().toString();
            String title = etTitle.getText().toString();
            dialogListener.sendValue(title, str);
            this.dismiss();
        }


    }
}
