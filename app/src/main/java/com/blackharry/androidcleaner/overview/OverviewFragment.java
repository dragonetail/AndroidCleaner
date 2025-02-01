package com.blackharry.androidcleaner.overview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class OverviewFragment extends Fragment {
    private static final String TAG = "OverviewFragment";
    private OverviewViewModel viewModel;
    private CircularProgressIndicator storageProgress;
    private TextView storageText;
    private TextView storageDesc;
    private TextView recordingCount;
    private TextView callCount;
    private TextView contactCount;
    private TextView totalDuration;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.logMethodEnter(TAG, "onCreate");
        viewModel = new ViewModelProvider(this).get(OverviewViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        LogUtils.logMethodEnter(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        initViews(view);
        observeViewModel();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.logMethodEnter(TAG, "onResume");
        refreshData();
    }

    private void initViews(View view) {
        LogUtils.logMethodEnter(TAG, "initViews");
        
        storageProgress = view.findViewById(R.id.storage_progress);
        storageText = view.findViewById(R.id.storage_text);
        storageDesc = view.findViewById(R.id.storage_desc);
        recordingCount = view.findViewById(R.id.recording_count);
        callCount = view.findViewById(R.id.call_count);
        contactCount = view.findViewById(R.id.contact_count);
        totalDuration = view.findViewById(R.id.total_duration);
    }

    private void observeViewModel() {
        LogUtils.logMethodEnter(TAG, "observeViewModel");
        
        try {
            // 观察存储使用情况
            viewModel.getStorageUsage().observe(getViewLifecycleOwner(), usage -> {
                storageProgress.setProgress((int) (usage.getUsedPercentage() * 100));
                storageText.setText(usage.getFormattedUsedSize());
                storageDesc.setText(String.format("总空间: %s", usage.getFormattedTotalSize()));
            });

            // 观察统计数据
            viewModel.getStatistics().observe(getViewLifecycleOwner(), stats -> {
                recordingCount.setText(String.valueOf(stats.getRecordingCount()));
                callCount.setText(String.valueOf(stats.getCallCount()));
                contactCount.setText(String.valueOf(stats.getContactCount()));
                totalDuration.setText(String.format("总通话时长：%s", stats.getFormattedTotalDuration()));
            });
        } catch (Exception e) {
            LogUtils.logError(TAG, "数据观察设置失败", e);
        }
    }

    private void refreshData() {
        LogUtils.logMethodEnter(TAG, "refreshData");
        try {
            viewModel.refreshData();
        } catch (Exception e) {
            LogUtils.logError(TAG, "数据刷新失败", e);
        }
    }
} 