package com.moment.whynote.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.moment.oetlib.view.OEditTextView;
import com.moment.oetlib.view.OToolBarView;
import com.moment.oetlib.view.tools.OToolItem;
import com.moment.oetlib.view.tools.OTools;
import com.moment.whynote.R;
import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;

import org.jetbrains.annotations.NotNull;

public class DetailFragment extends Fragment implements View.OnClickListener {


    private RelativeLayout rlRes;
    private EditText etTitle;
    private OEditTextView etDesc;
    private OToolBarView toolbar;
    private ResRepository repository;
    private InputMethodManager im;
    private ResData data;
    private FloatingActionButton btnModel;
    private static boolean isEditing = false;
    private static StringBuilder builder = new StringBuilder();


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
        rlRes = view.findViewById(R.id.rl_res);
        etTitle = view.findViewById(R.id.et_title);
        etDesc = view.findViewById(R.id.et_desc);
        toolbar = view.findViewById(R.id.toolbar);
        etDesc.getEditText().setBackgroundColor(0);
        new Thread(() -> {
            if (bundle.getInt("primaryKey") == 0) {
                data = repository.getResDataByUpdateDate(bundle.getLong("updateDate"));
            } else {
                data = repository.getResDataByUid(bundle.getInt("primaryKey"));
            }
            etTitle.setText(data.title);
            etDesc.getEditText().setText(data.desc);
        }).start();
        etDesc.getEditText().getOTools().autoTool();
        for (OToolItem oToolItem : etDesc.getEditText().getOTools().getToolList()) {
            oToolItem.applyOMDTool();
        }
        btnModel = view.findViewById(R.id.btn_model);
        btnModel.setOnClickListener(this);
        im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
            data.title = String.valueOf(etTitle.getText());
            data.desc = String.valueOf(etDesc.getEditText().getText());
            if (data.title.equals("") && data.desc.equals("")) {
                repository.deleteResData(data);
            } else {
                repository.upResData(data);
            }
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
                for (OToolItem oToolItem : etDesc.getEditText().getOTools().getToolList()) {
                    oToolItem.applyOMDTool();
                }

            }
        }
    }
}
