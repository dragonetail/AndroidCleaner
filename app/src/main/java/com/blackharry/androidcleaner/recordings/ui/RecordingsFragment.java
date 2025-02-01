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
    private com.google.android.material.bottomappbar.BottomAppBar selectionBottomBar;
    private RecyclerView recordingsList;
    private Spinner timeRangeSpinner;
    private Spinner sizeRangeSpinner;
    private Spinner sortSpinner;
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
        normalAppBar = view.findViewById(R.id.normal_app_bar);
        selectionAppBar = view.findViewById(R.id.selection_app_bar);
        selectionCount = view.findViewById(R.id.selection_count);
        selectionBottomBar = view.findViewById(R.id.selection_bottom_bar);
        recordingsList = view.findViewById(R.id.recordings_list);
        timeRangeSpinner = view.findViewById(R.id.time_range_spinner);
        sizeRangeSpinner = view.findViewById(R.id.size_range_spinner);
        sortSpinner = view.findViewById(R.id.sort_spinner);

        view.findViewById(R.id.close_selection_button).setOnClickListener(v -> exitSelectionMode());
        setupBottomBar();
    }

    private void setupToolbar() {
        toolbar.inflateMenu(R.menu.menu_recordings_selection);
        Menu menu = toolbar.getMenu();
        // 默认隐藏选择模式的菜单项
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }

        toolbar.setNavigationOnClickListener(v -> {
            if (isSelectionMode) {
                exitSelectionMode();
            }
        });

        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_share) {
                shareSelectedRecordings();
                return true;
            } else if (itemId == R.id.action_convert_text) {
                // TODO: 实现录音转文字功能
                Snackbar.make(requireView(), "录音转文字功能开发中", Snackbar.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_delete) {
                deleteSelectedRecordings();
                return true;
            } else if (itemId == R.id.action_select_all) {
                selectAllRecordings();
                return true;
            } else if (itemId == R.id.action_set_ringtone) {
                // TODO: 实现设置铃声功能
                Snackbar.make(requireView(), "设置铃声功能开发中", Snackbar.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_details) {
                showRecordingDetails();
                return true;
            } else if (itemId == R.id.action_rename) {
                // TODO: 实现重命名功能
                Snackbar.make(requireView(), "重命名功能开发中", Snackbar.LENGTH_SHORT).show();
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
            } else if (itemId == R.id.action_convert_text) {
                Snackbar.make(requireView(), "录音转文字功能开发中", Snackbar.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_delete) {
                deleteSelectedRecordings();
                return true;
            } else if (itemId == R.id.action_select_all) {
                selectAllRecordings();
                return true;
            } else if (itemId == R.id.action_more) {
                showMoreOptions();
                return true;
            }
            return false;
        });
    }

    private void showMoreOptions() {
        View moreButton = selectionBottomBar.findViewById(R.id.action_more);
        PopupMenu popup = new PopupMenu(requireContext(), moreButton);
        popup.getMenu().add(Menu.NONE, R.id.action_set_ringtone, Menu.NONE, "设置铃声");
        popup.getMenu().add(Menu.NONE, R.id.action_details, Menu.NONE, "详情");
        popup.getMenu().add(Menu.NONE, R.id.action_rename, Menu.NONE, "重命名");
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_set_ringtone) {
                Snackbar.make(requireView(), "设置铃声功能开发中", Snackbar.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_details) {
                showRecordingDetails();
                return true;
            } else if (itemId == R.id.action_rename) {
                Snackbar.make(requireView(), "重命名功能开发中", Snackbar.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void enterSelectionMode() {
        isSelectionMode = true;
        normalAppBar.setVisibility(View.GONE);
        selectionAppBar.setVisibility(View.VISIBLE);
        selectionBottomNav.setVisibility(View.VISIBLE);
        updateSelectionTitle();

        // 处理返回键
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), 
            new androidx.activity.OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    exitSelectionMode();
                }
            });
    }

    private void exitSelectionMode() {
        isSelectionMode = false;
        normalAppBar.setVisibility(View.VISIBLE);
        selectionAppBar.setVisibility(View.GONE);
        selectionBottomNav.setVisibility(View.GONE);
        adapter.clearSelection();
    }

    private void updateSelectionTitle() {
        int count = adapter.getSelectedItems().size();
        selectionCount.setText(String.format("已选择 %d 项", count));
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
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("提示");

                    // 根据错误类型添加不同的操作建议
                    if (error.contains("文件不存在")) {
                        builder.setMessage(error + "\n\n建议：点击刷新按钮更新录音列表")
                               .setPositiveButton("刷新", (dialog, which) -> {
                                   viewModel.loadRecordings();
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
        viewModel.playRecording(recording.getFilePath());
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
    public void onSelectionChange(Set<String> selectedItems) {
        LogUtils.logMethodEnter(TAG, "onSelectionChange");
        if (!isSelectionMode && !selectedItems.isEmpty()) {
            enterSelectionMode();
        } else if (isSelectionMode && selectedItems.isEmpty()) {
            exitSelectionMode();
        }
        updateSelectionTitle();
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
        Set<String> selectedItems = adapter.getSelectedItems();
        if (!selectedItems.isEmpty()) {
            ArrayList<Uri> uris = new ArrayList<>();
            for (String path : selectedItems) {
                uris.add(Uri.fromFile(new File(path)));
            }
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("audio/*");
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            startActivity(Intent.createChooser(shareIntent, "分享录音"));
        }
    }

    private void deleteSelectedRecordings() {
        Set<String> selectedItems = adapter.getSelectedItems();
        if (!selectedItems.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除录音")
                .setMessage(String.format("确定要删除选中的%d个录音文件吗？", selectedItems.size()))
                .setPositiveButton("删除", (dialog, which) -> {
                    viewModel.deleteRecordings(new ArrayList<>(selectedItems));
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