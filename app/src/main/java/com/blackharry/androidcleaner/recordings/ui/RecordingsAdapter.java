package com.blackharry.androidcleaner.recordings.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import com.blackharry.androidcleaner.common.utils.FormatUtils;
import com.google.android.material.slider.Slider;
import java.util.HashSet;
import java.util.Set;

public class RecordingsAdapter extends ListAdapter<RecordingEntity, RecordingsAdapter.ViewHolder> {
    private static final String TAG = "RecordingsAdapter";
    private final RecordingClickListener listener;
    private boolean selectionMode = false;
    private final Set<String> selectedItems = new HashSet<>();
    private int expandedPosition = -1;
    private RecyclerView recyclerView;

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

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
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
        
        // 设置基本信息
        holder.recordingName.setText(recording.getFileName());
        holder.recordingInfo.setText(String.format("%s • %s",
                FormatUtils.formatFileSize(holder.itemView.getContext(), recording.getFileSize()),
                FormatUtils.formatDateTime(recording.getCreationTime())));

        // 设置选择框状态
        holder.checkbox.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        holder.checkbox.setChecked(selectedItems.contains(recording.getFilePath()));

        // 设置展开状态
        boolean isExpanded = position == expandedPosition;
        holder.playbackControls.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(recording.getFilePath());
                holder.checkbox.setChecked(selectedItems.contains(recording.getFilePath()));
            } else {
                if (expandedPosition >= 0) {
                    notifyItemChanged(expandedPosition);
                }
                expandedPosition = isExpanded ? -1 : position;
                notifyItemChanged(position);
                if (!isExpanded) {
                    listener.onRecordingClick(recording);
                }
            }
        });

        // 设置长按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (!selectionMode) {
                selectionMode = true;
                toggleSelection(recording.getFilePath());
                notifyDataSetChanged();
                listener.onSelectionModeChange(true);
                return true;
            }
            return false;
        });

        // 设置更多按钮点击事件
        holder.moreButton.setOnClickListener(v -> {
            listener.onMoreClick(recording, v);
        });

        // 设置播放控制
        if (isExpanded) {
            holder.playPauseButton.setOnClickListener(v -> {
                listener.onPlayPauseClick(recording);
                holder.playPauseButton.setImageResource(
                    holder.playPauseButton.getTag() == "playing" ?
                    R.drawable.ic_play : R.drawable.ic_pause
                );
                holder.playPauseButton.setTag(
                    holder.playPauseButton.getTag() == "playing" ?
                    "paused" : "playing"
                );
            });

            holder.stopButton.setOnClickListener(v -> {
                listener.onStopClick(recording);
                holder.playPauseButton.setImageResource(R.drawable.ic_play);
                holder.playPauseButton.setTag("paused");
            });

            holder.playbackProgress.addOnChangeListener((slider, value, fromUser) -> {
                if (fromUser) {
                    listener.onSeekTo(recording, (long) value);
                }
            });
        }
    }

    public void toggleSelection(String filePath) {
        if (selectedItems.contains(filePath)) {
            selectedItems.remove(filePath);
        } else {
            selectedItems.add(filePath);
        }
        listener.onSelectionChange(selectedItems);
    }

    public void clearSelection() {
        selectedItems.clear();
        selectionMode = false;
        notifyDataSetChanged();
        listener.onSelectionModeChange(false);
    }

    public Set<String> getSelectedItems() {
        return new HashSet<>(selectedItems);
    }

    public void updateProgress(String filePath, long currentPosition, long duration) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i).getFilePath().equals(filePath)) {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(i);
                if (holder instanceof ViewHolder) {
                    ViewHolder vh = (ViewHolder) holder;
                    vh.playbackProgress.setValue((currentPosition * 100f) / duration);
                    vh.currentTime.setText(FormatUtils.formatDuration(currentPosition));
                    vh.totalTime.setText(FormatUtils.formatDuration(duration));
                }
                break;
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox checkbox;
        final ImageView recordingIcon;
        final TextView recordingName;
        final TextView recordingInfo;
        final ImageButton moreButton;
        final View playbackControls;
        final Slider playbackProgress;
        final TextView currentTime;
        final TextView totalTime;
        final ImageButton playPauseButton;
        final ImageButton stopButton;

        ViewHolder(View view) {
            super(view);
            checkbox = view.findViewById(R.id.checkbox);
            recordingIcon = view.findViewById(R.id.recording_icon);
            recordingName = view.findViewById(R.id.recording_name);
            recordingInfo = view.findViewById(R.id.recording_info);
            moreButton = view.findViewById(R.id.more_button);
            playbackControls = view.findViewById(R.id.playback_controls);
            playbackProgress = view.findViewById(R.id.playback_progress);
            currentTime = view.findViewById(R.id.current_time);
            totalTime = view.findViewById(R.id.total_time);
            playPauseButton = view.findViewById(R.id.play_pause_button);
            stopButton = view.findViewById(R.id.stop_button);
        }
    }

    public interface RecordingClickListener {
        void onRecordingClick(RecordingEntity recording);
        void onMoreClick(RecordingEntity recording, View anchor);
        void onPlayPauseClick(RecordingEntity recording);
        void onStopClick(RecordingEntity recording);
        void onSeekTo(RecordingEntity recording, long position);
        void onSelectionChange(Set<String> selectedItems);
        void onSelectionModeChange(boolean inSelectionMode);
    }
} 