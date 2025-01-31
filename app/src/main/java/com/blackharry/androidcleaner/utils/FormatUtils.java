package com.blackharry.androidcleaner.utils;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import java.util.Date;

public class FormatUtils {
    private static final String TAG = "FormatUtils";

    public static String formatDate(long timestamp) {
        try {
            LogUtils.logMethodEnter(TAG, "formatDate");
            LogUtils.d(TAG, String.format("格式化时间戳: %d", timestamp));
            
            String result = DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date(timestamp)).toString();
            
            LogUtils.d(TAG, String.format("格式化结果: %s", result));
            LogUtils.logMethodExit(TAG, "formatDate");
            return result;
        } catch (Exception e) {
            LogUtils.logError(TAG, "格式化日期失败", e);
            return String.valueOf(timestamp);
        }
    }

    public static String formatFileSize(Context context, long size) {
        try {
            LogUtils.logMethodEnter(TAG, "formatFileSize");
            LogUtils.d(TAG, String.format("格式化文件大小: %d字节", size));
            
            String result = Formatter.formatFileSize(context, size);
            
            LogUtils.d(TAG, String.format("格式化结果: %s", result));
            LogUtils.logMethodExit(TAG, "formatFileSize");
            return result;
        } catch (Exception e) {
            LogUtils.logError(TAG, "格式化文件大小失败", e);
            return String.format("%d B", size);
        }
    }

    public static String formatDuration(long seconds) {
        try {
            LogUtils.logMethodEnter(TAG, "formatDuration");
            LogUtils.d(TAG, String.format("格式化时长: %d秒", seconds));
            
            long minutes = seconds / 60;
            seconds = seconds % 60;
            String result = String.format("%02d:%02d", minutes, seconds);
            
            LogUtils.d(TAG, String.format("格式化结果: %s", result));
            LogUtils.logMethodExit(TAG, "formatDuration");
            return result;
        } catch (Exception e) {
            LogUtils.logError(TAG, "格式化时长失败", e);
            return String.format("%ds", seconds);
        }
    }
} 