package com.blackharry.androidcleaner.common.utils;

import android.content.Context;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogUtils {
    private static final String TAG = "AndroidCleaner";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);
    private static boolean isDebug = true;

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static void init(Context context) {
        // 初始化日志系统
        isDebug = true;
    }

    public static void cleanup() {
        // 清理日志系统资源
    }

    public static void d(String tag, String message) {
        if (isDebug) {
            log(Log.DEBUG, String.format("[%s] %s", tag, message));
        }
    }

    public static void i(String message) {
        log(Log.INFO, message);
    }

    public static void i(String tag, String message) {
        log(Log.INFO, String.format("[%s] %s", tag, message));
    }

    public static void w(String message) {
        log(Log.WARN, message);
    }

    public static void w(String tag, String message) {
        log(Log.WARN, String.format("[%s] %s", tag, message));
    }

    public static void e(String message) {
        log(Log.ERROR, message);
    }

    public static void e(String tag, String message) {
        log(Log.ERROR, String.format("[%s] %s", tag, message));
    }

    public static void e(String message, Throwable throwable) {
        log(Log.ERROR, message + "\n" + Log.getStackTraceString(throwable));
    }

    public static void e(String tag, String message, Throwable throwable) {
        log(Log.ERROR, String.format("[%s] %s\n%s", tag, message, Log.getStackTraceString(throwable)));
    }

    public static void logMethodEnter(String tag, String methodName) {
        if (isDebug) {
            log(Log.DEBUG, String.format("[%s] 进入方法: %s", tag, methodName));
        }
    }

    public static void logMethodExit(String tag, String methodName) {
        if (isDebug) {
            log(Log.DEBUG, String.format("[%s] 退出方法: %s", tag, methodName));
        }
    }

    public static void logError(String tag, String message, Exception e) {
        log(Log.ERROR, String.format("[%s] %s\n%s", tag, message, Log.getStackTraceString(e)));
    }

    private static void log(int priority, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String threadInfo = Thread.currentThread().getName();
        String levelName = getLevelName(priority);
        
        String formattedMessage = String.format("[%s] [%s] [%s] %s", 
            timestamp, threadInfo, levelName, message);
        
        Log.println(priority, TAG, formattedMessage);
    }

    private static String getLevelName(int priority) {
        switch (priority) {
            case Log.VERBOSE: return "VERBOSE";
            case Log.DEBUG: return "DEBUG";
            case Log.INFO: return "INFO";
            case Log.WARN: return "WARN";
            case Log.ERROR: return "ERROR";
            default: return "UNKNOWN";
        }
    }

    /**
     * 记录性能日志
     * @param tag 日志标签
     * @param message 日志消息
     * @param startTime 开始时间
     */
    public static void logPerformance(String tag, String message, long startTime) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log(Log.INFO, String.format("[%s] %s - 耗时: %d毫秒", tag, message, duration));
    }

    /**
     * 记录性能日志
     * @param tag 日志标签
     * @param message 日志消息
     */
    public static void logPerformance(String tag, String message) {
        log(Log.INFO, String.format("[%s] %s", tag, message));
    }
} 