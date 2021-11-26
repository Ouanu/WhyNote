package com.moment.whynote.fragment;

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

import org.jetbrains.annotations.NotNull;

public class DetailFragment extends Fragment implements View.OnClickListener {
    private final static String TAG = "DetailFragment";
    private EditText etTitle;
    private EditText etDesc;

    private ImageButton btnGetUrl;
    private FrameLayout flUri;

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
        etTitle = (EditText)view.findViewById(R.id.et_title);
        etDesc = view.findViewById(R.id.et_desc);
        btnGetUrl = view.findViewById(R.id.btn_get_uri);
        flUri = view.findViewById(R.id.fl_uri);
        etTitle.setText(bundle.getString("title"));
        etDesc.setText(bundle.getString("desc"));
        etDesc.setOnClickListener(this);
        btnGetUrl.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.et_desc) {
            etDesc.setFocusable(true);
            etDesc.setFocusableInTouchMode(true);
            etDesc.requestFocus();
            etDesc.requestFocusFromTouch();

        }
    }


}
