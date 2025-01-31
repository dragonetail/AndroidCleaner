package com.blackharry.androidcleaner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;
import com.blackharry.androidcleaner.recordings.RecordingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import com.blackharry.androidcleaner.recordings.data.DatabaseDebugActivity;
import android.app.AlertDialog;
import com.blackharry.androidcleaner.utils.LogUtils;

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

            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar == null) {
                Log.e(TAG, "onCreate: Toolbar not found in layout");
                return;
            }
            setSupportActionBar(toolbar);

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
            if (bottomNavigationView == null) {
                Log.e(TAG, "onCreate: BottomNavigationView not found in layout");
                return;
            }
            bottomNavigationView.setOnItemSelectedListener(item -> {
                String title = item.getTitle().toString();
                switch (title) {
                    case "recordings":
                        showRecordingsFragment();
                        return true;
                    case "categories":
                        // Handle categories action
                        return true;
                    case "settings":
                        // Handle settings action
                        return true;
                }
                return false;
            });

            // 检查并请求权限
            checkPermissions();

            LogUtils.logPerformance(TAG, "界面初始化", startTime);
            LogUtils.logMethodExit(TAG, "onCreate");
        } catch (Exception e) {
            LogUtils.logError(TAG, "初始化失败", e);
            Toast.makeText(this, "启动失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showRecordingsFragment() {
        try {
            LogUtils.logMethodEnter(TAG, "showRecordingsFragment");
            long startTime = System.currentTimeMillis();
            
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new RecordingsFragment())
                .commit();
            
            LogUtils.logPerformance(TAG, "加载录音列表", startTime);
            LogUtils.logMethodExit(TAG, "showRecordingsFragment");
            
        } catch (Exception e) {
            LogUtils.logError(TAG, "加载录音列表失败", e);
            Toast.makeText(this, "加载录音列表失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkPermissions() {
        LogUtils.logMethodEnter(TAG, "checkPermissions");
        List<String> permissionsNeeded = new ArrayList<>();
        
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
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
            LogUtils.i(TAG, "所有权限已获得，开始加载录音列表");
            showRecordingsFragment();
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
                LogUtils.i(TAG, "所有权限已获得，开始加载录音列表");
                showRecordingsFragment();
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
                    .setTitle("需要权限")
                    .setMessage("应用需要这些权限才能正常工作。是否重新申请权限？")
                    .setPositiveButton("重试", (dialog, which) -> {
                        LogUtils.d(TAG, "用户选择重试申请权限");
                        checkPermissions();
                    })
                    .setNegativeButton("退出", (dialog, which) -> {
                        LogUtils.w(TAG, "用户放弃申请权限，退出应用");
                        finish();
                    })
                    .setCancelable(false)
                    .show();
            }
        }
        LogUtils.logMethodExit(TAG, "onRequestPermissionsResult");
    }

    private String getPermissionName(String permission) {
        switch (permission) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "读取存储权限";
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return "写入存储权限";
            case Manifest.permission.READ_CALL_LOG:
                return "读取通话记录权限";
            case Manifest.permission.READ_MEDIA_AUDIO:
                return "读取音频文件权限";
            default:
                return permission;
        }
    }

    private void startDebugActivity() {
        Intent intent = new Intent(this, DatabaseDebugActivity.class);
        startActivity(intent);
        // 使用新的Activity转场API
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 
            R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_debug) {
            startDebugActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}