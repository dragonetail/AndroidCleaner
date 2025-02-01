package com.blackharry.androidcleaner.recordings.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import com.blackharry.androidcleaner.common.utils.FormatUtils;
import java.util.HashSet;
import java.util.Set;

public class RecordingsAdapter extends ListAdapter<RecordingEntity, RecordingsAdapter.ViewHolder> {
    private static final String TAG = "RecordingsAdapter";
    private final RecordingClickListener listener;
    private final Set<String> selectedItems = new HashSet<>();
    private boolean isSelectionMode = false;

    public RecordingsAdapter(RecordingClickListener listener) {
        super(new DiffUtil.ItemCallback<RecordingEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull RecordingEntity oldItem, @NonNull RecordingEntity newItem) {
                return oldItem.getFilePath().equals(newItem.getFilePath());
            }

            @Override
            public boolean areContentsTheSame(@NonNull RecordingEntity oldItem, @NonNull RecordingEntity newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recording, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecordingEntity recording = getItem(position);
        
        // 设置文件名
        holder.recordingName.setText(recording.getFileName());
        
        // 设置日期
        holder.recordingDate.setText(FormatUtils.formatDate(recording.getCreationTime()));
        
        // 设置文件大小
        holder.recordingSize.setText(FormatUtils.formatFileSize(holder.itemView.getContext(), recording.getFileSize()));
        
        // 设置时长
        holder.recordingDuration.setText(FormatUtils.formatDuration(recording.getDuration()));

        // 设置播放按钮点击事件
        holder.playButton.setOnClickListener(v -> {
            listener.onPlayPauseClick(recording);
        });

        // 设置项目点击事件
        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(recording.getFilePath());
            } else {
                listener.onRecordingClick(recording);
            }
        });

        // 设置长按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                toggleSelection(recording.getFilePath());
            }
            return true;
        });

        // 设置选中状态的视觉反馈
        holder.itemView.setActivated(selectedItems.contains(recording.getFilePath()));
    }

    public void toggleSelection(String filePath) {
        if (selectedItems.contains(filePath)) {
            selectedItems.remove(filePath);
            if (selectedItems.isEmpty()) {
                isSelectionMode = false;
            }
        } else {
            selectedItems.add(filePath);
        }
        listener.onSelectionChange(selectedItems);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
    }

    public Set<String> getSelectedItems() {
        return new HashSet<>(selectedItems);
    }

    public void setSelectionMode(boolean selectionMode) {
        if (this.isSelectionMode != selectionMode) {
            this.isSelectionMode = selectionMode;
            if (!selectionMode) {
                selectedItems.clear();
            }
            notifyDataSetChanged();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView recordingName;
        final TextView recordingDate;
        final TextView recordingSize;
        final TextView recordingDuration;
        final ImageButton playButton;

        ViewHolder(View view) {
            super(view);
            recordingName = view.findViewById(R.id.recording_name);
            recordingDate = view.findViewById(R.id.recording_date);
            recordingSize = view.findViewById(R.id.recording_size);
            recordingDuration = view.findViewById(R.id.recording_duration);
            playButton = view.findViewById(R.id.play_button);
        }
    }

    public interface RecordingClickListener {
        void onRecordingClick(RecordingEntity recording);
        void onPlayPauseClick(RecordingEntity recording);
        void onSelectionChange(Set<String> selectedItems);
    }
} 