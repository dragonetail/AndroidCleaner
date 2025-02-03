package com.blackharry.androidcleaner.contacts.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.List;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;

public class ContactsFragment extends Fragment implements ContactsAdapter.OnItemClickListener {
    private static final String TAG = "ContactsFragment";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private ContactsViewModel viewModel;
    private ContactsAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private SearchView searchView;
    private boolean isSelectionMode = false;
    private MenuItem selectAllMenuItem;
    private MenuItem clearSelectionMenuItem;
    private ActivityResultLauncher<String> permissionLauncher;
    private androidx.appcompat.widget.Toolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.logMethodEnter(TAG, "onCreate");
        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        
        // 检查权限
        checkAndRequestPermissions();
        
        LogUtils.logMethodExit(TAG, "onCreate");
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // 使用新的权限请求API
            registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 用户授予了权限，开始同步
                        viewModel.syncContacts();
                    } else {
                        // 用户拒绝了权限
                        Toast.makeText(requireContext(), "需要联系人权限才能同步数据", Toast.LENGTH_LONG).show();
                    }
                }).launch(Manifest.permission.READ_CONTACTS);
        } else {
            // 已有权限，开始同步
            viewModel.syncContacts();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        LogUtils.logMethodEnter(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        
        // 初始化视图
        initializeViews(view);
        
        // 设置观察者
        setupObservers();

        // 设置菜单
        setupMenu();
        
        LogUtils.logMethodExit(TAG, "onCreateView");
        return view;
    }

    private void initializeViews(View view) {
        LogUtils.logMethodEnter(TAG, "initializeViews");
        
        // 初始化Toolbar
        toolbar = view.findViewById(R.id.toolbar);
        setupToolbar();

        // 初始化SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            LogUtils.i(TAG, "用户下拉刷新");
            viewModel.syncContacts();
        });

        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ContactsAdapter(this);
        recyclerView.setAdapter(adapter);

        // 初始化其他视图
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.text_empty);

        // 初始化FAB
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            LogUtils.i(TAG, "用户点击FAB");
            showFilterDialog();
        });
    }

    private void setupToolbar() {
        LogUtils.logMethodEnter(TAG, "setupToolbar");
        if (toolbar != null) {
            toolbar.setTitle(R.string.title_bar_contacts);
            toolbar.inflateMenu(R.menu.menu_contacts);
            toolbar.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
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
            });
        }
    }

    private void setupObservers() {
        // 观察联系人数据
        viewModel.getContacts().observe(getViewLifecycleOwner(), contacts -> {
            LogUtils.i(TAG, String.format("收到%d个联系人", contacts.size()));
            adapter.submitList(contacts);
            updateEmptyView(contacts);
        });

        // 观察加载状态
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        // 观察错误信息
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_contacts, menu);

                // 设置搜索功能
                MenuItem searchItem = menu.findItem(R.id.action_search);
                searchView = (SearchView) searchItem.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        LogUtils.i(TAG, "搜索联系人: " + query);
                        viewModel.searchContacts(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (newText.isEmpty()) {
                            LogUtils.i(TAG, "搜索框清空，加载所有联系人");
                            viewModel.loadContacts();
                        }
                        return true;
                    }
                });

                // 保存菜单项引用
                selectAllMenuItem = menu.findItem(R.id.action_select_all);
                clearSelectionMenuItem = menu.findItem(R.id.action_clear_selection);
                
                updateMenuItems();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_filter_all) {
                    viewModel.loadContacts();
                    return true;
                } else if (itemId == R.id.action_filter_safe_zone) {
                    viewModel.loadContactsInSafeZone();
                    return true;
                } else if (itemId == R.id.action_filter_temp_zone) {
                    viewModel.loadContactsInTemporaryZone();
                    return true;
                } else if (itemId == R.id.action_filter_blacklist) {
                    viewModel.loadBlacklistedContacts();
                    return true;
                } else if (itemId == R.id.action_filter_deleted) {
                    viewModel.loadDeletedContacts();
                    return true;
                } else if (itemId == R.id.action_sync) {
                    viewModel.syncContacts();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void updateEmptyView(List<ContactEntity> contacts) {
        if (contacts.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void updateMenuItems() {
        if (selectAllMenuItem != null) {
            selectAllMenuItem.setVisible(isSelectionMode);
        }
        if (clearSelectionMenuItem != null) {
            clearSelectionMenuItem.setVisible(isSelectionMode);
        }
    }

    private void showFilterDialog() {
        String[] options = {
            "全部联系人",
            "有电话号码",
            "安全区联系人",
            "临时区联系人",
            "黑名单联系人",
            "已删除联系人",
            "最近联系"
        };

        new AlertDialog.Builder(requireContext())
            .setTitle("筛选联系人")
            .setItems(options, (dialog, which) -> {
                LogUtils.i(TAG, "用户选择筛选选项: " + options[which]);
                switch (which) {
                    case 0:
                        viewModel.loadContacts();
                        break;
                    case 1:
                        viewModel.loadContactsWithPhoneNumber();
                        break;
                    case 2:
                        viewModel.loadContactsInSafeZone();
                        break;
                    case 3:
                        viewModel.loadContactsInTemporaryZone();
                        break;
                    case 4:
                        viewModel.loadBlacklistedContacts();
                        break;
                    case 5:
                        viewModel.loadDeletedContacts();
                        break;
                    case 6:
                        // TODO: 实现按最近联系时间排序
                        break;
                }
            })
            .show();
    }

    private void showZoneDialog(ContactEntity contact) {
        String[] options = {
            "设为安全区联系人",
            "设为临时区联系人",
            "加入黑名单",
            "删除联系人"
        };

        boolean[] checkedItems = {
            contact.isSafeZone(),
            contact.isTemporaryZone(),
            contact.isBlacklisted(),
            false
        };

        new AlertDialog.Builder(requireContext())
            .setTitle("联系人分类")
            .setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
                // 互斥选择
                if (isChecked) {
                    for (int i = 0; i < checkedItems.length - 1; i++) {
                        if (i != which) {
                            ((AlertDialog) dialog).getListView().setItemChecked(i, false);
                            checkedItems[i] = false;
                        }
                    }
                }
            })
            .setPositiveButton("确定", (dialog, which) -> {
                LogUtils.i(TAG, "用户确认联系人分类设置");
                viewModel.updateContactZone(contact.getId(), 
                    checkedItems[0], checkedItems[1], checkedItems[2]);
                if (checkedItems[3]) {
                    viewModel.deleteContact(contact);
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    @Override
    public void onItemClick(ContactEntity contact) {
        LogUtils.i(TAG, "用户点击联系人: " + contact.getName());
        if (isSelectionMode) {
            adapter.toggleSelection(String.valueOf(contact.getId()));
            updateMenuItems();
        } else {
            showZoneDialog(contact);
        }
    }

    @Override
    public void onItemLongClick(ContactEntity contact) {
        LogUtils.i(TAG, "用户长按联系人: " + contact.getName());
        if (!isSelectionMode) {
            isSelectionMode = true;
            updateMenuItems();
        }
        adapter.toggleSelection(String.valueOf(contact.getId()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.logMethodEnter(TAG, "onDestroyView");
        if (searchView != null) {
            searchView.setOnQueryTextListener(null);
        }
        LogUtils.logMethodExit(TAG, "onDestroyView");
    }
}
