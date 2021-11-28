package com.moment.whynote.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.moment.whynote.utils.DataUtils;

import org.jetbrains.annotations.NotNull;
import java.util.List;
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
    private final DataUtils utils = new DataUtils();

    /**
     * @return 自定义布局
     */
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.long_click_menu, container, false);
        Objects.requireNonNull(getDialog()).getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        initView(view);
        bundle = getArguments();
        new Thread(()->data = repository.getResDataByUid(bundle.getInt("primaryKey"))).start();
        return view;
    }

    /*
    初始化控件
     */
    private void initView(View view) {
        Button btnCpAll = view.findViewById(R.id.btn_cp_all);
        Button btnCpUri = view.findViewById(R.id.btn_cp_uri);
        Button btnDelete = view.findViewById(R.id.btn_delete);
        btnCpAll.setOnClickListener(this);
        btnCpUri.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_delete) {
            new Thread(()-> {
                assert bundle != null;
                repository.deleteResData(data);
            }).start();
        } else if (v.getId() == R.id.btn_cp_all) {
            copyMethod(data.desc);
        } else if (v.getId() == R.id.btn_cp_uri) {
            copyMethod(data.uri);
        }
        dismiss();
    }


    //复制选项
    private void copyMethod(String desc) {
        /*
        若检测Uri为空时，自动生成Uri字符串，并提示用户再试一遍
        否则直接复制Uri
         */
        if(desc == null) {
            new Thread(()->{
                List<String> uriList = utils.getUris(data.desc);
                data.uri = utils.getUriString(uriList);
                repository.upResData(data);
            }).start();
            Toast.makeText(getContext(), "再试一下！！", Toast.LENGTH_SHORT).show();
        } else {
            if (null != requireContext().getSystemService(Context.CLIPBOARD_SERVICE)) {
                ClipboardManager manager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", desc);
                manager.setPrimaryClip(mClipData);
                Toast.makeText(getContext(), desc + " 已复制成功", Toast.LENGTH_SHORT).show();
            }
        }

    }


}
