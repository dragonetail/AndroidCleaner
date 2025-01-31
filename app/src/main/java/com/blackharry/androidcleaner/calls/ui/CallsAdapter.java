package com.blackharry.androidcleaner.calls.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.calls.data.CallEntity;
import com.blackharry.androidcleaner.common.utils.FormatUtils;
import android.provider.CallLog;
import java.util.HashSet;
import java.util.Set;

public class CallsAdapter extends ListAdapter<CallEntity, CallsAdapter.ViewHolder> {
    private final OnItemClickListener listener;
    private final Set<Long> selectedItems = new HashSet<>();

    public CallsAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<CallEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull CallEntity oldItem, @NonNull CallEntity newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull CallEntity oldItem, @NonNull CallEntity newItem) {
                return oldItem.getNumber().equals(newItem.getNumber()) &&
                       oldItem.getName().equals(newItem.getName()) &&
                       oldItem.getDate() == newItem.getDate() &&
                       oldItem.getDuration() == newItem.getDuration() &&
                       oldItem.getType() == newItem.getType();
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallEntity call = getItem(position);
        holder.bind(call, listener, selectedItems.contains(call.getId()));
    }

    public void toggleSelection(long id) {
        if (selectedItems.contains(id)) {
            selectedItems.remove(id);
        } else {
            selectedItems.add(id);
        }
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (int i = 0; i < getItemCount(); i++) {
            selectedItems.add(getItem(i).getId());
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public Set<Long> getSelectedItems() {
        return new HashSet<>(selectedItems);
    }

    public interface OnItemClickListener {
        void onItemClick(CallEntity call);
        void onItemLongClick(CallEntity call);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView numberText;
        private final TextView dateText;
        private final TextView durationText;
        private final ImageView typeIcon;
        private final ImageView recordingIcon;
        private final View itemView;

        ViewHolder(View view) {
            super(view);
            itemView = view;
            nameText = view.findViewById(R.id.text_name);
            numberText = view.findViewById(R.id.text_number);
            dateText = view.findViewById(R.id.text_date);
            durationText = view.findViewById(R.id.text_duration);
            typeIcon = view.findViewById(R.id.icon_type);
            recordingIcon = view.findViewById(R.id.icon_recording);
        }

        void bind(CallEntity call, OnItemClickListener listener, boolean isSelected) {
            // 设置联系人名称/号码
            String displayName = call.getName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = call.getNumber();
                nameText.setVisibility(View.GONE);
                numberText.setText(displayName);
            } else {
                nameText.setVisibility(View.VISIBLE);
                nameText.setText(displayName);
                numberText.setText(call.getNumber());
            }

            // 设置日期和时长
            dateText.setText(FormatUtils.formatDateTime(call.getDate()));
            durationText.setText(FormatUtils.formatDuration(call.getDuration()));

            // 设置通话类型图标
            switch (call.getType()) {
                case CallLog.Calls.INCOMING_TYPE:
                    typeIcon.setImageResource(R.drawable.ic_call_received);
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    typeIcon.setImageResource(R.drawable.ic_call_made);
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    typeIcon.setImageResource(R.drawable.ic_call_missed);
                    break;
                default:
                    typeIcon.setImageResource(R.drawable.ic_call);
            }

            // 设置录音图标
            recordingIcon.setVisibility(
                call.getRecordingPath() != null ? View.VISIBLE : View.GONE
            );

            // 设置选中状态
            itemView.setSelected(isSelected);
            itemView.setBackgroundResource(
                isSelected ? R.drawable.bg_item_selected : R.drawable.bg_item_normal
            );

            // 设置点击事件
            itemView.setOnClickListener(v -> listener.onItemClick(call));
            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(call);
                return true;
            });
        }
    }
} 