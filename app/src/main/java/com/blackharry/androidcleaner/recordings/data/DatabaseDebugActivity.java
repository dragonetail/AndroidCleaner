package com.blackharry.androidcleaner.recordings.data;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.blackharry.androidcleaner.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.blackharry.androidcleaner.utils.LogUtils;
import com.blackharry.androidcleaner.utils.TestDataManager;

@SuppressLint("SetTextI18n")
public class DatabaseDebugActivity extends AppCompatActivity {
    private static final String TAG = "DatabaseDebugActivity";
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            LogUtils.logMethodEnter(TAG, "onCreate");
            long startTime = System.currentTimeMillis();

            super.onCreate(savedInstanceState);
            handler = new Handler(Looper.getMainLooper());
            setContentView(R.layout.activity_database_debug);

            LogUtils.d(TAG, "开始初始化调试界面组件");

            // 设置工具栏
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(R.string.debug_title);
                LogUtils.d(TAG, "工具栏设置完成");
            }

            viewPager = findViewById(R.id.viewPager);
            tabLayout = findViewById(R.id.tabLayout);

            if (viewPager == null || tabLayout == null) {
                LogUtils.e(TAG, "界面组件未找到", new IllegalStateException("ViewPager或TabLayout为空"));
                finish();
                return;
            }

            LogUtils.d(TAG, "开始设置ViewPager适配器");
            DatabaseDebugPagerAdapter pagerAdapter = new DatabaseDebugPagerAdapter(this);
            viewPager.setAdapter(pagerAdapter);

            // 设置TabLayout
            LogUtils.d(TAG, "开始设置TabLayout");
            TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    String tabTitle;
                    switch (position) {
                        case 0:
                            tabTitle = "概览";
                            break;
                        case 1:
                            tabTitle = "录音";
                            break;
                        case 2:
                            tabTitle = "通话";
                            break;
                        case 3:
                            tabTitle = "联系人";
                            break;
                        default:
                            tabTitle = "未知";
                            break;
                    }
                    tab.setText(tabTitle);
                    LogUtils.d(TAG, String.format("设置Tab[%d]: %s", position, tabTitle));
                }
            );
            mediator.attach();

            // 插入测试数据
            LogUtils.i(TAG, "开始插入测试数据");
            TestDataManager.insertTestData(this);

            LogUtils.logPerformance(TAG, "调试界面初始化", startTime);
            LogUtils.logMethodExit(TAG, "onCreate");

        } catch (Exception e) {
            LogUtils.logError(TAG, "调试界面初始化失败", e);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            LogUtils.logMethodEnter(TAG, "onCreateOptionsMenu");
            getMenuInflater().inflate(R.menu.menu_debug, menu);
            LogUtils.logMethodExit(TAG, "onCreateOptionsMenu");
            return true;
        } catch (Exception e) {
            LogUtils.logError(TAG, "创建菜单失败", e);
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            LogUtils.logMethodEnter(TAG, "onOptionsItemSelected");
            if (item.getItemId() == android.R.id.home) {
                LogUtils.d(TAG, "用户点击返回按钮");
                finish();
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 
                    R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            } else if (item.getItemId() == R.id.action_refresh) {
                LogUtils.i(TAG, "用户点击刷新按钮，开始刷新数据");
                // TODO: 实现Fragment刷新机制
                return true;
            }
            LogUtils.logMethodExit(TAG, "onOptionsItemSelected");
            return super.onOptionsItemSelected(item);
        } catch (Exception e) {
            LogUtils.logError(TAG, "菜单操作失败", e);
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        try {
            LogUtils.logMethodEnter(TAG, "onDestroy");
            super.onDestroy();
            // 移除所有待执行的回调，避免内存泄漏
            if (handler != null) {
                LogUtils.d(TAG, "清理Handler回调");
                handler.removeCallbacksAndMessages(null);
            }
            LogUtils.logMethodExit(TAG, "onDestroy");
        } catch (Exception e) {
            LogUtils.logError(TAG, "销毁时发生错误", e);
        }
    }
}