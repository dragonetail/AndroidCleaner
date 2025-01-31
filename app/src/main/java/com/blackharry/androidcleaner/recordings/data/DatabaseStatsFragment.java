package com.blackharry.androidcleaner.recordings.data;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.utils.LogUtils;
import com.blackharry.androidcleaner.utils.FormatUtils;

public class DatabaseStatsFragment extends Fragment {
    private static final String TAG = "DatabaseStatsFragment";
    private TextView debugTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseStatsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            LogUtils.logMethodEnter(TAG, "onCreate");
            super.onCreate(savedInstanceState);
            viewModel = new ViewModelProvider(this).get(DatabaseStatsViewModel.class);
            LogUtils.logMethodExit(TAG, "onCreate");
        } catch (Exception e) {
            LogUtils.logError(TAG, "Fragment创建失败", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        try {
            LogUtils.logMethodEnter(TAG, "onCreateView");
            long startTime = System.currentTimeMillis();

            View view = inflater.inflate(R.layout.fragment_debug_stats, container, false);
            
            LogUtils.d(TAG, "开始初始化统计界面组件");
            initializeViews(view);

            viewModel.getDatabaseStats().observe(getViewLifecycleOwner(), stats -> {
                StringBuilder builder = new StringBuilder();
                builder.append("数据库统计\n");
                builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

                // 录音文件统计
                builder.append("【录音文件】\n");
                builder.append("总数: ").append(stats.recordingsCount).append("\n");
                builder.append("总大小: ").append(FormatUtils.formatFileSize(requireContext(), stats.recordingsTotalSize)).append("\n");
                builder.append("总时长: ").append(FormatUtils.formatDuration(stats.recordingsTotalDuration)).append("\n\n");

                // 通话记录统计
                builder.append("【通话记录】\n");
                builder.append("总数: ").append(stats.callsCount).append("\n");
                builder.append("总通话时长: ").append(FormatUtils.formatDuration(stats.callsTotalDuration)).append("\n");
                builder.append("总录音大小: ").append(FormatUtils.formatFileSize(requireContext(), stats.callsRecordingSize)).append("\n\n");

                // 联系人统计
                builder.append("【联系人】\n");
                builder.append("总数: ").append(stats.contactsCount).append("\n");
                builder.append("安全区: ").append(stats.safeZoneCount).append("\n");
                builder.append("临时区: ").append(stats.tempZoneCount).append("\n");
                builder.append("黑名单: ").append(stats.blacklistCount).append("\n");
                builder.append("电话号码总数: ").append(stats.phoneNumbersCount).append("\n\n");

                // 存储统计
                builder.append("【存储统计】\n");
                long totalSize = stats.recordingsTotalSize + stats.callsRecordingSize;
                builder.append("总存储大小: ").append(FormatUtils.formatFileSize(requireContext(), totalSize)).append("\n");
                builder.append("总录音时长: ").append(FormatUtils.formatDuration(stats.recordingsTotalDuration + stats.callsTotalDuration)).append("\n");

                debugTextView.setText(builder.toString());
            });

            swipeRefreshLayout.setOnRefreshListener(() -> {
                viewModel.refresh();
                swipeRefreshLayout.setRefreshing(false);
            });

            LogUtils.logPerformance(TAG, "统计界面初始化", startTime);
            LogUtils.logMethodExit(TAG, "onCreateView");
            return view;
        } catch (Exception e) {
            LogUtils.logError(TAG, "创建视图失败", e);
            return null;
        }
    }

    private void initializeViews(View view) {
        try {
            LogUtils.logMethodEnter(TAG, "initializeViews");
            
            debugTextView = view.findViewById(R.id.debugTextView);
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

            if (debugTextView == null || swipeRefreshLayout == null) {
                LogUtils.e(TAG, "界面组件未找到", new IllegalStateException("必要的TextView为空"));
                return;
            }

            LogUtils.d(TAG, "界面组件初始化完成");
            LogUtils.logMethodExit(TAG, "initializeViews");
        } catch (Exception e) {
            LogUtils.logError(TAG, "初始化界面组件失败", e);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            LogUtils.logMethodEnter(TAG, "onViewCreated");
            super.onViewCreated(view, savedInstanceState);

            observeViewModel();
            refreshStats();

            LogUtils.logMethodExit(TAG, "onViewCreated");
        } catch (Exception e) {
            LogUtils.logError(TAG, "视图创建后初始化失败", e);
        }
    }

    private void observeViewModel() {
        try {
            LogUtils.logMethodEnter(TAG, "observeViewModel");
            
            viewModel.getRecordingsCount().observe(getViewLifecycleOwner(), count -> {
                LogUtils.d(TAG, String.format("录音数量更新: %d", count));
                debugTextView.append("\n录音数量: " + count);
            });

            viewModel.getRecordingsSize().observe(getViewLifecycleOwner(), size -> {
                LogUtils.d(TAG, String.format("录音总大小更新: %d bytes", size));
                debugTextView.append("\n录音总大小: " + FormatUtils.formatFileSize(requireContext(), size));
            });

            viewModel.getCallsCount().observe(getViewLifecycleOwner(), count -> {
                LogUtils.d(TAG, String.format("通话记录数量更新: %d", count));
                debugTextView.append("\n通话记录数量: " + count);
            });

            viewModel.getContactsCount().observe(getViewLifecycleOwner(), count -> {
                LogUtils.d(TAG, String.format("联系人数量更新: %d", count));
                debugTextView.append("\n联系人数量: " + count);
            });

            LogUtils.logMethodExit(TAG, "observeViewModel");
        } catch (Exception e) {
            LogUtils.logError(TAG, "设置数据观察失败", e);
        }
    }

    private void refreshStats() {
        try {
            LogUtils.logMethodEnter(TAG, "refreshStats");
            long startTime = System.currentTimeMillis();
            
            viewModel.refreshStats(() -> {
                LogUtils.logPerformance(TAG, "刷新统计数据", startTime);
            });
            
            LogUtils.logMethodExit(TAG, "refreshStats");
        } catch (Exception e) {
            LogUtils.logError(TAG, "刷新统计数据失败", e);
        }
    }
} 