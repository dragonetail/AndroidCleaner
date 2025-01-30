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

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.RecordingViewHolder> {
    private List<File> recordings = new ArrayList<>();
    private final Context context;

    public RecordingAdapter(Context context) {
        this.context = context;
    }

    public void setRecordings(List<File> recordings) {
        this.recordings = recordings;
        notifyDataSetChanged();
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
        holder.recordingName.setText(recording.getName());
        holder.recordingDate.setText(String.valueOf(recording.lastModified()));
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