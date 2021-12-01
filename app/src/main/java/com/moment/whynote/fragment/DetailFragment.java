package com.moment.whynote.fragment;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moment.whynote.R;
import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;
import com.moment.whynote.utils.DataUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends Fragment implements View.OnClickListener {
    //    private final static String TAG = "DetailFragment";
    private EditText etDesc;
    private boolean flag = false;
    private final DataUtils utils = new DataUtils();
    private RecyclerView recyclerView;
    private List<String> uriList = new ArrayList<>();
    private EditText etTitle;
    private Method method;
    private final Class<EditText> cls = EditText.class;
    private final ResRepository repository = ResRepository.getInstance();
    private ResData data = null;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
        etTitle = view.findViewById(R.id.et_title);
        etDesc = view.findViewById(R.id.et_desc);
        ImageButton btnGetUrl = view.findViewById(R.id.btn_get_uri);
        /*
          初始化title、desc的数据
         */
        if (bundle != null) {
            new Thread(() -> {
//                从bundle获取“主键”
                if(null != repository.getResDataByUid(bundle.getInt("primaryKey"))) {
                    data = repository.getResDataByUid(bundle.getInt("primaryKey"));
                } else {
                    data = repository.getResDataByUpdateDate(bundle.getLong("updateDate"));
                }
                System.out.println(data.toString());
//                初始化数据
                etTitle.setText(data.title);
                etDesc.setText(data.desc);
            }).start();

        }
        etSetOnTouchListener();
        btnGetUrl.setOnClickListener(this);
        /*
        隐藏软键盘
         */
        try {
            method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(etDesc, false);
            method.invoke(etTitle, false);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 重写OnTouch事件，触摸滑动不弹出键盘输入，点击才可输入
     */
    @SuppressLint("ClickableViewAccessibility")
    private void etSetOnTouchListener() {
        etDesc.setOnTouchListener((v, event) -> {
            if (event.getAction() == 0) {
                flag = true;
            } else if (event.getAction() == 2) {
                //触摸事件：不弹出键盘
                flag = false;
                try {
                    etDesc.setCursorVisible(false);
                    method.invoke(etDesc, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (event.getAction() == 1 && flag) {
                //点击事件，弹出键盘
                try {
                    etDesc.setCursorVisible(true);
                    method.invoke(etDesc, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        });


        //同上
        etTitle.setOnTouchListener((v, event) -> {
            if (event.getAction() == 0) {
                flag = true;
            } else if (event.getAction() == 2) {
                flag = false;
                try {
                    method.invoke(etTitle, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (event.getAction() == 1 && flag) {
                try {
                    method.invoke(etTitle, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        });
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_get_uri) {
            uriList = utils.getUris(etDesc.getText().toString());
            new Thread(() -> data.uri = utils.getUriString(uriList)).start();
            updateUri();
            recyclerView.requestFocus();
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
            System.out.println("onclick.............");
            String text = textView.getText().toString();
            if (null != requireContext().getSystemService(Context.CLIPBOARD_SERVICE)) {
                ClipboardManager manager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Label", text);
                manager.setPrimaryClip(mClipData);
                Toast.makeText(getContext(), text + " 已复制成功", Toast.LENGTH_SHORT).show();
            }
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

    /**
     * 更新UI
     */
    private void updateUri() {
        UriAdapter adapter = new UriAdapter();
        recyclerView.setAdapter(adapter);
    }


    /**
     * 返回保存已修改的数据
     */
    @Override
    public void onStop() {
        super.onStop();
        System.out.println(data.title);
        //判断标题、内容是否同时为空，若为空则删除该data
        if (etTitle.getText().toString().equals("") && etDesc.getText().toString().equals("")) {
            new Thread(() -> repository.deleteResData(data)).start();
            //否则，检查data的数据与textview中数据是否相同，不相同则更新
        } else if (!data.title.equals(etTitle.getText().toString()) ||
                !data.desc.equals(etDesc.getText().toString())) {
            data.title = etTitle.getText().toString();
            data.desc = etDesc.getText().toString();
            data.updateDate = System.currentTimeMillis();
            new Thread(() -> repository.upResData(data)).start();
        }

    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return super.onCreateAnimation(transit, enter, nextAnim);
    }
}
