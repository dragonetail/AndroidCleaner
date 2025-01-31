package com.blackharry.androidcleaner.recordings.data;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.blackharry.androidcleaner.calls.data.CallsDebugFragment;
import com.blackharry.androidcleaner.contacts.data.ContactsDebugFragment;
import com.blackharry.androidcleaner.utils.LogUtils;

public class DatabaseDebugPagerAdapter extends FragmentStateAdapter {
    private static final String TAG = "DatabaseDebugAdapter";
    private static final int PAGE_COUNT = 4;

    public DatabaseDebugPagerAdapter(FragmentActivity activity) {
        super(activity);
        LogUtils.logMethodEnter(TAG, "构造函数");
        LogUtils.logMethodExit(TAG, "构造函数");
    }

    @Override
    public Fragment createFragment(int position) {
        try {
            LogUtils.logMethodEnter(TAG, "createFragment");
            LogUtils.d(TAG, String.format("创建页面[%d]", position));
            
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = new DatabaseStatsFragment();
                    LogUtils.d(TAG, "创建数据库概览页面");
                    break;
                case 1:
                    fragment = new RecordingsDebugFragment();
                    LogUtils.d(TAG, "创建录音调试页面");
                    break;
                case 2:
                    fragment = new CallsDebugFragment();
                    LogUtils.d(TAG, "创建通话调试页面");
                    break;
                case 3:
                    fragment = new ContactsDebugFragment();
                    LogUtils.d(TAG, "创建联系人调试页面");
                    break;
                default:
                    LogUtils.e(TAG, String.format("未知页面位置: %d", position), 
                        new IllegalArgumentException("Invalid position"));
                    fragment = new Fragment();
                    break;
            }
            
            LogUtils.logMethodExit(TAG, "createFragment");
            return fragment;
        } catch (Exception e) {
            LogUtils.logError(TAG, "创建页面失败", e);
            return new Fragment(); // 返回空白页面作为降级处理
        }
    }

    @Override
    public int getItemCount() {
        try {
            LogUtils.logMethodEnter(TAG, "getItemCount");
            LogUtils.d(TAG, String.format("页面总数: %d", PAGE_COUNT));
            LogUtils.logMethodExit(TAG, "getItemCount");
            return PAGE_COUNT;
        } catch (Exception e) {
            LogUtils.logError(TAG, "获取页面数量失败", e);
            return 0; // 返回0作为降级处理
        }
    }

    @Override
    public long getItemId(int position) {
        try {
            LogUtils.logMethodEnter(TAG, "getItemId");
            long itemId = super.getItemId(position);
            LogUtils.d(TAG, String.format("页面[%d]的ID: %d", position, itemId));
            LogUtils.logMethodExit(TAG, "getItemId");
            return itemId;
        } catch (Exception e) {
            LogUtils.logError(TAG, "获取页面ID失败", e);
            return -1; // 返回-1作为降级处理
        }
    }

    @Override
    public boolean containsItem(long itemId) {
        try {
            LogUtils.logMethodEnter(TAG, "containsItem");
            boolean contains = super.containsItem(itemId);
            LogUtils.d(TAG, String.format("是否包含ID为%d的页面: %b", itemId, contains));
            LogUtils.logMethodExit(TAG, "containsItem");
            return contains;
        } catch (Exception e) {
            LogUtils.logError(TAG, "检查页面ID失败", e);
            return false; // 返回false作为降级处理
        }
    }
}