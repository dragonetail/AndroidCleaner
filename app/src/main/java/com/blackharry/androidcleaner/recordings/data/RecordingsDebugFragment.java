package com.blackharry.androidcleaner.recordings.data;

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
import com.blackharry.androidcleaner.recordings.RecordingsViewModel;

public class RecordingsDebugFragment extends Fragment {
    private TextView debugTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecordingsViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debug_recordings, container, false);
        
        debugTextView = view.findViewById(R.id.debugTextView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        viewModel = new ViewModelProvider(this).get(RecordingsViewModel.class);
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshRecordings();
            swipeRefreshLayout.setRefreshing(false);
        });

        viewModel.getRecordings().observe(getViewLifecycleOwner(), recordings -> {
            StringBuilder builder = new StringBuilder();
            builder.append("录音文件总数: ").append(recordings.size()).append("\n");
            
            long totalSize = 0;
            long totalDuration = 0;
            for (RecordingEntity recording : recordings) {
                totalSize += recording.getFileSize();
                totalDuration += recording.getDuration();
            }
            
            builder.append("总大小: ").append(FormatUtils.formatFileSize(requireContext(), totalSize)).append("\n");
            builder.append("总时长: ").append(FormatUtils.formatDuration(totalDuration)).append("\n\n");

            for (RecordingEntity recording : recordings) {
                builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                builder.append("ID: ").append(recording.getId()).append("\n");
                builder.append("文件名: ").append(recording.getFileName()).append("\n");
                builder.append("路径: ").append(recording.getFilePath()).append("\n");
                builder.append("大小: ").append(FormatUtils.formatFileSize(requireContext(), recording.getFileSize())).append("\n");
                builder.append("创建时间: ").append(FormatUtils.formatDate(recording.getCreatedTime())).append("\n");
                builder.append("时长: ").append(FormatUtils.formatDuration(recording.getDuration())).append("\n");
                builder.append("\n");
            }
            debugTextView.setText(builder.toString());
        });

        return view;
    }
} 