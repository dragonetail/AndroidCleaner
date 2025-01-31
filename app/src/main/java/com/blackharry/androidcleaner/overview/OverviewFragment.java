package com.blackharry.androidcleaner.overview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.common.utils.FormatUtils;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import java.util.List;

public class OverviewFragment extends Fragment {
    private static final String TAG = "OverviewFragment";
    private OverviewViewModel viewModel;
    private PieChart storageChart;
    private TextView totalStorage;
    private TextView recordingCount;
    private TextView recordingSize;
    private TextView callCount;
    private TextView callDuration;
    private TextView contactCount;
    private TextView cleanupSuggestion;
    private TextView cleanupDetails;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.logMethodEnter(TAG, "onCreate");
        viewModel = new ViewModelProvider(this).get(OverviewViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        LogUtils.logMethodEnter(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogUtils.logMethodEnter(TAG, "onViewCreated");
        
        try {
            initViews(view);
            setupStorageChart();
            observeViewModel();
        } catch (Exception e) {
            LogUtils.logError(TAG, "视图初始化失败", e);
        }
    }

    private void initViews(View view) {
        LogUtils.logMethodEnter(TAG, "initViews");
        
        storageChart = view.findViewById(R.id.chart_storage);
        totalStorage = view.findViewById(R.id.text_total_storage);
        recordingCount = view.findViewById(R.id.text_recording_count);
        recordingSize = view.findViewById(R.id.text_recording_size);
        callCount = view.findViewById(R.id.text_call_count);
        callDuration = view.findViewById(R.id.text_call_duration);
        contactCount = view.findViewById(R.id.text_contact_count);
        cleanupSuggestion = view.findViewById(R.id.text_cleanup_suggestion);
        cleanupDetails = null;
    }

    private void setupStorageChart() {
        LogUtils.logMethodEnter(TAG, "setupStorageChart");
        
        storageChart.setDrawHoleEnabled(true);
        storageChart.setHoleColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
        storageChart.setTransparentCircleColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
        storageChart.setTransparentCircleAlpha(110);
        storageChart.setDescription(null);
        storageChart.setDrawEntryLabels(false);
        storageChart.getLegend().setEnabled(false);
    }

    private void observeViewModel() {
        LogUtils.logMethodEnter(TAG, "observeViewModel");
        
        try {
            viewModel.getTotalStorageSize().observe(getViewLifecycleOwner(), size -> {
                totalStorage.setText(FormatUtils.formatFileSize(requireContext(), size));
                updateStorageChart(size);
            });

            viewModel.getRecordingCount().observe(getViewLifecycleOwner(), count -> {
                recordingCount.setText(String.format("录音文件数量：%d", count));
            });

            viewModel.getRecordingSize().observe(getViewLifecycleOwner(), size -> {
                recordingSize.setText(String.format("录音文件大小：%s", FormatUtils.formatFileSize(requireContext(), size)));
            });

            viewModel.getCallCount().observe(getViewLifecycleOwner(), count -> {
                callCount.setText(String.format("通话记录数量：%d", count));
            });

            viewModel.getTotalCallDuration().observe(getViewLifecycleOwner(), duration -> {
                callDuration.setText(String.format("总通话时长：%s", FormatUtils.formatDuration(duration)));
            });

            viewModel.getContactCount().observe(getViewLifecycleOwner(), count -> {
                contactCount.setText(String.format("联系人数量：%d", count));
            });

            viewModel.getCleanupSuggestion().observe(getViewLifecycleOwner(), suggestion -> {
                if (suggestion.isEmpty()) {
                    cleanupSuggestion.setText("暂无清理建议");
                } else {
                    cleanupSuggestion.setText(suggestion);
                }
            });
        } catch (Exception e) {
            LogUtils.logError(TAG, "数据观察设置失败", e);
        }
    }

    private void updateStorageChart(long totalSize) {
        LogUtils.logMethodEnter(TAG, "updateStorageChart");
        
        try {
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(totalSize));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(ContextCompat.getColor(requireContext(), R.color.primary));
            dataSet.setValueTextSize(14f);
            dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return FormatUtils.formatFileSize(requireContext(), (long) value);
                }
            });

            storageChart.setData(data);
            storageChart.invalidate();
        } catch (Exception e) {
            LogUtils.logError(TAG, "图表更新失败", e);
        }
    }
} 