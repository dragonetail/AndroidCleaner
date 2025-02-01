package com.blackharry.androidcleaner.common.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.app.Application;

import java.io.File;

/**
 * 存储工具类，提供存储空间相关的功能
 */
public class StorageUtils {
    
    /**
     * 获取内部存储可用空间（字节）
     */
    public static long getInternalStorageAvailableSpace(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    /**
     * 获取内部存储总空间（字节）
     */
    public static long getInternalStorageTotalSpace(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    /**
     * 获取设备总存储空间（包括内部存储和外部存储）
     */
    public static long getTotalStorageSize(Application application) {
        long internalTotal = getInternalStorageTotalSpace(application);
        long externalTotal = 0;
        
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            externalTotal = totalBlocks * blockSize;
        }
        
        return internalTotal + externalTotal;
    }

    /**
     * 获取文件夹大小（字节）
     */
    public static long getFolderSize(File directory) {
        if (!directory.exists()) {
            return 0;
        }
        
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else {
                    size += getFolderSize(file);
                }
            }
        }
        return size;
    }

    /**
     * 格式化文件大小
     */
    public static String formatSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 检查存储空间是否足够
     */
    public static boolean hasEnoughSpace(Context context, long requiredSpace) {
        return getInternalStorageAvailableSpace(context) >= requiredSpace;
    }

    /**
     * 删除文件或目录
     */
    public static boolean deleteFileOrDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteFileOrDirectory(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }
} 