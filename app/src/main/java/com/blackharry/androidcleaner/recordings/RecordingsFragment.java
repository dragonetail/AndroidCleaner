package com.blackharry.androidcleaner.recordings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.utils.LogUtils;
import com.google.android.material.snackbar.Snackbar;

public class RecordingsFragment extends Fragment {
    private static final String TAG = "RecordingsFragment";
    private RecordingsViewModel viewModel;
    private RecordingAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            LogUtils.logMethodEnter(TAG, "onCreate");
            super.onCreate(savedInstanceState);
            viewModel = new ViewModelProvider(this).get(RecordingsViewModel.class);
            LogUtils.logMethodExit(TAG, "onCreate");
        } catch (Exception e) {
            LogUtils.logError(TAG, "Fragment创建失败", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        try {
            LogUtils.logMethodEnter(TAG, "onCreateView");
            long startTime = System.currentTimeMillis();
            
            View view = inflater.inflate(R.layout.fragment_recordings, container, false);
            
            LogUtils.d(TAG, "开始初始化录音列表界面");
            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
            if (recyclerView == null) {
                LogUtils.e(TAG, "界面组件未找到", new IllegalStateException("RecyclerView为空"));
                return view;
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new RecordingAdapter(requireContext(), recording -> {
                LogUtils.i(TAG, String.format("用户点击录音: %s", recording.getFileName()));
                // TODO: 实现录音播放功能
            });
            recyclerView.setAdapter(adapter);

            LogUtils.logPerformance(TAG, "录音列表界面初始化", startTime);
            LogUtils.logMethodExit(TAG, "onCreateView");
            return view;
        } catch (Exception e) {
            LogUtils.logError(TAG, "创建视图失败", e);
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            LogUtils.logMethodEnter(TAG, "onViewCreated");
            super.onViewCreated(view, savedInstanceState);

            viewModel.getRecordings().observe(getViewLifecycleOwner(), recordings -> {
                LogUtils.d(TAG, String.format("录音列表更新，数量: %d", recordings.size()));
                adapter.submitList(recordings);
            });

            viewModel.getError().observe(getViewLifecycleOwner(), error -> {
                if (error != null) {
                    LogUtils.e(TAG, "加载录音列表失败", new Exception(error));
                    showError(error);
                }
            });

            loadRecordings();
            LogUtils.logMethodExit(TAG, "onViewCreated");
        } catch (Exception e) {
            LogUtils.logError(TAG, "视图创建后初始化失败", e);
        }
    }

    private void loadRecordings() {
        try {
            LogUtils.logMethodEnter(TAG, "loadRecordings");
            long startTime = System.currentTimeMillis();
            
            viewModel.loadRecordings(() -> {
                LogUtils.logPerformance(TAG, "加载录音列表", startTime);
            });
            
            LogUtils.logMethodExit(TAG, "loadRecordings");
        } catch (Exception e) {
            LogUtils.logError(TAG, "加载录音列表失败", e);
        }
    }

    private void showError(String message) {
        try {
            LogUtils.logMethodEnter(TAG, "showError");
            if (getView() != null) {
                Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
            }
            LogUtils.logMethodExit(TAG, "showError");
        } catch (Exception e) {
            LogUtils.logError(TAG, "显示错误信息失败", e);
        }
    }
}