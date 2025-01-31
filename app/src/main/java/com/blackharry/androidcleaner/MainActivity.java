package com.blackharry.androidcleaner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;
import com.blackharry.androidcleaner.recordings.ui.RecordingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.blackharry.androidcleaner.overview.OverviewFragment;
import com.blackharry.androidcleaner.calls.ui.CallsFragment;
import com.blackharry.androidcleaner.contacts.ui.ContactsFragment;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private final String[] REQUIRED_PERMISSIONS;

    public MainActivity() {
        // 根据Android版本选择不同的权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            REQUIRED_PERMISSIONS = new String[] {
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_CALL_LOG
            };
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            REQUIRED_PERMISSIONS = new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_CALL_LOG
            };
        } else {
            REQUIRED_PERMISSIONS = new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_CALL_LOG
            };
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            LogUtils.logMethodEnter(TAG, "onCreate");
            long startTime = System.currentTimeMillis();
            
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            LogUtils.d(TAG, "开始初始化界面组件");

            initializeToolbar();
            initializeBottomNavigation();
            checkPermissions();

            LogUtils.logPerformance(TAG, "界面初始化", startTime);
            LogUtils.logMethodExit(TAG, "onCreate");
        } catch (Exception e) {
            LogUtils.logError(TAG, "初始化失败", e);
            Toast.makeText(this, "启动失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitle(R.string.app_name);
        }
    }

    private void initializeBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView == null) {
            LogUtils.logError(TAG, "BottomNavigationView not found in layout", 
                new IllegalStateException("View not found"));
            return;
        }
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_overview) {
                showFragment(new OverviewFragment());
                return true;
            } else if (itemId == R.id.navigation_recordings) {
                showFragment(new RecordingsFragment());
                return true;
            } else if (itemId == R.id.navigation_calls) {
                showFragment(new CallsFragment());
                return true;
            } else if (itemId == R.id.navigation_contacts) {
                showFragment(new ContactsFragment());
                return true;
            }
            return false;
        });
        
        // 默认选中概览页面
        bottomNavigationView.setSelectedItemId(R.id.navigation_overview);
    }

    private void showFragment(Fragment fragment) {
        try {
            LogUtils.logMethodEnter(TAG, "showFragment: " + fragment.getClass().getSimpleName());
            long startTime = System.currentTimeMillis();
            
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
            
            LogUtils.logPerformance(TAG, "切换Fragment", startTime);
            LogUtils.logMethodExit(TAG, "showFragment");
        } catch (Exception e) {
            LogUtils.logError(TAG, "切换Fragment失败", e);
            Toast.makeText(this, "切换页面失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkPermissions() {
        LogUtils.logMethodEnter(TAG, "checkPermissions");
        List<String> permissionsNeeded = new ArrayList<>();
        
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != 
                    PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
                LogUtils.d(TAG, String.format("需要申请权限: %s", permission));
            }
        }
        
        if (!permissionsNeeded.isEmpty()) {
            LogUtils.i(TAG, String.format("需要申请的权限数量: %d", permissionsNeeded.size()));
            
            boolean shouldShowRationale = false;
            for (String permission : permissionsNeeded) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    shouldShowRationale = true;
                    LogUtils.d(TAG, String.format("需要显示权限说明: %s", permission));
                    break;
                }
            }

            if (shouldShowRationale) {
                LogUtils.d(TAG, "显示权限说明对话框");
                new AlertDialog.Builder(this)
                    .setTitle("需要权限")
                    .setMessage("应用需要存储权限来访问录音文件，需要通话记录权限来识别通话录音。")
                    .setPositiveButton("授权", (dialog, which) -> {
                        LogUtils.d(TAG, "用户同意授权，开始申请权限");
                        ActivityCompat.requestPermissions(this, 
                            permissionsNeeded.toArray(new String[0]), 
                            PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("退出", (dialog, which) -> {
                        LogUtils.w(TAG, "用户拒绝授权，退出应用");
                        finish();
                    })
                    .setCancelable(false)
                    .show();
            } else {
                LogUtils.d(TAG, "直接申请权限");
                ActivityCompat.requestPermissions(this, 
                    permissionsNeeded.toArray(new String[0]), 
                    PERMISSION_REQUEST_CODE);
            }
        } else {
            LogUtils.i(TAG, "所有权限已获得，显示概览页面");
            showFragment(new OverviewFragment());
        }
        LogUtils.logMethodExit(TAG, "checkPermissions");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        LogUtils.logMethodEnter(TAG, "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                    LogUtils.w(TAG, String.format("权限被拒绝: %s", permissions[i]));
                } else {
                    LogUtils.i(TAG, String.format("权限已授予: %s", permissions[i]));
                }
            }
            
            if (deniedPermissions.isEmpty()) {
                LogUtils.i(TAG, "所有权限已获得，显示概览页面");
                showFragment(new OverviewFragment());
            } else {
                LogUtils.w(TAG, String.format("有%d个权限被拒绝", deniedPermissions.size()));
                StringBuilder message = new StringBuilder("需要以下权限才能正常运行：\n");
                for (String permission : deniedPermissions) {
                    String permissionName = getPermissionName(permission);
                    message.append("• ").append(permissionName).append("\n");
                    LogUtils.d(TAG, String.format("被拒绝的权限: %s", permissionName));
                }
                Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show();
                
                new AlertDialog.Builder(this)
                    .setTitle("权限被拒绝")
                    .setMessage("没有必要的权限，应用无法正常工作。")
                    .setPositiveButton("重试", (dialog, which) -> checkPermissions())
                    .setNegativeButton("退出", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
            }
        }
        LogUtils.logMethodExit(TAG, "onRequestPermissionsResult");
    }

    private String getPermissionName(String permission) {
        switch (permission) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.READ_MEDIA_AUDIO:
                return "存储权限";
            case Manifest.permission.READ_CALL_LOG:
                return "通话记录权限";
            default:
                return permission;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.i(TAG, "MainActivity销毁");
    }
}