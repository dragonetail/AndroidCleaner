package com.blackharry.androidcleaner;

import android.app.Application;
import android.os.StrictMode;
import com.blackharry.androidcleaner.utils.LogUtils;

public class App extends Application {
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        try {
            LogUtils.logMethodEnter(TAG, "onCreate");
            long startTime = System.currentTimeMillis();
            
            super.onCreate();
            
            // 设置全局异常处理器
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                LogUtils.logError(TAG, String.format(
                    "应用崩溃 - 线程: %s, 异常类型: %s", 
                    thread.getName(), 
                    throwable.getClass().getSimpleName()
                ), throwable);
            });

            // 开启严格模式（仅在Debug模式下）
            if (BuildConfig.DEBUG) {
                LogUtils.d(TAG, "启用严格模式");
                enableStrictMode();
            }
            
            LogUtils.logPerformance(TAG, "应用初始化", startTime);
            LogUtils.logMethodExit(TAG, "onCreate");
        } catch (Exception e) {
            LogUtils.logError(TAG, "应用初始化失败", e);
            throw e;
        }
    }

    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build());
            
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build());
    }
} 