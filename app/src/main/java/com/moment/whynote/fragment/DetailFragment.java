package com.moment.whynote.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
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

public class DetailFragment extends Fragment {
    private EditText etTitle;
    private EditText etDesc;
    private ImageButton btnGetUrl;
    private FrameLayout flUri;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.detail_fragment, container, false);
        etTitle = view.findViewById(R.id.et_title);
        etDesc = view.findViewById(R.id.et_desc);
        btnGetUrl = view.findViewById(R.id.btn_get_url);
        flUri = view.findViewById(R.id.fl_uri);
        return view;
    }

}
