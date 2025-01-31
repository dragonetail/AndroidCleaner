package com.blackharry.androidcleaner.calls.data;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.utils.FormatUtils;
import com.blackharry.androidcleaner.utils.LogUtils;

public class CallsDebugFragment extends Fragment {
    private static final String TAG = "CallsDebugFragment";
    private TextView debugTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CallsDebugViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            LogUtils.logMethodEnter(TAG, "onCreateView");
            long startTime = System.currentTimeMillis();
            
            LogUtils.d(TAG, "开始初始化界面");
            View view = inflater.inflate(R.layout.fragment_debug_calls, container, false);
            
            debugTextView = view.findViewById(R.id.debugTextView);
            if (debugTextView == null) {
                LogUtils.e(TAG, "界面组件未找到", new IllegalStateException("TextView为空"));
                return view;
            }
            
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            if (swipeRefreshLayout == null) {
                LogUtils.e(TAG, "界面组件未找到", new IllegalStateException("SwipeRefreshLayout为空"));
                return view;
            }
            
            LogUtils.d(TAG, "初始化ViewModel");
            viewModel = new ViewModelProvider(this).get(CallsDebugViewModel.class);
            
            swipeRefreshLayout.setOnRefreshListener(() -> {
                try {
                    LogUtils.d(TAG, "用户下拉刷新");
                    // TODO: 实现刷新逻辑
                    swipeRefreshLayout.setRefreshing(false);
                } catch (Exception e) {
                    LogUtils.logError(TAG, "刷新失败", e);
                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            LogUtils.d(TAG, "开始观察通话记录数据");
            viewModel.getAllCalls().observe(getViewLifecycleOwner(), calls -> {
                try {
                    LogUtils.d(TAG, String.format("收到%d条通话记录", calls.size()));
                    long updateStartTime = System.currentTimeMillis();
                    
                    StringBuilder builder = new StringBuilder();
                    builder.append("通话记录总数: ").append(calls.size()).append("\n");
                    
                    long totalDuration = 0;
                    long totalSize = 0;
                    for (CallEntity call : calls) {
                        totalDuration += call.callDuration;
                        totalSize += call.recordingFilesize;
                    }
                    
                    builder.append("总通话时长: ").append(FormatUtils.formatDuration(totalDuration)).append("\n");
                    builder.append("总录音大小: ").append(FormatUtils.formatFileSize(requireContext(), totalSize)).append("\n\n");

                    LogUtils.d(TAG, String.format("总通话时长: %d秒, 总录音大小: %d字节", totalDuration, totalSize));

                    for (CallEntity call : calls) {
                        builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                        builder.append("ID: ").append(call.id).append("\n");
                        builder.append("电话号码: ").append(call.phoneNumber).append("\n");
                        builder.append("联系人ID: ").append(call.contactId != null ? call.contactId : "未知").append("\n");
                        builder.append("通话时间: ").append(FormatUtils.formatDate(call.callTime)).append("\n");
                        builder.append("通话时长: ").append(FormatUtils.formatDuration(call.callDuration)).append("\n");
                        builder.append("录音信息:\n");
                        builder.append("  文件名: ").append(call.recordingFilename).append("\n");
                        builder.append("  路径: ").append(call.recordingFilepath).append("\n");
                        builder.append("  大小: ").append(FormatUtils.formatFileSize(requireContext(), call.recordingFilesize)).append("\n");
                        builder.append("  创建时间: ").append(FormatUtils.formatDate(call.recordingCreatedTime)).append("\n");
                        builder.append("\n");
                        
                        LogUtils.v(TAG, String.format("处理通话记录 - ID: %d, 号码: %s, 时长: %d秒", 
                            call.id, call.phoneNumber, call.callDuration));
                    }
                    debugTextView.setText(builder.toString());
                    
                    LogUtils.logPerformance(TAG, "更新通话记录显示", updateStartTime);
                } catch (Exception e) {
                    LogUtils.logError(TAG, "更新通话记录显示失败", e);
                }
            });

            LogUtils.logPerformance(TAG, "Fragment初始化", startTime);
            LogUtils.logMethodExit(TAG, "onCreateView");
            return view;
        } catch (Exception e) {
            LogUtils.logError(TAG, "创建视图失败", e);
            return null;
        }
    }
} 