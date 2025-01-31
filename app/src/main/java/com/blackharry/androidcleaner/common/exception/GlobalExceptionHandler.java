package com.blackharry.androidcleaner.common.exception;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.blackharry.androidcleaner.common.utils.LogUtils;

/**
 * 全局异常处理器
 * 负责捕获和处理应用中未被捕获的异常，包括：
 * 1. 记录异常日志
 * 2. 收集设备信息
 * 3. 保存崩溃报告
 * 4. 向用户显示友好的错误提示
 *
 * @author BlackHarry
 * @version 1.0
 * @since 2024-01-31
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "GlobalExceptionHandler";
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;
    
    /**
     * 构造函数
     * @param context 应用上下文
     */
    public GlobalExceptionHandler(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        LogUtils.e(TAG, "未捕获的异常：" + ex.getMessage(), ex);
        
        boolean handled = handleException(ex);
        
        if (!handled && defaultHandler != null) {
            defaultHandler.uncaughtException(thread, ex);
        }
    }
    
    /**
     * 处理异常
     * @param ex 捕获的异常
     * @return 是否成功处理
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        
        try {
            // 在主线程显示错误信息
            new Handler(Looper.getMainLooper()).post(() -> {
                String message;
                if (ex instanceof AppException) {
                    message = ((AppException) ex).getErrorCode().getMessage();
                } else {
                    message = "应用发生错误，请稍后重试";
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            });
            
            // 收集错误信息
            collectErrorInfo(ex);
            
            return true;
        } catch (Exception e) {
            LogUtils.e(TAG, "处理异常时出错", e);
            return false;
        }
    }
    
    /**
     * 收集错误信息并保存到文件
     * @param ex 捕获的异常
     */
    private void collectErrorInfo(Throwable ex) {
        StringBuilder info = new StringBuilder();
        // 收集设备信息
        info.append("设备信息：\n");
        info.append("设备型号：").append(android.os.Build.MODEL).append("\n");
        info.append("Android版本：").append(android.os.Build.VERSION.RELEASE).append("\n");
        info.append("系统版本号：").append(android.os.Build.VERSION.SDK_INT).append("\n");
        info.append("制造商：").append(android.os.Build.MANUFACTURER).append("\n");
        
        // 收集异常信息
        info.append("\n异常信息：\n");
        info.append("异常类型：").append(ex.getClass().getName()).append("\n");
        info.append("异常信息：").append(ex.getMessage()).append("\n");
        info.append("异常堆栈：\n");
        for (StackTraceElement element : ex.getStackTrace()) {
            info.append("\tat ").append(element.toString()).append("\n");
        }
        
        // 保存错误日志到文件
        String fileName = "crash-" + System.currentTimeMillis() + ".log";
        try {
            java.io.File dir = new java.io.File(context.getExternalFilesDir(null), "crash_logs");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            java.io.File file = new java.io.File(dir, fileName);
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(info.toString());
            writer.close();
            LogUtils.i(TAG, "错误日志已保存到：" + file.getAbsolutePath());
        } catch (Exception e) {
            LogUtils.e(TAG, "保存错误日志失败", e);
        }
    }
    
    /**
     * 初始化全局异常处理器
     * @param context 应用上下文
     */
    public static void init(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(context));
    }
}