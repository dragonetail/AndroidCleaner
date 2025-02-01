package com.blackharry.androidcleaner.recordings.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.blackharry.androidcleaner.common.utils.FormatUtils;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RecordingsFragment extends Fragment implements RecordingsAdapter.RecordingClickListener {
    private static final String TAG = "RecordingsFragment";
    private RecordingsViewModel viewModel;
    private RecordingsAdapter adapter;
    private View emptyState;
    private View normalAppBar;
    private View selectionAppBar;
    private TextView selectionCount;
    private BottomAppBar selectionBottomBar;
    private RecyclerView recordingsList;
    private androidx.appcompat.widget.Toolbar toolbar;
    private boolean isSelectionMode = false;
    private BottomNavigationView selectionBottomNav;

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
            setupToolbar();
            observeViewModel();
            setupBackPressHandler();
        } catch (Exception e) {
            LogUtils.logError(TAG, "视图初始化失败", e);
        }
        
        LogUtils.logMethodExit(TAG, "onCreateView");
        return view;
    }

    private void setupBackPressHandler() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
            new androidx.activity.OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (isSelectionMode) {
                        exitSelectionMode();
                    } else {
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            });
    }

    private void initializeViews(View view) {
        LogUtils.logMethodEnter(TAG, "initializeViews");
        
        emptyState = view.findViewById(R.id.empty_state);
        normalAppBar = view.findViewById(R.id.normal_app_bar);
        selectionAppBar = view.findViewById(R.id.selection_app_bar);
        selectionCount = view.findViewById(R.id.selection_count);
        selectionBottomBar = view.findViewById(R.id.selection_bottom_bar);
        recordingsList = view.findViewById(R.id.recordings_list);
        toolbar = view.findViewById(R.id.toolbar);

        view.findViewById(R.id.close_selection_button).setOnClickListener(v -> exitSelectionMode());
        setupBottomBar();
    }

    private void setupToolbar() {
        LogUtils.logMethodEnter(TAG, "setupToolbar");
        
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            
            // 时间过滤
            if (itemId == R.id.time_all) {
                viewModel.setTimeFilter(RecordingsViewModel.TimeFilter.ALL);
                return true;
            } else if (itemId == R.id.time_today) {
                viewModel.setTimeFilter(RecordingsViewModel.TimeFilter.TODAY);
                return true;
            } else if (itemId == R.id.time_week) {
                viewModel.setTimeFilter(RecordingsViewModel.TimeFilter.WEEK);
                return true;
            } else if (itemId == R.id.time_month) {
                viewModel.setTimeFilter(RecordingsViewModel.TimeFilter.MONTH);
                return true;
            } else if (itemId == R.id.time_quarter) {
                viewModel.setTimeFilter(RecordingsViewModel.TimeFilter.QUARTER);
                return true;
            }
            
            // 时长过滤
            else if (itemId == R.id.duration_1min) {
                viewModel.setDurationFilter(RecordingsViewModel.DurationFilter.MIN_1);
                return true;
            } else if (itemId == R.id.duration_5min) {
                viewModel.setDurationFilter(RecordingsViewModel.DurationFilter.MIN_5);
                return true;
            } else if (itemId == R.id.duration_30min) {
                viewModel.setDurationFilter(RecordingsViewModel.DurationFilter.MIN_30);
                return true;
            } else if (itemId == R.id.duration_2hour) {
                viewModel.setDurationFilter(RecordingsViewModel.DurationFilter.HOUR_2);
                return true;
            } else if (itemId == R.id.duration_longer) {
                viewModel.setDurationFilter(RecordingsViewModel.DurationFilter.LONGER);
                return true;
            }
            
            // 排序方式
            else if (itemId == R.id.sort_time_desc) {
                viewModel.setSortOrder(RecordingsViewModel.SortOrder.TIME_DESC);
                return true;
            } else if (itemId == R.id.sort_time_asc) {
                viewModel.setSortOrder(RecordingsViewModel.SortOrder.TIME_ASC);
                return true;
            } else if (itemId == R.id.sort_size_desc) {
                viewModel.setSortOrder(RecordingsViewModel.SortOrder.SIZE_DESC);
                return true;
            } else if (itemId == R.id.sort_size_asc) {
                viewModel.setSortOrder(RecordingsViewModel.SortOrder.SIZE_ASC);
                return true;
            }
            
            return false;
        });
    }

    private void setupBottomBar() {
        LogUtils.logMethodEnter(TAG, "setupBottomBar");
        
        selectionBottomBar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_share) {
                shareSelectedRecordings();
                return true;
            } else if (itemId == R.id.action_delete) {
                deleteSelectedRecordings();
                return true;
            } else if (itemId == R.id.action_more) {
                showMoreOptions();
                return true;
            }
            return false;
        });
    }

    private void showMoreOptions() {
        LogUtils.logMethodEnter(TAG, "showMoreOptions");
        
        PopupMenu popup = new PopupMenu(requireContext(), selectionBottomBar);
        popup.getMenu().add(Menu.NONE, 1, Menu.NONE, "全选");
        popup.getMenu().add(Menu.NONE, 2, Menu.NONE, "重命名");
        popup.getMenu().add(Menu.NONE, 3, Menu.NONE, "详情");
        popup.getMenu().add(Menu.NONE, 4, Menu.NONE, "添加标签");
        popup.getMenu().add(Menu.NONE, 5, Menu.NONE, "导出");
        
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    selectAllRecordings();
                    return true;
                case 2:
                    // TODO: 实现重命名功能
                    return true;
                case 3:
                    showRecordingDetails();
                    return true;
                case 4:
                    // TODO: 实现标签功能
                    return true;
                case 5:
                    // TODO: 实现导出功能
                    return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void enterSelectionMode() {
        LogUtils.logMethodEnter(TAG, "enterSelectionMode");
        isSelectionMode = true;
        
        normalAppBar.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction(() -> {
                normalAppBar.setVisibility(View.GONE);
                selectionAppBar.setVisibility(View.VISIBLE);
                selectionAppBar.setAlpha(0f);
                selectionAppBar.animate()
                    .alpha(1f)
                    .setDuration(200);
            });
        
        selectionBottomBar.setVisibility(View.VISIBLE);
        selectionBottomBar.setTranslationY(selectionBottomBar.getHeight());
        selectionBottomBar.animate()
            .translationY(0f)
            .setDuration(200);
        
        updateSelectionTitle();
        adapter.setSelectionMode(true);
        adapter.notifyDataSetChanged();
    }

    private void exitSelectionMode() {
        LogUtils.logMethodEnter(TAG, "exitSelectionMode");
        isSelectionMode = false;
        
        selectionAppBar.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction(() -> {
                selectionAppBar.setVisibility(View.GONE);
                normalAppBar.setVisibility(View.VISIBLE);
                normalAppBar.setAlpha(0f);
                normalAppBar.animate()
                    .alpha(1f)
                    .setDuration(200);
            });
        
        selectionBottomBar.animate()
            .translationY(selectionBottomBar.getHeight())
            .setDuration(200)
            .withEndAction(() -> selectionBottomBar.setVisibility(View.GONE));
        
        adapter.setSelectionMode(false);
        adapter.clearSelection();
        adapter.notifyDataSetChanged();
    }

    private void updateSelectionTitle() {
        int count = adapter.getSelectedItems().size();
        selectionCount.setText(String.format("已选择 %d 项", count));
        
        Menu menu = selectionBottomBar.getMenu();
        menu.findItem(R.id.action_share).setEnabled(count > 0);
        menu.findItem(R.id.action_delete).setEnabled(count > 0);
        menu.findItem(R.id.action_more).setEnabled(count > 0);
        
        if (count > 0) {
            selectionBottomBar.setElevation(8f);
        } else {
            selectionBottomBar.setElevation(0f);
        }
    }

    private void setupRecyclerView() {
        LogUtils.logMethodEnter(TAG, "setupRecyclerView");
        
        adapter = new RecordingsAdapter(this);
        recordingsList.setAdapter(adapter);
        recordingsList.setLayoutManager(new LinearLayoutManager(requireContext()));
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
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("提示");

                    // 根据错误类型添加不同的操作建议
                    if (error.contains("文件不存在")) {
                        builder.setMessage(error + "\n\n建议：点击刷新按钮更新录音列表")
                               .setPositiveButton("刷新", (dialog, which) -> {
                                   viewModel.loadRecordings(true);
                               })
                               .setNegativeButton("取消", null);
                    } else if (error.contains("存储权限")) {
                        builder.setMessage(error + "\n\n建议：请检查是否授予应用存储权限")
                               .setPositiveButton("去设置", (dialog, which) -> {
                                   // 跳转到应用设置页面
                                   startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                       android.net.Uri.fromParts("package", requireContext().getPackageName(), null)));
                               })
                               .setNegativeButton("取消", null);
                    } else {
                        builder.setMessage(error)
                               .setPositiveButton("确定", null);
                    }

                    builder.show();
                }
            });

            viewModel.getPlaybackState().observe(getViewLifecycleOwner(), state -> {
                // 播放状态更新时，通过重新提交列表来刷新UI
                if (state != null) {
                    adapter.notifyDataSetChanged();
                }
            });

            viewModel.getWaveformData().observe(getViewLifecycleOwner(), waveformData -> {
                // 波形数据更新时，通过重新提交列表来刷新UI
                if (waveformData != null) {
                    adapter.notifyDataSetChanged();
                }
            });
        } catch (Exception e) {
            LogUtils.logError(TAG, "数据观察设置失败", e);
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle("错误")
                .setMessage("应用发生错误，请重试")
                .setPositiveButton("确定", null)
                .show();
        }
    }

    @Override
    public void onRecordingClick(RecordingEntity recording) {
        LogUtils.logMethodEnter(TAG, "onRecordingClick");
        if (!isSelectionMode) {
            // 播放录音
            viewModel.playRecording(recording.getFilePath());
        }
        LogUtils.logMethodExit(TAG, "onRecordingClick");
    }

    @Override
    public void onPlayPauseClick(RecordingEntity recording) {
        LogUtils.logMethodEnter(TAG, "onPlayPauseClick");
        viewModel.playRecording(recording.getFilePath());
        LogUtils.logMethodExit(TAG, "onPlayPauseClick");
    }

    @Override
    public void onSelectionChange(Set<String> selectedItems) {
        LogUtils.logMethodEnter(TAG, "onSelectionChange");
        if (selectedItems.isEmpty()) {
            exitSelectionMode();
        } else if (!isSelectionMode) {
            enterSelectionMode();
        }
        updateSelectionTitle(selectedItems.size());
        LogUtils.logMethodExit(TAG, "onSelectionChange");
    }

    private void updateSelectionTitle(int count) {
        if (selectionCount != null) {
            selectionCount.setText(String.format("已选择 %d 项", count));
        }
    }

    public void onSeekTo(RecordingEntity recording, int progress) {
        LogUtils.logMethodEnter(TAG, "onSeekTo");
        viewModel.seekTo(progress);
    }

    public void onSpeedChange(RecordingEntity recording, float speed) {
        LogUtils.logMethodEnter(TAG, "onSpeedChange");
        viewModel.setPlaybackSpeed(speed);
    }

    @Override
    public void onPause() {
        super.onPause();
        LogUtils.logMethodEnter(TAG, "onPause");
        // 当Fragment暂停时停止播放
        viewModel.stopPlayback();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.logMethodEnter(TAG, "onDestroyView");
        viewModel.stopPlayback();
    }

    private void shareSelectedRecordings() {
        LogUtils.logMethodEnter(TAG, "shareSelectedRecordings");
        
        Set<String> selectedItems = adapter.getSelectedItems();
        if (!selectedItems.isEmpty()) {
            ArrayList<Uri> uris = new ArrayList<>();
            for (String filePath : selectedItems) {
                File file = new File(filePath);
                if (file.exists()) {
                    uris.add(Uri.fromFile(file));
                }
            }
            
            if (!uris.isEmpty()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.setType("audio/*");
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                startActivity(Intent.createChooser(shareIntent, "分享录音"));
            }
        }
    }

    private void deleteSelectedRecordings() {
        Set<String> selectedItems = adapter.getSelectedItems();
        if (!selectedItems.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除录音")
                .setMessage(String.format("确定要删除选中的%d个录音文件吗？", selectedItems.size()))
                .setPositiveButton("删除", (dialog, which) -> {
                    // 删除录音
                    for (String filePath : selectedItems) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    viewModel.loadRecordings(true);
                    exitSelectionMode();
                })
                .setNegativeButton("取消", null)
                .show();
        }
    }

    private void selectAllRecordings() {
        List<RecordingEntity> allRecordings = adapter.getCurrentList();
        for (RecordingEntity recording : allRecordings) {
            adapter.toggleSelection(recording.getFilePath());
        }
    }

    private void showRecordingDetails() {
        Set<String> selectedItems = adapter.getSelectedItems();
        if (selectedItems.size() == 1) {
            String filePath = selectedItems.iterator().next();
            RecordingEntity recording = null;
            for (RecordingEntity item : adapter.getCurrentList()) {
                if (item.getFilePath().equals(filePath)) {
                    recording = item;
                    break;
                }
            }
            
            if (recording != null) {
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("录音详情")
                    .setMessage(String.format(
                        "文件名：%s\n" +
                        "创建时间：%s\n" +
                        "时长：%s\n" +
                        "大小：%s\n" +
                        "路径：%s",
                        recording.getFileName(),
                        FormatUtils.formatDate(recording.getCreationTime()),
                        FormatUtils.formatDuration(recording.getDuration()),
                        FormatUtils.formatFileSize(requireContext(), recording.getFileSize()),
                        recording.getFilePath()
                    ))
                    .setPositiveButton("确定", null)
                    .show();
            }
        }
    }
} 