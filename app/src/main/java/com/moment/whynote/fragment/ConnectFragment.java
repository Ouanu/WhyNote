package com.moment.whynote.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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


    private static final String TAG = "ConnectFragment";
    private EditText etIpAddress;
    private EditText etPort;

    public interface ConnectListener {
        void onConnectSelected(Bundle bundle);
    }

    private static ConnectListener connectCallback;


    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        connectCallback = (ConnectListener) context;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

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
        Button btnDisconnect = view.findViewById(R.id.btn_disconnect);
        btnDisconnect.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        if (v.getId() == R.id.btn_connect) {
            bundle.putString("command", "start");
            bundle.putString("ip", etIpAddress.getText().toString());
            bundle.putInt("port", Integer.valueOf(etPort.getText().toString()));
            connectCallback.onConnectSelected(bundle);
            dismiss();
        } else if (v.getId() == R.id.btn_disconnect) {
            bundle.putString("command", "finish");
            connectCallback.onConnectSelected(bundle);
            dismiss();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: delete========");
        super.onDestroy();
    }
}
