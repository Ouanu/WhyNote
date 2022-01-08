package com.moment.whynote.fragment;

import android.os.Bundle;
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
    private String preTitle = "";
    private String preDesc = "";

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
        etTitle.setText(bundle.getString("title"));
        etDesc.getEditText().setText(bundle.getString("desc"));
        new Thread(()->{
            data = repository.getResDataByUid(bundle.getInt("primaryKey"));
            preTitle = etTitle.getText().toString();
            preDesc = etDesc.getEditText().getText().toString();
        }).start();


    }

    @Override
    public void onStop() {
        super.onStop();
        if(etTitle.getText().toString().contains("")
                &&etDesc.getEditText().getText().toString().contains("")){
            new Thread(()->repository.deleteResData(data));
        } else {
            data.title = etTitle.getText().toString();
            data.desc = etDesc.getEditText().getText().toString();
            new Thread(()->repository.upResData(data));
        }
    }
}
