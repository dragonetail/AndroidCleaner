package com.blackharry.androidcleaner.recordings.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import com.blackharry.androidcleaner.common.utils.FormatUtils;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import java.util.HashSet;
import java.util.Set;

public class RecordingsAdapter extends ListAdapter<RecordingEntity, RecordingsAdapter.ViewHolder> {
    private static final String TAG = "RecordingsAdapter";
    private final RecordingClickListener listener;
    private boolean isSelectionMode = false;
    private final Set<String> selectedItems = new HashSet<>();

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
        LogUtils.logMethodEnter(TAG, "onBindViewHolder");
        RecordingEntity recording = getItem(position);
        
        // 设置文件名
        holder.recordingName.setText(recording.getFileName());
        
        // 设置日期
        holder.recordingDate.setText(FormatUtils.formatDate(recording.getCreationTime()));
        
        // 设置文件大小
        holder.recordingSize.setText(FormatUtils.formatFileSize(holder.itemView.getContext(), recording.getFileSize()));
        
        // 设置时长
        holder.recordingDuration.setText(FormatUtils.formatDuration(recording.getDuration()));

        // 根据选择模式切换显示状态
        updateViewState(holder, recording);

        // 设置播放按钮点击事件
        holder.playButton.setOnClickListener(v -> {
            if (!isSelectionMode) {
                listener.onPlayPauseClick(recording);
            }
        });

        // 设置选择框点击事件
        holder.checkbox.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(recording.getFilePath());
            }
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
                LogUtils.i(TAG, "进入选择模式");
                isSelectionMode = true;
                toggleSelection(recording.getFilePath());
                notifyDataSetChanged(); // 刷新所有项以切换显示状态
                return true;
            }
            return false;
        });

        LogUtils.logMethodExit(TAG, "onBindViewHolder");
    }

    private void updateViewState(@NonNull ViewHolder holder, RecordingEntity recording) {
        boolean isSelected = selectedItems.contains(recording.getFilePath());
        
        // 切换播放按钮和选择框的显示状态
        if (isSelectionMode) {
            holder.playButton.setVisibility(View.GONE);
            holder.checkbox.setVisibility(View.VISIBLE);
        } else {
            holder.playButton.setVisibility(View.VISIBLE);
            holder.checkbox.setVisibility(View.GONE);
        }
        
        // 更新选择状态
        holder.checkbox.setChecked(isSelected);
        holder.itemView.setActivated(isSelected);
    }

    public void setSelectionMode(boolean selectionMode) {
        LogUtils.logMethodEnter(TAG, "setSelectionMode");
        if (this.isSelectionMode != selectionMode) {
            this.isSelectionMode = selectionMode;
            if (!selectionMode) {
                selectedItems.clear();
            }
            notifyDataSetChanged();
        }
        LogUtils.logMethodExit(TAG, "setSelectionMode");
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void toggleSelection(String filePath) {
        LogUtils.logMethodEnter(TAG, "toggleSelection");
        if (selectedItems.contains(filePath)) {
            selectedItems.remove(filePath);
        } else {
            selectedItems.add(filePath);
        }
        notifyDataSetChanged();
        listener.onSelectionChange(selectedItems);
        LogUtils.logMethodExit(TAG, "toggleSelection");
    }

    public boolean isSelected(String filePath) {
        return selectedItems.contains(filePath);
    }

    public void addSelection(String filePath) {
        LogUtils.logMethodEnter(TAG, "addSelection");
        if (selectedItems.add(filePath)) {
            notifyDataSetChanged();
            listener.onSelectionChange(selectedItems);
        }
        LogUtils.logMethodExit(TAG, "addSelection");
    }

    public void removeSelection(String filePath) {
        LogUtils.logMethodEnter(TAG, "removeSelection");
        if (selectedItems.remove(filePath)) {
            notifyDataSetChanged();
            listener.onSelectionChange(selectedItems);
        }
        LogUtils.logMethodExit(TAG, "removeSelection");
    }

    public void clearSelection() {
        LogUtils.logMethodEnter(TAG, "clearSelection");
        if (!selectedItems.isEmpty()) {
            selectedItems.clear();
            notifyDataSetChanged();
            listener.onSelectionChange(selectedItems);
        }
        LogUtils.logMethodExit(TAG, "clearSelection");
    }

    public Set<String> getSelectedItems() {
        return new HashSet<>(selectedItems);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView recordingName;
        final TextView recordingDate;
        final TextView recordingSize;
        final TextView recordingDuration;
        final ImageButton playButton;
        final CheckBox checkbox;

        ViewHolder(View view) {
            super(view);
            recordingName = view.findViewById(R.id.file_name);
            recordingDate = view.findViewById(R.id.file_date);
            recordingSize = view.findViewById(R.id.file_size);
            recordingDuration = view.findViewById(R.id.duration);
            playButton = view.findViewById(R.id.play_button);
            checkbox = view.findViewById(R.id.checkbox);
        }
    }

    public interface RecordingClickListener {
        void onRecordingClick(RecordingEntity recording);
        void onPlayPauseClick(RecordingEntity recording);
        void onSelectionChange(Set<String> selectedItems);
        void onSeekTo(RecordingEntity recording, int progress);
        void onSpeedChange(RecordingEntity recording, float speed);
    }
} 