package com.blackharry.androidcleaner.recordings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.blackharry.androidcleaner.R;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.blackharry.androidcleaner.utils.FormatUtils;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import com.blackharry.androidcleaner.utils.LogUtils;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

public class RecordingAdapter extends ListAdapter<RecordingEntity, RecordingAdapter.RecordingViewHolder> {
    private static final String TAG = "RecordingAdapter";
    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(RecordingEntity recording);
    }

    public RecordingAdapter(Context context, OnItemClickListener listener) {
        super(new RecordingDiffCallback());
        LogUtils.logMethodEnter(TAG, "构造函数");
        this.context = context;
        this.listener = listener;
        LogUtils.logMethodExit(TAG, "构造函数");
    }

    @NonNull
    @Override
    public RecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            LogUtils.logMethodEnter(TAG, "onCreateViewHolder");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recording, parent, false);
            RecordingViewHolder holder = new RecordingViewHolder(view);
            LogUtils.logMethodExit(TAG, "onCreateViewHolder");
            return holder;
        } catch (Exception e) {
            LogUtils.logError(TAG, "创建ViewHolder失败", e);
            throw e;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingViewHolder holder, int position) {
        try {
            LogUtils.logMethodEnter(TAG, "onBindViewHolder");
            RecordingEntity recording = getItem(position);
            LogUtils.d(TAG, String.format("绑定录音数据 - 位置: %d, 文件名: %s", position, recording.getFileName()));
            
            holder.bind(recording, listener);
            
            LogUtils.logMethodExit(TAG, "onBindViewHolder");
        } catch (Exception e) {
            LogUtils.logError(TAG, "绑定ViewHolder失败", e);
        }
    }

    static class RecordingViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "RecordingViewHolder";
        private final TextView recordingName;
        private final TextView recordingDate;
        private final SimpleDateFormat dateFormat;

        RecordingViewHolder(View itemView) {
            super(itemView);
            LogUtils.logMethodEnter(TAG, "构造函数");
            
            recordingName = itemView.findViewById(R.id.recordingName);
            recordingDate = itemView.findViewById(R.id.recordingDate);
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            
            if (recordingName == null || recordingDate == null) {
                LogUtils.e(TAG, "界面组件未找到", new IllegalStateException("必要的TextView为空"));
            }
            
            LogUtils.logMethodExit(TAG, "构造函数");
        }

        void bind(RecordingEntity recording, OnItemClickListener listener) {
            try {
                LogUtils.logMethodEnter(TAG, "bind");
                
                recordingName.setText(recording.getFileName());
                recordingDate.setText(String.format("%s | %s | %s", 
                    dateFormat.format(new Date(recording.getCreatedTime())), 
                    FormatUtils.formatFileSize(itemView.getContext(), recording.getFileSize()), 
                    FormatUtils.formatDuration(recording.getDuration())));

                itemView.setOnClickListener(v -> {
                    LogUtils.d(TAG, String.format("点击录音项: %s", recording.getFileName()));
                    listener.onItemClick(recording);
                });
                
                LogUtils.logMethodExit(TAG, "bind");
            } catch (Exception e) {
                LogUtils.logError(TAG, "绑定数据失败", e);
            }
        }
    }

    private static class RecordingDiffCallback extends DiffUtil.ItemCallback<RecordingEntity> {
        private static final String TAG = "RecordingDiffCallback";
        
        @Override
        public boolean areItemsTheSame(@NonNull RecordingEntity oldItem, @NonNull RecordingEntity newItem) {
            boolean same = oldItem.getFilePath().equals(newItem.getFilePath());
            LogUtils.d(TAG, String.format("比较项是否相同: %b, 路径: %s", same, oldItem.getFilePath()));
            return same;
        }

        @Override
        public boolean areContentsTheSame(@NonNull RecordingEntity oldItem, @NonNull RecordingEntity newItem) {
            boolean same = oldItem.equals(newItem);
            LogUtils.d(TAG, String.format("比较内容是否相同: %b, 文件: %s", same, oldItem.getFileName()));
            return same;
        }
    }
} 