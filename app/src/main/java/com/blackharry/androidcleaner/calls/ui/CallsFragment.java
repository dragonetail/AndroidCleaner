package com.blackharry.androidcleaner.calls.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.calls.data.CallEntity;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import com.google.android.material.snackbar.Snackbar;

public class CallsFragment extends Fragment implements CallsAdapter.OnItemClickListener {
    private static final String TAG = "CallsFragment";
    private CallsViewModel viewModel;
    private CallsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private SearchView searchView;
    private boolean isSelectionMode = false;
    private MenuItem selectAllMenuItem;
    private MenuItem clearSelectionMenuItem;
    private FloatingActionButton fab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.logMethodEnter(TAG, "onCreate");
        viewModel = new ViewModelProvider(this).get(CallsViewModel.class);
        LogUtils.logMethodExit(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        LogUtils.logMethodEnter(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_calls, container, false);
        
        // 初始化视图
        initializeViews(view);
        
        // 设置观察者
        setupObservers();
        
        // 添加菜单提供者
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_calls, menu);
                selectAllMenuItem = menu.findItem(R.id.action_select_all);
                clearSelectionMenuItem = menu.findItem(R.id.action_clear_selection);
                updateMenuItems();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.action_select_all) {
                    adapter.selectAll();
                    return true;
                } else if (id == R.id.action_clear_selection) {
                    adapter.clearSelection();
                    isSelectionMode = false;
                    updateMenuItems();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        
        LogUtils.logMethodExit(TAG, "onCreateView");
        return view;
    }

    private void initializeViews(View view) {
        // 初始化SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            LogUtils.i(TAG, "用户下拉刷新");
            viewModel.syncCallLogs();
        });

        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CallsAdapter(this);
        recyclerView.setAdapter(adapter);

        // 初始化其他视图
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.text_empty);

        // 初始化FAB
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            LogUtils.i(TAG, "用户点击FAB");
            showFilterDialog();
        });
    }

    private void setupObservers() {
        // 观察通话记录数据
        viewModel.getCalls().observe(getViewLifecycleOwner(), calls -> {
            LogUtils.i(TAG, String.format("收到%d条通话记录", calls.size()));
            adapter.submitList(calls);
            updateEmptyView(calls);
        });

        // 观察加载状态
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        // 观察错误信息
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void updateEmptyView(List<CallEntity> calls) {
        if (calls.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getAllCalls().observe(getViewLifecycleOwner(), calls -> {
            adapter.submitList(calls);
        });
    }

    private void showFilterDialog() {
        String[] options = {
            "全部通话",
            "今日通话",
            "本周通话",
            "本月通话",
            "有录音的通话"
        };

        new AlertDialog.Builder(requireContext())
            .setTitle("筛选通话记录")
            .setItems(options, (dialog, which) -> {
                LogUtils.i(TAG, "用户选择筛选选项: " + options[which]);
                switch (which) {
                    case 0:
                        viewModel.loadCalls();
                        break;
                    case 1:
                        viewModel.loadCallsAfter(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
                        break;
                    case 2:
                        viewModel.loadCallsAfter(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000);
                        break;
                    case 3:
                        viewModel.loadCallsAfter(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);
                        break;
                    case 4:
                        viewModel.getCallsWithRecordings();
                        break;
                }
            })
            .show();
    }

    @Override
    public void onItemClick(CallEntity call) {
        if (isSelectionMode) {
            adapter.toggleSelection(call.getId());
            if (adapter.getSelectedItems().isEmpty()) {
                isSelectionMode = false;
                updateMenuItems();
            }
        } else {
            // TODO: 显示通话详情
            Toast.makeText(getContext(), "通话时间：" + call.getDuration() + "秒", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemLongClick(CallEntity call) {
        if (!isSelectionMode) {
            isSelectionMode = true;
            updateMenuItems();
        }
        adapter.toggleSelection(call.getId());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.logMethodEnter(TAG, "onDestroyView");
    }

    private void updateMenuItems() {
        if (selectAllMenuItem != null) {
            selectAllMenuItem.setVisible(isSelectionMode);
        }
        if (clearSelectionMenuItem != null) {
            clearSelectionMenuItem.setVisible(isSelectionMode);
        }
    }
} 