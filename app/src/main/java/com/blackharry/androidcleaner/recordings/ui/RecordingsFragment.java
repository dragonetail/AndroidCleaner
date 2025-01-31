package com.blackharry.androidcleaner.recordings.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Set;

public class RecordingsFragment extends Fragment implements RecordingsAdapter.RecordingClickListener {
    private static final String TAG = "RecordingsFragment";
    private RecordingsViewModel viewModel;
    private RecordingsAdapter adapter;
    private View emptyState;
    private BottomAppBar bottomActionBar;
    private RecyclerView recordingsList;
    private Spinner timeRangeSpinner;
    private Spinner sizeRangeSpinner;
    private Spinner sortSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.logMethodEnter(TAG, "onCreate");
        viewModel = new ViewModelProvider(this).get(RecordingsViewModel.class);
        LogUtils.logMethodExit(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        LogUtils.logMethodEnter(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_recordings, container, false);
        
        try {
            initializeViews(view);
            setupRecyclerView();
            setupSpinners();
            observeViewModel();
        } catch (Exception e) {
            LogUtils.logError(TAG, "视图初始化失败", e);
        }
        
        LogUtils.logMethodExit(TAG, "onCreateView");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogUtils.logMethodEnter(TAG, "onViewCreated");
        
        try {
            initializeViews(view);
            setupRecyclerView();
            setupSpinners();
            observeViewModel();
        } catch (Exception e) {
            LogUtils.logError(TAG, "视图初始化失败", e);
        }
    }

    private void initializeViews(View view) {
        LogUtils.logMethodEnter(TAG, "initializeViews");
        
        emptyState = view.findViewById(R.id.empty_state);
        bottomActionBar = view.findViewById(R.id.bottom_action_bar);
        recordingsList = view.findViewById(R.id.recordings_list);
        timeRangeSpinner = view.findViewById(R.id.time_range_spinner);
        sizeRangeSpinner = view.findViewById(R.id.size_range_spinner);
        sortSpinner = view.findViewById(R.id.sort_spinner);

        view.findViewById(R.id.select_all_button).setOnClickListener(v -> {
            // TODO: 实现全选功能
        });

        view.findViewById(R.id.delete_selected_button).setOnClickListener(v -> {
            Set<String> selectedItems = adapter.getSelectedItems();
            if (!selectedItems.isEmpty()) {
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("删除录音")
                    .setMessage(String.format("确定要删除选中的%d个录音文件吗？", selectedItems.size()))
                    .setPositiveButton("删除", (dialog, which) -> {
                        viewModel.deleteRecordings(new ArrayList<>(selectedItems));
                        adapter.clearSelection();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            }
        });
    }

    private void setupRecyclerView() {
        LogUtils.logMethodEnter(TAG, "setupRecyclerView");
        
        adapter = new RecordingsAdapter(this);
        recordingsList.setAdapter(adapter);
        recordingsList.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupSpinners() {
        LogUtils.logMethodEnter(TAG, "setupSpinners");
        
        // 设置时间范围选项
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            new String[]{"全部时间", "最近一周", "最近一月", "最近三月"}
        );
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeRangeSpinner.setAdapter(timeAdapter);

        // 设置大小范围选项
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            new String[]{"全部大小", "小于1MB", "1MB-10MB", "大于10MB"}
        );
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeRangeSpinner.setAdapter(sizeAdapter);

        // 设置排序方式选项
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            new String[]{"时间降序", "时间升序", "大小降序", "大小升序"}
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);
    }

    private void observeViewModel() {
        LogUtils.logMethodEnter(TAG, "observeViewModel");
        
        try {
            viewModel.getRecordings().observe(getViewLifecycleOwner(), recordings -> {
                adapter.submitList(recordings);
                emptyState.setVisibility(recordings.isEmpty() ? View.VISIBLE : View.GONE);
            });

            viewModel.getError().observe(getViewLifecycleOwner(), error -> {
                if (error != null) {
                    Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG).show();
                }
            });

            viewModel.getPlaybackState().observe(getViewLifecycleOwner(), state -> {
                if (state != null) {
                    adapter.updateProgress(state.filePath, state.currentPosition, state.duration);
                }
            });
        } catch (Exception e) {
            LogUtils.logError(TAG, "数据观察设置失败", e);
        }
    }

    @Override
    public void onRecordingClick(RecordingEntity recording) {
        LogUtils.logMethodEnter(TAG, "onRecordingClick");
        viewModel.playRecording(recording.getFilePath());
    }

    @Override
    public void onMoreClick(RecordingEntity recording, View anchor) {
        LogUtils.logMethodEnter(TAG, "onMoreClick");
        // TODO: 显示更多操作菜单
    }

    @Override
    public void onPlayPauseClick(RecordingEntity recording) {
        LogUtils.logMethodEnter(TAG, "onPlayPauseClick");
        if (viewModel.getPlaybackState().getValue() != null &&
            viewModel.getPlaybackState().getValue().isPlaying) {
            viewModel.pausePlayback();
        } else {
            viewModel.resumePlayback();
        }
    }

    @Override
    public void onStopClick(RecordingEntity recording) {
        LogUtils.logMethodEnter(TAG, "onStopClick");
        viewModel.stopPlayback();
    }

    @Override
    public void onSeekTo(RecordingEntity recording, long position) {
        LogUtils.logMethodEnter(TAG, "onSeekTo");
        viewModel.seekTo(position);
    }

    @Override
    public void onSelectionChange(Set<String> selectedItems) {
        LogUtils.logMethodEnter(TAG, "onSelectionChange");
        // TODO: 更新选中项数量显示
    }

    @Override
    public void onSelectionModeChange(boolean inSelectionMode) {
        LogUtils.logMethodEnter(TAG, "onSelectionModeChange");
        bottomActionBar.setVisibility(inSelectionMode ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.logMethodEnter(TAG, "onDestroyView");
        viewModel.stopPlayback();
    }
} 