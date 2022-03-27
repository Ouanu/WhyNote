package com.moment.whynote.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;
import com.moment.whynote.service.ControlService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * 长按自定义弹窗
 * 实现功能：
 * 复制内容、复制链接、分享链接、删除......
 */
public class ConnectFragment extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "ConnectFragment";
    private EditText etIpAddress;
    private EditText etPort;
    private final ResRepository repository = ResRepository.getInstance();
    private List<ResData> resDataList;
    private SharedPreferences sharedPreferences;


    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository.getAllResData().observe(getViewLifecycleOwner(), resData -> resDataList = resData);
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
        sharedPreferences = getContext().getSharedPreferences("setting", MODE_PRIVATE);
        if (!sharedPreferences.getString("IP", "NULL").equals("NULL")) {
            etIpAddress.setText(sharedPreferences.getString("IP", "NULL"));
            etPort.setText(String.valueOf(sharedPreferences.getInt("PORT", 0)));
        }
        return view;
    }


    private void initView(View view) {
        etIpAddress = view.findViewById(R.id.et_ip_address);
        etPort = view.findViewById(R.id.et_port);
        Button btnConnect = view.findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(this);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        if (v.getId() == R.id.btn_connect) {
            bundle.putString("IP", etIpAddress.getText().toString());
            bundle.putInt("PORT", Integer.parseInt(etPort.getText().toString()));
            Intent intent = new Intent(getContext(), ControlService.class);
            ArrayList list = new ArrayList();
            list.add(resDataList);
            bundle.putParcelableArrayList("LIST", list);
            intent.putExtras(bundle);
            getContext().startService(intent);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("IP", etIpAddress.getText().toString());
            editor.putInt("PORT", Integer.parseInt(etPort.getText().toString()));
            editor.apply();
            dismiss();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
