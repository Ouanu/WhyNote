package com.moment.whynote.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.moment.whynote.R;
import com.moment.whynote.data.ResData;
import com.moment.whynote.viewmodel.ResViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ResFragment extends Fragment implements View.OnClickListener {
    //    private final static String TAG = "ResFragment.class";
    private RecyclerView recyclerView;
    private ResAdapter adapter;
    private final ResViewModel resViewModel = new ResViewModel();
    private TextView title;
    private FloatingActionButton insertBtn;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.insert_btn) {
            new Thread(()->{
                InsertFragment fragment = new InsertFragment();
                fragment.show(getParentFragmentManager(), "INSERT_FRAGMENT");
            }).start();
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
        recyclerView = view.findViewById(R.id.res_fragment_list);
        title = view.findViewById(R.id.title);
        insertBtn = view.findViewById(R.id.insert_btn);
        insertBtn.setOnClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        manager = this.getParentFragmentManager();

        return view;
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
        TextView tv_desc = itemView.findViewById(R.id.tv_desc);
        ResData data;

        public void bind(ResData data) {
            tv_title.setText(data.title);
            tv_desc.setText(data.desc);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            this.data = data;
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putInt("primaryKey", data.uid);
            resCallback.onFragmentSelected(bundle);
        }

        @Override
        public boolean onLongClick(View v) {
            Toast.makeText(itemView.getContext(), "长按----", Toast.LENGTH_SHORT).show();
            MenuFragment menuFragment = new MenuFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("primaryKey", data.uid);
            menuFragment.setArguments(bundle);
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
            System.out.println("data==========" + position);
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
