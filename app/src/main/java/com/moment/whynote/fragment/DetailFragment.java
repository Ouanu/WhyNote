package com.moment.whynote.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moment.oetlib.view.OEditTextView;
import com.moment.oetlib.view.OToolBarView;
import com.moment.whynote.R;
import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;

import org.jetbrains.annotations.NotNull;

public class DetailFragment extends Fragment {


    private RelativeLayout rlRes;
    private EditText etTitle;
    private OEditTextView etDesc;
    private OToolBarView toolbar;
    private ResRepository repository;
    private ResData data;

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
}
