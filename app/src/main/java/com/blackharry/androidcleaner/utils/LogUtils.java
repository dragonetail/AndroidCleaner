package com.blackharry.androidcleaner.utils;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogUtils {
    private static final boolean DEBUG = true;  // 在release版本中设为false
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);

    public static void v(String tag, String message) {
        if (DEBUG) {
            String formattedMessage = formatLog(tag, message);
            Log.v(tag, formattedMessage);
        }
    }

    public static void d(String tag, String message) {
        if (DEBUG) {
            String formattedMessage = formatLog(tag, message);
            Log.d(tag, formattedMessage);
        }
    }

    public static void i(String tag, String message) {
        String formattedMessage = formatLog(tag, message);
        Log.i(tag, formattedMessage);
    }

    public static void w(String tag, String message) {
        String formattedMessage = formatLog(tag, message);
        Log.w(tag, formattedMessage);
    }

    public static void e(String tag, String message, Throwable throwable) {
        String formattedMessage = formatLog(tag, message);
        Log.e(tag, formattedMessage, throwable);
    }

    private static String formatLog(String tag, String message) {
        return String.format(Locale.CHINA,
            "[%s] [%s] [%s] %s",
            DATE_FORMAT.format(new Date()),
            Thread.currentThread().getName(),
            tag,
            message
        );
    }

    public static void logMethodEnter(String tag, String methodName) {
        if (DEBUG) {
            d(tag, String.format("进入方法: %s", methodName));
        }
    }

    public static void logMethodExit(String tag, String methodName) {
        if (DEBUG) {
            d(tag, String.format("退出方法: %s", methodName));
        }
    }

    public static void logOperation(String tag, String operation, String details) {
        if (DEBUG) {
            d(tag, String.format("操作: %s, 详情: %s", operation, details));
        }
    }

    public static void logError(String tag, String message, Throwable throwable) {
        e(tag, String.format("错误: %s", message), throwable);
    }

    public static void logPerformance(String tag, String operation, long startTime) {
        if (DEBUG) {
            long duration = System.currentTimeMillis() - startTime;
            d(tag, String.format("性能统计 - %s: %d ms", operation, duration));
        }
    }
} 