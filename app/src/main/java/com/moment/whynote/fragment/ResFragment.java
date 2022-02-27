package com.moment.whynote.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.moment.whynote.R;
import com.moment.whynote.data.ResData;
import com.moment.whynote.database.ResRepository;
import com.moment.whynote.utils.DataUtils;
import com.moment.whynote.utils.OCRImageUtil;
import com.moment.whynote.view.OTextView;
import com.moment.whynote.viewmodel.ResViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class ResFragment extends Fragment implements View.OnClickListener {
    //    private final static String TAG = "ResFragment.class";
    private RecyclerView recyclerView;
    private ResAdapter adapter;
    private final ResViewModel resViewModel = new ResViewModel();
    private ResRepository repository;
    @SuppressLint("StaticFieldLeak")
    private static final DataUtils utils = new DataUtils();
    //布局管理
    private RecyclerView.LayoutManager layoutManager = null;

    private ImageView ivChangeLayout;
    Bundle bundle;

    private static boolean showAnimate = true;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onClick(View v) {
        /*
        新建笔记
         */
        if (v.getId() == R.id.insert_btn) {
            new Thread(() -> {
                ResData newData = new ResData();
                @SuppressLint("SdCardPath")
                File file = new File(getString(R.string.ExternalStoragePath) , String.valueOf(newData.updateDate));
                if (!file.exists() || !file.isDirectory())
                    file.mkdirs();
                newData.dirPath = file.getAbsolutePath();
                repository.insertData(newData);
                Bundle bundle = new Bundle();
                bundle.putInt("primaryKey", newData.uid);
                bundle.putLong("updateDate", newData.updateDate);
                bundle.putString("dirPath", file.getAbsolutePath());
                resCallback.onFragmentSelected(bundle);
            }).start();
        } else if (v.getId() == R.id.iv_change_layout) {
            /*
            更换布局，修改配置文件
             */
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            int choice = sharedPreferences.getInt("LayoutManager", 0);
            showAnimate = true;
            if (choice == 0) {
                getLayoutManager(1);
                bundle.putInt("LayoutManager", 1);
                editor.putInt("LayoutManager", 1);
            } else {
                getLayoutManager(0);
                bundle.putInt("LayoutManager", 0);
                editor.putInt("LayoutManager", 0);
            }
            editor.apply();
        } else if (v.getId() == R.id.title_2) {
//            ConnectFragment connectFragment = new ConnectFragment();
//            manager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                    .setCustomAnimations(R.anim.no_slide, R.anim.from_bottom);
//            connectFragment.show(manager, "NULL");
            OCRImageUtil.getInstance().execute(getActivity().getContentResolver(), Uri.parse("file:///sdcard/Android/data/com.moment.whynote/files/Documents/text2.jpg"));

        }
    }


    public interface ResListener {
        void onFragmentSelected(Bundle bundle);
    }

    private static ResListener resCallback;
    private static FragmentManager manager = null;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            resCallback = (ResListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.res_fragment, container, false);
        initView(view);
        return view;
    }

    /**
     * 初始化控件
     *
     * @param view 获取布局视图
     */
    private void initView(View view) {
        bundle = getArguments();
        recyclerView = view.findViewById(R.id.res_fragment_list);
        FloatingActionButton insertBtn = view.findViewById(R.id.insert_btn);
        insertBtn.setOnClickListener(this);
        ivChangeLayout = view.findViewById(R.id.iv_change_layout);
        ivChangeLayout.setOnClickListener(this);
        assert bundle != null;
        getLayoutManager(bundle.getInt("LayoutManager"));
        manager = this.getParentFragmentManager();
        TextView title2 = view.findViewById(R.id.title_2);
        title2.setOnClickListener(this);
        repository = ResRepository.getInstance();
    }

    /**
     * 获取布局
     *
     * @param choice 布局设置选项
     */
    private void getLayoutManager(int choice) {
        if (choice == 0) {
            //线性布局
            layoutManager = new LinearLayoutManager(getContext());
            ivChangeLayout.setImageResource(R.drawable.ic_baseline_list_24);
        } else if (choice == 1) {
            //瀑布布局
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            ivChangeLayout.setImageResource(R.drawable.ic_baseline_grid_on_24);
        }
        /*
        数据更新动画，只有切换布局或更新数据才会播放，点击item退出不会播放
         */
        if (showAnimate) {
            LayoutAnimationController controller = new LayoutAnimationController(AnimationUtils.loadAnimation(getContext(), R.anim.animate));
            recyclerView.setLayoutAnimation(controller);
            showAnimate = false;
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


    /**
     * 监听Livedata<List<ResData>>,当数据发生变化时，更新UI
     *
     * @param view fragment视图布局
     */
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        resViewModel.dataList.observe(getViewLifecycleOwner(), this::updateUI);
    }

    /**
     * Holder
     */
    static class ResHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ResHolder(@NonNull @NotNull View itemView) {
            super(itemView);
        }

        TextView tv_title = itemView.findViewById(R.id.tv_title);
        OTextView tv_desc = itemView.findViewById(R.id.tv_desc);
        TextView tv_date = itemView.findViewById(R.id.tv_date);
        ResData data;

        public void bind(ResData data) {
            if (data.title.equals("")) {
                tv_desc.setText(data.desc);
                tv_desc.insertImage(data.desc);
                tv_title.setText(utils.getNowDateDefault(data.updateDate));
            } else {
                tv_title.setText(data.title);
                tv_desc.setText(data.desc);
                tv_desc.insertImage(data.desc);
            }
            tv_date.setText(utils.getNowDateDefault(data.updateDate));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            this.data = data;
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putInt("primaryKey", data.uid);
            bundle.putString("dirPath", data.dirPath);
            resCallback.onFragmentSelected(bundle);
        }

        @Override
        public boolean onLongClick(View v) {
            MenuFragment menuFragment = new MenuFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("primaryKey", data.uid);
            menuFragment.setArguments(bundle);
            manager.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .setCustomAnimations(R.anim.no_slide, R.anim.from_bottom);
            menuFragment.show(manager, "NULL");
            return false;
        }
    }

    /**
     * 构建适配器
     */
    private class ResAdapter extends RecyclerView.Adapter<ResHolder> {
        private final List<ResData> dataList;

        public ResAdapter(List<ResData> dataList) {
            this.dataList = dataList;
        }

        @NonNull
        @NotNull
        @Override
        public ResHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.res_fragment_item, parent, false);
            return new ResHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull ResFragment.ResHolder holder, int position) {
            ResData data = dataList.get(position);
            holder.bind(data);
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

    }

    /**
     * 更新UI
     *
     * @param dataList 数据列表
     */
    private void updateUI(List<ResData> dataList) {
        adapter = new ResAdapter(dataList);
        recyclerView.setAdapter(adapter);
    }


}
