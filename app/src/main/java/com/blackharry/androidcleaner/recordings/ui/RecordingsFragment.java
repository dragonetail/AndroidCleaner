package com.blackharry.androidcleaner.recordings.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.blackharry.androidcleaner.recordings.data.RecordingRepository;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.blackharry.androidcleaner.common.utils.FormatUtils;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.google.android.material.datepicker.MaterialDatePicker;
import androidx.core.util.Pair;
import com.google.android.material.slider.RangeSlider;

public class RecordingsFragment extends Fragment implements RecordingsAdapter.RecordingClickListener {
    private static final String TAG = "RecordingsFragment";
    private RecordingsViewModel viewModel;
    private RecordingsAdapter adapter;
    private View emptyState;
    private View normalAppBar;
    private View selectionAppBar;
    private TextView selectionCount;
    private RecyclerView recordingsList;
    private androidx.appcompat.widget.Toolbar toolbar;
    private boolean isSelectionMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.logMethodEnter(TAG, "onCreate");
        viewModel = new ViewModelProvider(this).get(RecordingsViewModel.class);
        
        // 检查是否需要加载测试数据
        if (savedInstanceState == null) {
            // 只在Fragment首次创建时加载测试数据，避免配置变更时重复加载
            if (viewModel.getRepository().getAllRecordings().isEmpty()) {
                // 如果没有数据，加载测试数据
                viewModel.loadTestData();
            } else {
                // 如果有数据，直接加载现有数据
                viewModel.loadRecordings(true);
            }
        }
        
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
                        setEnabled(false);
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
        recordingsList = view.findViewById(R.id.recordings_list);
        toolbar = view.findViewById(R.id.toolbar);

