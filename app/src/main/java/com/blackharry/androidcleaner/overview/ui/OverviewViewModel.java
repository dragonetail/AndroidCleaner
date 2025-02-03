package com.blackharry.androidcleaner.overview.ui;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import com.blackharry.androidcleaner.AppDatabase;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.blackharry.androidcleaner.common.utils.TestDataGenerator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverviewViewModel extends AndroidViewModel {
    private static final String TAG = "OverviewViewModel";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<StorageUsage> storageUsage = new MutableLiveData<>();
    private final MutableLiveData<Statistics> statistics = new MutableLiveData<>();

    public OverviewViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<StorageUsage> getStorageUsage() {
        return storageUsage;
    }

    public LiveData<Statistics> getStatistics() {
        return statistics;
    }

    public void refreshData() {
        LogUtils.logMethodEnter(TAG, "refreshData");
        
        executorService.execute(() -> {
            try {
                // 获取存储使用情况
                String storagePath = getApplication().getExternalFilesDir(null).getPath();
                StatFs stat = new StatFs(storagePath);
                long totalBytes = stat.getTotalBytes();
                long availableBytes = stat.getAvailableBytes();
                long usedBytes = totalBytes - availableBytes;
                
                storageUsage.postValue(new StorageUsage(
                    usedBytes,
                    totalBytes,
                    (float) usedBytes / totalBytes
                ));

                // 获取统计数据
                AppDatabase db = AppDatabase.getInstance(getApplication());
                int recordingCount = db.recordingDao().getRecordingCount();
                int callCount = db.callDao().getCallCount();
                int contactCount = db.contactDao().getContactCount();
                long totalDuration = db.recordingDao().getTotalRecordingDuration();

                statistics.postValue(new Statistics(
                    recordingCount,
                    callCount,
                    contactCount,
                    totalDuration
                ));
            } catch (Exception e) {
                LogUtils.logError(TAG, "刷新数据失败", e);
                error.postValue("刷新数据失败：" + e.getMessage());
            }
        });
    }

    public void resetAppState(Runnable onSuccess) {
        LogUtils.logMethodEnter(TAG, "resetAppState");
        
        executorService.execute(() -> {
            try {
                // 清空数据库
                AppDatabase db = AppDatabase.getInstance(getApplication());
                db.clearAllTables();
                LogUtils.i(TAG, "数据库已清空");

                // 清除SharedPreferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
                prefs.edit().clear().apply();
                LogUtils.i(TAG, "SharedPreferences已清空");

                // 加载测试数据
                db.recordingDao().insertAll(TestDataGenerator.generateTestRecordings());
                LogUtils.i(TAG, "测试数据已加载");

                // 通知UI线程重置完成
                new Handler(Looper.getMainLooper()).post(() -> {
                    LogUtils.i(TAG, "应用已重置到初始状态");
                    refreshData(); // 刷新UI数据
                    onSuccess.run();
                });
            } catch (Exception e) {
                LogUtils.logError(TAG, "重置应用状态失败", e);
                error.postValue("重置失败：" + e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    public static class StorageUsage {
        private final long usedSize;
        private final long totalSize;
        private final float usedPercentage;

        public StorageUsage(long usedSize, long totalSize, float usedPercentage) {
            this.usedSize = usedSize;
            this.totalSize = totalSize;
            this.usedPercentage = usedPercentage;
        }

        public String getFormattedUsedSize() {
            return formatSize(usedSize);
        }

        public String getFormattedTotalSize() {
            return formatSize(totalSize);
        }

        public float getUsedPercentage() {
            return usedPercentage;
        }

        private String formatSize(long size) {
            if (size <= 0) return "0 B";
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
        }
    }

    public static class Statistics {
        private final int recordingCount;
        private final int callCount;
        private final int contactCount;
        private final long totalDuration;

        public Statistics(int recordingCount, int callCount, int contactCount, long totalDuration) {
            this.recordingCount = recordingCount;
            this.callCount = callCount;
            this.contactCount = contactCount;
            this.totalDuration = totalDuration;
        }

        public int getRecordingCount() {
            return recordingCount;
        }

        public int getCallCount() {
            return callCount;
        }

        public int getContactCount() {
            return contactCount;
        }

        public String getFormattedTotalDuration() {
            long seconds = totalDuration / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            minutes %= 60;
            
            if (hours > 0) {
                return String.format("%d小时%d分钟", hours, minutes);
            } else {
                return String.format("%d分钟", minutes);
            }
        }
    }
} 