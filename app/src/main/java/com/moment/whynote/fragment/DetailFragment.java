package com.moment.whynote.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.moment.oetlib.view.OEditTextView;
import com.moment.oetlib.view.OToolBarView;
import com.moment.oetlib.view.tools.OToolItem;
import com.moment.whynote.R;
import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;

import org.jetbrains.annotations.NotNull;


@SuppressWarnings("ALL")
public class DetailFragment extends Fragment implements View.OnClickListener {


    private EditText etTitle;
    private OEditTextView etDesc;
    private OToolBarView toolbar;
    private ResRepository repository;
    private InputMethodManager im;
    private ResData data;
    private static boolean isEditing = false;
    private static final StringBuilder builder = new StringBuilder();
    private DetailHandler handler;

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
        repository = ResRepository.getInstance();
        etTitle = view.findViewById(R.id.et_title);
        etDesc = view.findViewById(R.id.et_desc);
        toolbar = view.findViewById(R.id.toolbar);
        etDesc.getEditText().setBackgroundColor(0);
        handler = new DetailHandler();
        new Thread(() -> {
            assert bundle != null;
            if (bundle.getInt("primaryKey") == 0) {
                data = repository.getResDataByUpdateDate(bundle.getLong("updateDate"));
            } else {
                data = repository.getResDataByUid(bundle.getInt("primaryKey"));
            }
            etTitle.setText(data.title);
            etDesc.getEditText().setText(data.desc);
            handler.sendEmptyMessage(20000);
        }).start();
        etDesc.getEditText().getOTools().autoTool();

        FloatingActionButton btnModel = view.findViewById(R.id.btn_model);
        btnModel.setOnClickListener(this);
        im = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(etDesc.getEditText().getApplicationWindowToken(), 0);
        etDesc.getEditText().setFocusableInTouchMode(false);

        etDesc.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                builder.replace(0, builder.length(), s.toString());
            }
        });



    }

    @Override
    public void onStop() {
        super.onStop();
        new Thread(() -> {
            Looper.prepare();
            data.title = String.valueOf(etTitle.getText());
            data.desc = String.valueOf(etDesc.getEditText().getText());
            if (data.title.equals("") && data.desc.equals("")) {
                repository.deleteResData(data);
            } else {
                repository.upResData(data);
            }
            Looper.loop();
        }).start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_model) {
            if(!isEditing) {
                isEditing = true;
                toolbar.setVisibility(View.VISIBLE);
                etDesc.getEditText().setFocusableInTouchMode(true);
                etDesc.getEditText().setFocusable(true);
                im.showSoftInput(v, 0);
                etDesc.getEditText().setText(builder);
            } else {
                isEditing = false;
                toolbar.setVisibility(View.GONE);
                etDesc.getEditText().setFocusable(false);
                etDesc.getEditText().setFocusableInTouchMode(false);
                im.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                applyTools();

            }
        }
    }

    private void applyTools() {
        for (OToolItem oToolItem : etDesc.getEditText().getOTools().getToolList()) {
            oToolItem.applyOMDTool();
        }
    }

    @SuppressLint("all")
    private class DetailHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
            if (msg.what == 20000) {
                applyTools();
            }
        }
    }
}
