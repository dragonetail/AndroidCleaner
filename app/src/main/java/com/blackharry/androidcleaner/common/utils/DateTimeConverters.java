package com.blackharry.androidcleaner.common.utils;

import androidx.room.TypeConverter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Room数据库日期时间类型转换器
 * 用于在LocalDateTime和long之间进行转换
 */
public class DateTimeConverters {
    @TypeConverter
    public static LocalDateTime fromTimestamp(Long value) {
        return value == null ? null : 
            LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }

    @TypeConverter
    public static Long dateToTimestamp(LocalDateTime date) {
        return date == null ? null : 
            date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static String formatDateForFileName(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
} 