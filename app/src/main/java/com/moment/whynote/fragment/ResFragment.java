package com.moment.whynote.fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moment.whynote.R;
import com.moment.whynote.data.ResData;
import com.moment.whynote.viewmodel.ResViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ResFragment extends Fragment {
//    private final static String TAG = "ResFragment.class";
    private RecyclerView recyclerView;
    private ResAdapter adapter;
    private final ResViewModel resViewModel = new ResViewModel();


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.res_fragment, container, false);
        recyclerView = view.findViewById(R.id.res_fragment_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }


    /**
     * 监听Livedata<List<ResData>>,当数据发生变化时，更新UI
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
    static class ResHolder extends RecyclerView.ViewHolder{
        public ResHolder(@NonNull @NotNull View itemView) {
            super(itemView);
        }

        TextView tv_title = itemView.findViewById(R.id.tv_title);
        TextView tv_desc = itemView.findViewById(R.id.tv_desc);

        public void bind(ResData data){
            tv_title.setText(data.title);
            tv_desc.setText(data.desc);
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
     * @param dataList 数据列表
     */
    private void updateUI(List<ResData> dataList) {
        adapter = new ResAdapter(dataList);
        recyclerView.setAdapter(adapter);
    }


}
