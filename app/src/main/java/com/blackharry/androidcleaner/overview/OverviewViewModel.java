package com.blackharry.androidcleaner.overview;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.blackharry.androidcleaner.AppDatabase;
import com.blackharry.androidcleaner.common.exception.AppException;
import com.blackharry.androidcleaner.common.exception.ErrorCode;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.blackharry.androidcleaner.common.utils.StorageUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverviewViewModel extends AndroidViewModel {
    private static final String TAG = "OverviewViewModel";
    private final AppDatabase database;
    private final ExecutorService executorService;
    
    private final MutableLiveData<Long> totalStorageSize = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer> recordingCount = new MutableLiveData<>(0);
    private final MutableLiveData<Long> recordingSize = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer> callCount = new MutableLiveData<>(0);
    private final MutableLiveData<Long> totalCallDuration = new MutableLiveData<>(0L);
    private final MutableLiveData<Integer> contactCount = new MutableLiveData<>(0);
    private final MutableLiveData<String> cleanupSuggestion = new MutableLiveData<>("");
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public OverviewViewModel(Application application) {
        super(application);
        LogUtils.logMethodEnter(TAG, "OverviewViewModel");
        database = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
        loadData();
    }

    public LiveData<Long> getTotalStorageSize() {
        return totalStorageSize;
    }

    public LiveData<Integer> getRecordingCount() {
        return recordingCount;
    }

    public LiveData<Long> getRecordingSize() {
        return recordingSize;
    }

    public LiveData<Integer> getCallCount() {
        return callCount;
    }

    public LiveData<Long> getTotalCallDuration() {
        return totalCallDuration;
    }

    public LiveData<Integer> getContactCount() {
        return contactCount;
    }

    public LiveData<String> getCleanupSuggestion() {
        return cleanupSuggestion;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<StorageUsage> getStorageUsage() {
        return Transformations.map(recordingSize, size -> {
            long totalSize = StorageUtils.getTotalStorageSize(getApplication());
            return new StorageUsage(size, totalSize);
        });
    }

    public LiveData<Statistics> getStatistics() {
        return Transformations.map(totalCallDuration, duration -> {
            Statistics stats = new Statistics();
            stats.setRecordingCount(recordingCount.getValue());
            stats.setCallCount(callCount.getValue());
            stats.setContactCount(contactCount.getValue());
            stats.setTotalDuration(duration);
            return stats;
        });
    }

    public static class StorageUsage {
        private final long usedSize;
        private final long totalSize;

        public StorageUsage(long usedSize, long totalSize) {
            this.usedSize = usedSize;
            this.totalSize = totalSize;
        }

        public float getUsedPercentage() {
            return totalSize > 0 ? (float) usedSize / totalSize : 0;
        }

        public String getFormattedUsedSize() {
            return StorageUtils.formatSize(usedSize);
        }
    }

    public static class Statistics {
        private int recordingCount;
        private int callCount;
        private int contactCount;
        private long totalDuration;

        public int getRecordingCount() {
            return recordingCount;
        }

        public void setRecordingCount(Integer count) {
            this.recordingCount = count != null ? count : 0;
        }

        public int getCallCount() {
            return callCount;
        }

        public void setCallCount(Integer count) {
            this.callCount = count != null ? count : 0;
        }

        public int getContactCount() {
            return contactCount;
        }

        public void setContactCount(Integer count) {
            this.contactCount = count != null ? count : 0;
        }

        public long getTotalDuration() {
            return totalDuration;
        }

        public void setTotalDuration(Long duration) {
            this.totalDuration = duration != null ? duration : 0;
        }

        public String getFormattedTotalDuration() {
            long hours = totalDuration / 3600;
            long minutes = (totalDuration % 3600) / 60;
            return String.format("%d小时%d分钟", hours, minutes);
        }
    }

    private void loadData() {
        LogUtils.logMethodEnter(TAG, "loadData");
        long startTime = System.currentTimeMillis();

        executorService.execute(() -> {
            try {
                // 加载录音统计
                int recordings = database.recordingDao().getCount();
                recordingCount.postValue(recordings);
                
                long size = database.recordingDao().getTotalSize();
                recordingSize.postValue(size);
                totalStorageSize.postValue(size);

                // 加载通话统计
                int calls = database.callDao().getCount();
                callCount.postValue(calls);
                
                long duration = database.callDao().getTotalDuration();
                totalCallDuration.postValue(duration);

                // 加载联系人统计
                int contacts = database.contactDao().getCount();
                contactCount.postValue(contacts);

                // 生成清理建议
                generateCleanupSuggestion(recordings, size, calls);

                LogUtils.logPerformance(TAG, "数据加载完成", startTime);
            } catch (Exception e) {
                LogUtils.e(TAG, "数据加载失败", e);
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "数据加载失败";
                error.postValue(errorMessage);
                throw new AppException(ErrorCode.DATABASE_ERROR, "数据加载失败", e);
            }
        });
    }

    private void generateCleanupSuggestion(int recordingCount, long totalSize, int callCount) {
        try {
            StringBuilder suggestion = new StringBuilder();
            
            if (totalSize > 1024 * 1024 * 1024) { // 大于1GB
                suggestion.append("录音文件占用空间较大，建议清理\n");
            }
            
            if (recordingCount > 1000) {
                suggestion.append("录音数量过多，建议清理过期录音\n");
            }
            
            if (callCount > 5000) {
                suggestion.append("通话记录较多，建议清理旧记录\n");
            }
            
            cleanupSuggestion.postValue(suggestion.toString().trim());
        } catch (Exception e) {
            LogUtils.e(TAG, "生成清理建议失败", e);
            throw new AppException(ErrorCode.SYSTEM_ERROR, "生成清理建议失败", e);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        LogUtils.i(TAG, "ViewModel销毁");
        executorService.shutdown();
    }
} 