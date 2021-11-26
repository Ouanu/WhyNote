package com.moment.whynote.fragment;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView recyclerView;
    private List<String> uriList = new ArrayList<>();
    private UriAdapter adapter;


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.detail_fragment, container, false);
        recyclerView = view.findViewById(R.id.rv_uris_fragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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

    /**
     * 重写OnTouch事件，触摸滑动不弹出键盘输入，点击才可输入
     */
    private void etDescSetOnTouchListener() {
        etDesc.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: ----" + event.getAction() + "   " + flag);

                if (event.getAction() == 0) {
                    flag = true;
                } else if (event.getAction() == 2) {
                    flag = false;
                    etDesc.setFocusable(false);
                } else if (event.getAction() == 1 && flag) {
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
            uriList = utils.getUris(etDesc.getText().toString());
            updateUri();
        }
    }


    private class UriHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public UriHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            textView.setOnClickListener(this);
        }
        TextView textView = itemView.findViewById(R.id.tv_uri);
        private void bind(String uri) {
            textView.setText(uri);
        }

        @Override
        public void onClick(View v) {
            String text = textView.getText().toString();
            ClipboardManager manager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", text);
            manager.setPrimaryClip(mClipData);
            Toast.makeText(getContext(), text + " 已复制成功", Toast.LENGTH_SHORT).show();
            Log.i("xxx", "onClick: " + text);
        }
    }

    private class UriAdapter extends RecyclerView.Adapter<UriHolder> {

        @NonNull
        @NotNull
        @Override
        public UriHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.uri_list_item, parent, false);
            return new UriHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull DetailFragment.UriHolder holder, int position) {
            String uri = uriList.get(position);
            holder.bind(uri);
        }

        @Override
        public int getItemCount() {
            return uriList.size();
        }
    }


    private void updateUri() {
        adapter = new UriAdapter();
        recyclerView.setAdapter(adapter);
    }

}
