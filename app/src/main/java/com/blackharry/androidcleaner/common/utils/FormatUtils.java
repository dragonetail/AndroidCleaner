package com.blackharry.androidcleaner.common.utils;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FormatUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    private static final long KB = 1024L;
    private static final long MB = KB * 1024L;
    private static final long GB = MB * 1024L;

    public static String formatDate(long timestamp) {
        return DateFormat.format("yyyy/M/d", timestamp).toString();
    }

    public static String formatDateTime(long timestamp) {
        return DateFormat.format("yyyy/M/d HH:mm:ss", timestamp).toString();
    }

    public static String formatFileSize(Context context, long size) {
        return Formatter.formatFileSize(context, size);
    }

    public static String formatFileSize(long size) {
        if (size >= GB) {
            return String.format(Locale.CHINA, "%.2f GB", (float) size / GB);
        } else if (size >= MB) {
            return String.format(Locale.CHINA, "%.2f MB", (float) size / MB);
        } else if (size >= KB) {
            return String.format(Locale.CHINA, "%.2f KB", (float) size / KB);
        } else {
            return String.format(Locale.CHINA, "%d B", size);
        }
    }

    public static String formatDuration(long durationMs) {
        if (durationMs < 0) {
            return "00:00";
        }
        
        long hours = TimeUnit.MILLISECONDS.toHours(durationMs);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60;
        
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() != 11) {
            return phoneNumber;
        }
        return phoneNumber.substring(0, 3) + " " + phoneNumber.substring(3, 7) + " " + phoneNumber.substring(7);
    }
} 