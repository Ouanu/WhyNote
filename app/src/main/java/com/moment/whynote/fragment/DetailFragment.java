package com.moment.whynote.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moment.whynote.R;
import com.moment.whynote.utils.DataUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = "DetailFragment";
    private EditText etDesc;
    private boolean flag = false;
    private DataUtils utils = new DataUtils();

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
        EditText etTitle = (EditText) view.findViewById(R.id.et_title);
        etDesc = view.findViewById(R.id.et_desc);
        ImageButton btnGetUrl = view.findViewById(R.id.btn_get_uri);
        FrameLayout flUri = view.findViewById(R.id.fl_uri);
        etTitle.setText(bundle.getString("title"));
        etDesc.setText(bundle.getString("desc"));
        etDescSetOnTouchListener();
        btnGetUrl.setOnClickListener(this);
    }

    private void etDescSetOnTouchListener() {
        etDesc.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: ----" + event.getAction() + "   " + flag);

                if(event.getAction() == 0) {
                    flag = true;
                } else if(event.getAction() == 2) {
                    flag = false;
                    etDesc.setFocusable(false);
                } else if(event.getAction() == 1 && flag) {
                    etDesc.setFocusable(true);
                    etDesc.setFocusableInTouchMode(true);
                    etDesc.requestFocus();
                    etDesc.requestFocusFromTouch();
                }
                return false;
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_get_uri) {
            List<String> uris = new ArrayList<>();
            uris = utils.getUris(etDesc.getText().toString());
        }
    }


}
