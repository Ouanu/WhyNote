package com.moment.whynote.fragment;

import android.content.Intent;
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
import com.moment.whynote.service.ControlService;
import org.jetbrains.annotations.NotNull;


/**
 * 长按自定义弹窗
 * 实现功能：
 * 复制内容、复制链接、分享链接、删除......
 */
public class ConnectFragment extends DialogFragment implements View.OnClickListener {


    private EditText etIpAddress;
    private EditText etPort;

    /**
     * @return 自定义布局
     */
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.connect_fragment, container, false);
        initView(view);
        return view;
    }


    private void initView(View view) {
        etIpAddress = view.findViewById(R.id.et_ip_address);
        etPort = view.findViewById(R.id.et_port);
        Button btnConnect = view.findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_connect) {
            Intent start = new Intent(getContext(), ControlService.class);
            start.putExtra("ip",etIpAddress.getText().toString());
            start.putExtra("port", Integer.valueOf(etPort.getText().toString()));
            requireContext().startService(start);
            dismiss();
        }
    }
}
