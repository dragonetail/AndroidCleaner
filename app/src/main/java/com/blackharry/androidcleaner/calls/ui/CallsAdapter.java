package com.blackharry.androidcleaner.calls.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.calls.data.CallEntity;
import com.blackharry.androidcleaner.common.utils.FormatUtils;
import com.google.android.material.card.MaterialCardView;
import java.util.HashSet;
import java.util.Set;

public class CallsAdapter extends ListAdapter<CallEntity, CallsAdapter.CallViewHolder> {
    private static final String TAG = "CallsAdapter";
    private final OnItemClickListener listener;
    private final Set<Long> selectedItems = new HashSet<>();

    public interface OnItemClickListener {
        void onItemClick(CallEntity call);
        void onItemLongClick(CallEntity call);
    }

    public CallsAdapter(OnItemClickListener listener) {
        super(new CallDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call, parent, false);
        return new CallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
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

    static class CallViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView phoneNumber;
        private final TextView callTime;
        private final TextView duration;
        private final TextView recordingInfo;

        public CallViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            phoneNumber = itemView.findViewById(R.id.phone_number);
            callTime = itemView.findViewById(R.id.call_time);
            duration = itemView.findViewById(R.id.duration);
            recordingInfo = itemView.findViewById(R.id.recording_info);
        }

        public void bind(CallEntity call, OnItemClickListener listener, boolean isSelected) {
            String displayName = call.getName();
            if (displayName == null || displayName.isEmpty()) {
                phoneNumber.setText(call.getNumber());
            } else {
                phoneNumber.setText(String.format("%s\n%s", displayName, call.getNumber()));
            }
            
            callTime.setText(FormatUtils.formatDateTime(call.getDate()));
            duration.setText(FormatUtils.formatDuration(call.getDuration()));
            
            if (call.getRecordingPath() != null) {
                recordingInfo.setVisibility(View.VISIBLE);
                recordingInfo.setText(String.format("录音大小：%s", 
                    FormatUtils.formatFileSize(call.getRecordingSize())));
            } else {
                recordingInfo.setVisibility(View.GONE);
            }

            cardView.setChecked(isSelected);
            
            itemView.setOnClickListener(v -> listener.onItemClick(call));
            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(call);
                return true;
            });
        }
    }

    static class CallDiffCallback extends DiffUtil.ItemCallback<CallEntity> {
        @Override
        public boolean areItemsTheSame(@NonNull CallEntity oldItem, @NonNull CallEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CallEntity oldItem, @NonNull CallEntity newItem) {
            return oldItem.equals(newItem);
        }
    }
} 