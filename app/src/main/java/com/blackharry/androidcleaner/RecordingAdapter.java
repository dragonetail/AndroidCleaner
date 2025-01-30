package com.blackharry.androidcleaner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.content.Context;
import java.util.Date;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.RecordingViewHolder> {

    private final List<File> recordings;
    private final Context context;

    public RecordingAdapter(List<File> recordings, Context context) {
        this.recordings = recordings;
        this.context = context;
    }

    @NonNull
    @Override
    public RecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recording, parent, false);
        return new RecordingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingViewHolder holder, int position) {
        File recording = recordings.get(position);
        
        // 设置文件名
        holder.recordingName.setText(recording.getName());
        
        // 格式化并设置日期
        String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", 
            new Date(recording.lastModified())).toString();
        
        // 格式化文件大小
        String size = Formatter.formatFileSize(context, recording.length());
        
        holder.recordingDate.setText(date + " | " + size);

        // 添加点击事件
        holder.itemView.setOnClickListener(v -> {
            // 处理录音文件点击事件
            handleRecordingClick(recording);
        });
    }

    private void handleRecordingClick(File recording) {
        // TODO: 实现录音文件的播放功能
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    static class RecordingViewHolder extends RecyclerView.ViewHolder {
        TextView recordingName;
        TextView recordingDate;

        RecordingViewHolder(View itemView) {
            super(itemView);
            recordingName = itemView.findViewById(R.id.recordingName);
            recordingDate = itemView.findViewById(R.id.recordingDate);
        }
    }
} 