package com.blackharry.androidcleaner;

import android.app.Application;
import android.content.Intent;
import android.os.StrictMode;
import com.blackharry.androidcleaner.sync.DataSyncService;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.blackharry.androidcleaner.common.executor.AppExecutors;
import com.blackharry.androidcleaner.common.exception.GlobalExceptionHandler;

public class App extends Application {
    private static final String TAG = "App";
    private static App instance;

    @Override
    public void onCreate() {
        try {
            LogUtils.logMethodEnter(TAG, "onCreate");
            long startTime = System.currentTimeMillis();
            
            super.onCreate();
            instance = this;
            
            // 初始化全局异常处理器
            GlobalExceptionHandler.init(this);

            // 开启严格模式
            if (BuildConfig.DEBUG) {
                LogUtils.d(TAG, "启用严格模式");
                enableStrictMode();
            }
            
            // 初始化日志系统
            LogUtils.init(this);
            
            // 初始化数据库
            AppDatabase.getInstance(this);
            
            // 启动数据同步服务
            startDataSync();
            
            LogUtils.logPerformance(TAG, "应用初始化", startTime);
            LogUtils.logMethodExit(TAG, "onCreate");
        } catch (Exception e) {
            LogUtils.logError(TAG, "应用初始化失败", e);
            throw e;
        }
    }

    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build());
    }

    private void startDataSync() {
        try {
            LogUtils.i(TAG, "启动数据同步服务");
            Intent syncIntent = new Intent(this, DataSyncService.class);
            startService(syncIntent);
        } catch (Exception e) {
            LogUtils.logError(TAG, "启动数据同步服务失败", e);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogUtils.i(TAG, "应用终止");
        cleanupAndExit();
    }

    private void cleanupAndExit() {
        try {
            LogUtils.i(TAG, "开始清理资源");
            
            // 停止数据同步服务
            stopService(new Intent(this, DataSyncService.class));
            
            // 关闭数据库
            AppDatabase.destroyInstance();
            
            // 关闭线程池
            AppExecutors.getInstance().shutdown();
            
            // 清理日志
            LogUtils.cleanup();
            
            LogUtils.i(TAG, "资源清理完成");
        } catch (Exception e) {
            LogUtils.logError(TAG, "资源清理失败", e);
        }
    }

    public static App getInstance() {
        return instance;
    }
}