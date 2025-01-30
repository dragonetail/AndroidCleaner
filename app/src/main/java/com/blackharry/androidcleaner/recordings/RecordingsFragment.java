package com.blackharry.androidcleaner.recordings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import com.blackharry.androidcleaner.R;

public class RecordingsFragment extends Fragment {
    private RecordingsViewModel viewModel;
    private RecordingAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recordings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(RecordingsViewModel.class);
        
        // 设置RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecordingAdapter(getContext());
        recyclerView.setAdapter(adapter);
        
        // 观察数据变化
        viewModel.getRecordings().observe(getViewLifecycleOwner(), recordings -> {
            adapter.setRecordings(recordings);
        });
        
        // 加载录音文件
        viewModel.loadRecordings();
    }
} 