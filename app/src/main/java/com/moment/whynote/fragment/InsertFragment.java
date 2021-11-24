package com.moment.whynote.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import org.jetbrains.annotations.NotNull;

public class InsertFragment extends DialogFragment implements View.OnClickListener {

    private EditText etString;
    private Button btnConfirm;

    public interface DialogListener {
        public void sendValue(String str);
    }

    private DialogListener dialogListener;


    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        dialogListener = (DialogListener) getActivity();
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.insert_fragment, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        etString = view.findViewById(R.id.et_string);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_confirm) {
            String str = etString.getText().toString();
            dialogListener.sendValue(str);
            this.dismiss();
        }


    }
}
