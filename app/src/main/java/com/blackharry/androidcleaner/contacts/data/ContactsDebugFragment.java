package com.blackharry.androidcleaner.contacts.data;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.utils.FormatUtils;
import com.blackharry.androidcleaner.utils.LogUtils;

public class ContactsDebugFragment extends Fragment {
    private static final String TAG = "ContactsDebugFragment";
    private ContactsDebugViewModel viewModel;
    private TextView debugTextView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            LogUtils.logMethodEnter(TAG, "onCreateView");
            long startTime = System.currentTimeMillis();
            
            LogUtils.d(TAG, "开始初始化界面");
            View view = inflater.inflate(R.layout.fragment_debug_contacts, container, false);
            
            debugTextView = view.findViewById(R.id.debugTextView);
            if (debugTextView == null) {
                LogUtils.e(TAG, "界面组件未找到", new IllegalStateException("TextView为空"));
                return view;
            }
            
            swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
            if (swipeRefreshLayout == null) {
                LogUtils.e(TAG, "界面组件未找到", new IllegalStateException("SwipeRefreshLayout为空"));
                return view;
            }
            
            LogUtils.d(TAG, "初始化ViewModel");
            viewModel = new ViewModelProvider(this).get(ContactsDebugViewModel.class);
            
            swipeRefreshLayout.setOnRefreshListener(() -> {
                try {
                    LogUtils.d(TAG, "用户下拉刷新");
                    viewModel.refreshContacts();
                    swipeRefreshLayout.setRefreshing(false);
                } catch (Exception e) {
                    LogUtils.logError(TAG, "刷新联系人失败", e);
                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            LogUtils.d(TAG, "开始观察联系人数据");
            viewModel.getContactsWithPhoneNumbers().observe(getViewLifecycleOwner(), contacts -> {
                try {
                    LogUtils.d(TAG, String.format("收到%d个联系人数据", contacts.size()));
                    long updateStartTime = System.currentTimeMillis();
                    
                    StringBuilder builder = new StringBuilder();
                    builder.append("联系人总数: ").append(contacts.size()).append("\n");
                    
                    int safeZoneCount = 0;
                    int tempZoneCount = 0;
                    int blacklistCount = 0;
                    for (ContactWithPhoneNumbers contact : contacts) {
                        if (contact.contact.isSafeZone) safeZoneCount++;
                        if (contact.contact.isTempZone) tempZoneCount++;
                        if (contact.contact.isBlacklist) blacklistCount++;
                    }
                    
                    builder.append("安全区联系人: ").append(safeZoneCount).append("\n");
                    builder.append("临时区联系人: ").append(tempZoneCount).append("\n");
                    builder.append("黑名单联系人: ").append(blacklistCount).append("\n\n");

                    LogUtils.d(TAG, String.format("联系人统计 - 安全区: %d, 临时区: %d, 黑名单: %d", 
                        safeZoneCount, tempZoneCount, blacklistCount));

                    for (ContactWithPhoneNumbers contact : contacts) {
                        builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                        builder.append("ID: ").append(contact.contact.id).append("\n");
                        builder.append("姓名: ").append(contact.contact.name).append("\n");
                        builder.append("状态: ");
                        if (contact.contact.isSafeZone) builder.append("[安全区] ");
                        if (contact.contact.isTempZone) builder.append("[临时区] ");
                        if (contact.contact.isBlacklist) builder.append("[黑名单] ");
                        if (contact.contact.isDeleted) builder.append("[已删除] ");
                        builder.append("\n");
                        builder.append("电话号码:\n");
                        for (PhoneNumberEntity phone : contact.phoneNumbers) {
                            builder.append("  ").append(phone.phoneNumber)
                                   .append(phone.isPrimary ? " (主要)" : "")
                                   .append("\n");
                        }
                        builder.append("最后更新: ").append(FormatUtils.formatDate(contact.contact.lastUpdated)).append("\n");
                        builder.append("\n");
                        
                        LogUtils.v(TAG, String.format("处理联系人 - ID: %d, 姓名: %s, 电话数量: %d", 
                            contact.contact.id, contact.contact.name, contact.phoneNumbers.size()));
                    }
                    debugTextView.setText(builder.toString());
                    
                    LogUtils.logPerformance(TAG, "更新联系人显示", updateStartTime);
                } catch (Exception e) {
                    LogUtils.logError(TAG, "更新联系人显示失败", e);
                }
            });

            LogUtils.logPerformance(TAG, "Fragment初始化", startTime);
            LogUtils.logMethodExit(TAG, "onCreateView");
            return view;
        } catch (Exception e) {
            LogUtils.logError(TAG, "创建视图失败", e);
            return null;
        }
    }
} 