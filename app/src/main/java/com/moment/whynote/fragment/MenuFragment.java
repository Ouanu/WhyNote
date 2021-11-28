package com.moment.whynote.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.moment.whynote.R;
import com.moment.whynote.database.ResRepository;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 添加文本弹窗，一键粘贴复制
 * 自定义弹窗
 */
public class MenuFragment extends DialogFragment implements View.OnClickListener {


    private Button btnCpAll;
    private Button btnCpUri;
    private Button btnDelete;
    private ResRepository repository = ResRepository.getInstance();

    /**
     * @return 自定义布局
     */
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.long_click_menu, container, false);
        Objects.requireNonNull(getDialog()).getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        btnCpAll = view.findViewById(R.id.btn_cp_all);
        btnCpUri = view.findViewById(R.id.btn_cp_uri);
        btnDelete = view.findViewById(R.id.btn_delete);
        btnCpAll.setOnClickListener(this);
        btnCpUri.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        Bundle bundle = getArguments();
        if(v.getId() == R.id.btn_delete) {
            new Thread(()-> repository.deleteResData(repository.getResDataByUid(bundle.getInt("primaryKey")))).start();
            dismiss();
        }
    }


}