        view.findViewById(R.id.close_selection_button).setOnClickListener(v -> exitSelectionMode());
        view.findViewById(R.id.action_delete).setOnClickListener(v -> deleteSelectedItems());
        view.findViewById(R.id.action_select_all).setOnClickListener(v -> selectAllItems());
        view.findViewById(R.id.action_more).setOnClickListener(v -> showMoreOptions(v));
    }

    private void setupToolbar() {
        LogUtils.logMethodEnter(TAG, "setupToolbar");
        
        // 设置菜单资源
        toolbar.inflateMenu(R.menu.menu_recordings);
        
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            
            // 时间过滤
            if (itemId == R.id.time_all) {
                viewModel.setTimeFilter(RecordingsViewModel.TimeFilter.ALL);
                return true;
            } else if (itemId == R.id.time_year_ago) {
                viewModel.setTimeFilter(RecordingsViewModel.TimeFilter.YEAR_AGO);
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
            else if (itemId == R.id.duration_all) {
                viewModel.setDurationFilter(RecordingsViewModel.DurationFilter.ALL);
                return true;
            } else if (itemId == R.id.duration_1min) {
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
            else if (itemId == R.id.menu_sort_date_desc) {
                viewModel.setSortOrder(RecordingsViewModel.SortOrder.TIME_DESC);
                return true;
            } else if (itemId == R.id.menu_sort_date_asc) {
                viewModel.setSortOrder(RecordingsViewModel.SortOrder.TIME_ASC);
                return true;
            } else if (itemId == R.id.menu_sort_size_desc) {
                viewModel.setSortOrder(RecordingsViewModel.SortOrder.SIZE_DESC);
                return true;
            } else if (itemId == R.id.menu_sort_size_asc) {
                viewModel.setSortOrder(RecordingsViewModel.SortOrder.SIZE_ASC);
                return true;
            }
            
            return false;
        });

        // 设置菜单准备监听器，用于更新选中状态
        toolbar.setOnMenuItemClickListener(item -> {
            Menu menu = toolbar.getMenu();
            
            // 更新时间过滤选中状态
            RecordingsViewModel.TimeFilter timeFilter = viewModel.getCurrentTimeFilter();
            updateTimeFilterChecked(menu, timeFilter);
            
            // 更新时长过滤选中状态
            RecordingsViewModel.DurationFilter durationFilter = viewModel.getCurrentDurationFilter();
            updateDurationFilterChecked(menu, durationFilter);
            
            // 更新排序方式选中状态
            RecordingsViewModel.SortOrder sortOrder = viewModel.getCurrentSortOrder();
            updateSortOrderChecked(menu, sortOrder);
            
            return false;
        });
    }

    private void updateTimeFilterChecked(Menu menu, RecordingsViewModel.TimeFilter filter) {
        menu.findItem(R.id.time_all).setChecked(filter == RecordingsViewModel.TimeFilter.ALL);
        menu.findItem(R.id.time_year_ago).setChecked(filter == RecordingsViewModel.TimeFilter.YEAR_AGO);
        menu.findItem(R.id.time_today).setChecked(filter == RecordingsViewModel.TimeFilter.TODAY);
        menu.findItem(R.id.time_week).setChecked(filter == RecordingsViewModel.TimeFilter.WEEK);
        menu.findItem(R.id.time_month).setChecked(filter == RecordingsViewModel.TimeFilter.MONTH);
        menu.findItem(R.id.time_quarter).setChecked(filter == RecordingsViewModel.TimeFilter.QUARTER);
    }

    private void updateDurationFilterChecked(Menu menu, RecordingsViewModel.DurationFilter filter) {
        menu.findItem(R.id.duration_all).setChecked(filter == RecordingsViewModel.DurationFilter.ALL);
        menu.findItem(R.id.duration_1min).setChecked(filter == RecordingsViewModel.DurationFilter.MIN_1);
        menu.findItem(R.id.duration_5min).setChecked(filter == RecordingsViewModel.DurationFilter.MIN_5);
        menu.findItem(R.id.duration_30min).setChecked(filter == RecordingsViewModel.DurationFilter.MIN_30);
        menu.findItem(R.id.duration_2hour).setChecked(filter == RecordingsViewModel.DurationFilter.HOUR_2);
        menu.findItem(R.id.duration_longer).setChecked(filter == RecordingsViewModel.DurationFilter.LONGER);
    }

    private void updateSortOrderChecked(Menu menu, RecordingsViewModel.SortOrder order) {
        menu.findItem(R.id.menu_sort_date_desc).setChecked(order == RecordingsViewModel.SortOrder.TIME_DESC);
        menu.findItem(R.id.menu_sort_date_asc).setChecked(order == RecordingsViewModel.SortOrder.TIME_ASC);
        menu.findItem(R.id.menu_sort_size_desc).setChecked(order == RecordingsViewModel.SortOrder.SIZE_DESC);
        menu.findItem(R.id.menu_sort_size_asc).setChecked(order == RecordingsViewModel.SortOrder.SIZE_ASC);
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

    private void deleteSelectedItems() {
        LogUtils.logMethodEnter(TAG, "deleteSelectedItems");
        
        Set<String> selectedItems = adapter.getSelectedItems();
        if (!selectedItems.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除录音")
                .setMessage(String.format("确定要删除选中的%d个录音文件吗？", selectedItems.size()))
                .setPositiveButton("删除", (dialog, which) -> {
                    LogUtils.logMethodEnter(TAG, "deleteSelectedItems.onPositiveButton");
                    // 删除录音文件和数据库记录
                    int[] totalCount = {selectedItems.size()};
                    int[] failCount = {0};
                    int[] completedCount = {0};
                    
                    // 创建检查完成的 Runnable
                    final Runnable checkCompletion = () -> {
                        if (completedCount[0] == totalCount[0]) {
                            requireActivity().runOnUiThread(() -> {
                                // 显示删除结果
                                String message;
                                if (failCount[0] == 0) {
                                    message = String.format("已成功删除%d个录音", totalCount[0]);
                                } else {
                                    message = String.format("已删除%d个录音，%d个删除失败", 
                                        totalCount[0] - failCount[0], failCount[0]);
                                }
                                Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
                                
                                // 刷新列表并退出选择模式
                                viewModel.loadRecordings(true);
                                exitSelectionMode();
                            });
                        }
                    };
                    
                    for (String filePath : selectedItems) {
                        try {
                            File file = new File(filePath);
                            // 如果文件存在且删除失败，则不删除数据库记录
                            if (file.exists() && !file.delete()) {
                                failCount[0]++;
                                completedCount[0]++;
                                LogUtils.e(TAG, "文件删除失败: " + filePath);
                                checkCompletion.run();
                                continue;
                            }
                            // 无论文件是否存在，都删除数据库记录
                            viewModel.getRepository().deleteRecordingByPath(filePath, new RecordingRepository.Callback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    LogUtils.i(TAG, "成功删除记录: " + filePath);
                                    completedCount[0]++;
                                    checkCompletion.run();
                                }

                                @Override
                                public void onError(Exception e) {
                                    failCount[0]++;
                                    completedCount[0]++;
                                    LogUtils.e(TAG, "删除操作失败: " + filePath, e);
                                    checkCompletion.run();
                                }
                            });
                        } catch (Exception e) {
                            failCount[0]++;
                            completedCount[0]++;
                            LogUtils.e(TAG, "删除操作失败: " + filePath, e);
                            checkCompletion.run();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        }
        LogUtils.logMethodExit(TAG, "deleteSelectedItems");
    }

    private void selectAllItems() {
        LogUtils.logMethodEnter(TAG, "selectAllItems");
        
        List<RecordingEntity> allRecordings = adapter.getCurrentList();
        boolean hasUnselectedItems = false;
        
        // 检查是否有未选中的项
        for (RecordingEntity recording : allRecordings) {
            if (!adapter.isSelected(recording.getFilePath())) {
                hasUnselectedItems = true;
                break;
            }
        }
        
        // 如果有未选中的项，则全选；否则取消全选
        for (RecordingEntity recording : allRecordings) {
            if (hasUnselectedItems) {
                adapter.addSelection(recording.getFilePath());
            } else {
                adapter.removeSelection(recording.getFilePath());
            }
        }
        
        // 更新UI
        updateSelectionTitle();
        adapter.notifyDataSetChanged();
        
        LogUtils.logMethodExit(TAG, "selectAllItems");
    }

    private void showMoreOptions(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_recordings_selection_more, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_share) {
                // 实现分享逻辑
                return true;
            } else if (itemId == R.id.action_convert_text) {
                // 实现转文字逻辑
                return true;
            } else if (itemId == R.id.action_rename) {
                // 实现重命名逻辑
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

        adapter.setSelectionMode(false);
        adapter.clearSelection();
        adapter.notifyDataSetChanged();
    }

    private void updateSelectionTitle() {
        int count = adapter.getSelectedItems().size();
        selectionCount.setText(String.format("已选择 %d 项", count));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.menu_sort_date_asc) {
            viewModel.setSortOption(RecordingsViewModel.SortOption.DATE_ASC);
            return true;
        } else if (itemId == R.id.menu_sort_date_desc) {
            viewModel.setSortOption(RecordingsViewModel.SortOption.DATE_DESC);
            return true;
        } else if (itemId == R.id.menu_sort_duration_asc) {
            viewModel.setSortOption(RecordingsViewModel.SortOption.DURATION_ASC);
            return true;
        } else if (itemId == R.id.menu_sort_duration_desc) {
            viewModel.setSortOption(RecordingsViewModel.SortOption.DURATION_DESC);
            return true;
        } else if (itemId == R.id.menu_sort_size_asc) {
            viewModel.setSortOption(RecordingsViewModel.SortOption.SIZE_ASC);
            return true;
        } else if (itemId == R.id.menu_sort_size_desc) {
            viewModel.setSortOption(RecordingsViewModel.SortOption.SIZE_DESC);
            return true;
        } else if (itemId == R.id.menu_filter_date) {
            showDateFilterDialog();
            return true;
        } else if (itemId == R.id.menu_filter_duration) {
            showDurationFilterDialog();
            return true;
        } else if (itemId == R.id.menu_clear_filters) {
            viewModel.clearFilters();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showDateFilterDialog() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("选择日期范围");
        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        
        picker.addOnPositiveButtonClickListener(selection -> {
            viewModel.setDateFilter(selection.first, selection.second);
        });
        
        picker.show(getChildFragmentManager(), picker.toString());
    }

    private void showDurationFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_duration_filter, null);
        RangeSlider durationSlider = dialogView.findViewById(R.id.duration_slider);
        TextView minDurationText = dialogView.findViewById(R.id.min_duration_text);
        TextView maxDurationText = dialogView.findViewById(R.id.max_duration_text);

        durationSlider.setValues(0f, 3600f); // 默认0-60分钟
        durationSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            minDurationText.setText(formatDuration(values.get(0).longValue()));
            maxDurationText.setText(formatDuration(values.get(1).longValue()));
        });

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("选择时长范围")
            .setView(dialogView)
            .setPositiveButton("确定", (dialog, which) -> {
                List<Float> values = durationSlider.getValues();
                viewModel.setDurationFilter(
                    values.get(0).longValue() * 1000, // 转换为毫秒
                    values.get(1).longValue() * 1000
                );
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        } else {
            return (seconds / 60) + "分钟";
        }
    }
} 