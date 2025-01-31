package com.blackharry.androidcleaner.recordings.data;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.blackharry.androidcleaner.calls.data.CallEntity;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;
import com.blackharry.androidcleaner.contacts.data.PhoneNumberEntity;
import com.blackharry.androidcleaner.utils.LogUtils;
import com.blackharry.androidcleaner.utils.AppExecutors;
import java.util.List;

public class DatabaseStatsViewModel extends AndroidViewModel {
    private static final String TAG = "DatabaseStatsViewModel";
    private final AppDatabase database;
    private final MutableLiveData<DatabaseStats> databaseStats;
    private final AppExecutors executors;
    private final MutableLiveData<Integer> recordingsCount = new MutableLiveData<>();
    private final MutableLiveData<Long> recordingsSize = new MutableLiveData<>();
    private final MutableLiveData<Integer> callsCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> contactsCount = new MutableLiveData<>();

    public DatabaseStatsViewModel(Application application) {
        super(application);
        LogUtils.logMethodEnter(TAG, "构造函数");
        database = AppDatabase.getDatabase(application);
        databaseStats = new MutableLiveData<>();
        executors = AppExecutors.getInstance();
        LogUtils.logMethodExit(TAG, "构造函数");
        loadStats();
    }

    public LiveData<DatabaseStats> getDatabaseStats() {
        return databaseStats;
    }

    public void refresh() {
        try {
            LogUtils.logMethodEnter(TAG, "refresh");
            long startTime = System.currentTimeMillis();

            executors.diskIO().execute(() -> {
                try {
                    LogUtils.d(TAG, "开始加载数据库统计信息");
                    DatabaseStats stats = new DatabaseStats();

                    // 加载录音统计
                    stats.recordingsCount = database.recordingDao().getCount();
                    stats.recordingsTotalSize = database.recordingDao().getTotalSize();
                    stats.recordingsTotalDuration = database.recordingDao().getTotalDuration();
                    LogUtils.d(TAG, String.format("录音统计 - 数量: %d, 大小: %d, 时长: %d", 
                        stats.recordingsCount, stats.recordingsTotalSize, stats.recordingsTotalDuration));

                    // 加载通话统计
                    stats.callsCount = database.callDao().getCount();
                    stats.callsTotalDuration = database.callDao().getTotalDuration();
                    stats.callsRecordingSize = database.callDao().getTotalRecordingSize();
                    LogUtils.d(TAG, String.format("通话统计 - 数量: %d, 时长: %d, 录音大小: %d", 
                        stats.callsCount, stats.callsTotalDuration, stats.callsRecordingSize));

                    // 加载联系人统计
                    stats.contactsCount = database.contactDao().getCount();
                    stats.safeZoneCount = database.contactDao().getSafeZoneCount();
                    stats.tempZoneCount = database.contactDao().getTempZoneCount();
                    stats.blacklistCount = database.contactDao().getBlacklistCount();
                    stats.phoneNumbersCount = database.contactDao().getPhoneNumbersCount();
                    LogUtils.d(TAG, String.format("联系人统计 - 总数: %d, 安全区: %d, 临时区: %d, 黑名单: %d, 号码数: %d",
                        stats.contactsCount, stats.safeZoneCount, stats.tempZoneCount, 
                        stats.blacklistCount, stats.phoneNumbersCount));

                    executors.mainThread().execute(() -> {
                        databaseStats.setValue(stats);
                        LogUtils.logPerformance(TAG, "加载数据库统计", startTime);
                    });
                } catch (Exception e) {
                    LogUtils.logError(TAG, "加载数据库统计失败", e);
                }
            });

            LogUtils.logMethodExit(TAG, "refresh");
        } catch (Exception e) {
            LogUtils.logError(TAG, "刷新数据库统计时发生异常", e);
        }
    }

    private void loadStats() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            DatabaseStats stats = new DatabaseStats();

            // 加载录音统计
            List<RecordingEntity> recordings = database.recordingDao().getAllRecordingsSync();
            stats.recordingsCount = recordings.size();
            for (RecordingEntity recording : recordings) {
                stats.recordingsTotalSize += recording.getFileSize();
                stats.recordingsTotalDuration += recording.getDuration();
            }

            // 加载通话统计
            List<CallEntity> calls = database.callDao().getAllCallsSync();
            stats.callsCount = calls.size();
            for (CallEntity call : calls) {
                stats.callsTotalDuration += call.callDuration;
                stats.callsRecordingSize += call.recordingFilesize;
            }

            // 加载联系人统计
            List<ContactEntity> contacts = database.contactDao().getAllContactsSync();
            stats.contactsCount = contacts.size();
            for (ContactEntity contact : contacts) {
                if (contact.isSafeZone) stats.safeZoneCount++;
                if (contact.isTempZone) stats.tempZoneCount++;
                if (contact.isBlacklist) stats.blacklistCount++;
            }

            // 加载电话号码统计
            List<PhoneNumberEntity> phoneNumbers = database.contactDao().getAllPhoneNumbersSync();
            stats.phoneNumbersCount = phoneNumbers.size();

            databaseStats.postValue(stats);
        });
    }

    public LiveData<Integer> getRecordingsCount() {
        return recordingsCount;
    }

    public LiveData<Long> getRecordingsSize() {
        return recordingsSize;
    }

    public LiveData<Integer> getCallsCount() {
        return callsCount;
    }

    public LiveData<Integer> getContactsCount() {
        return contactsCount;
    }

    public void refreshStats(Runnable onComplete) {
        try {
            LogUtils.logMethodEnter(TAG, "refreshStats");
            long startTime = System.currentTimeMillis();

            executors.diskIO().execute(() -> {
                try {
                    LogUtils.d(TAG, "开始刷新数据库统计信息");
                    
                    // 更新录音统计
                    recordingsCount.postValue(database.recordingDao().getCount());
                    recordingsSize.postValue(database.recordingDao().getTotalSize());
                    
                    // 更新通话统计
                    callsCount.postValue(database.callDao().getCount());
                    
                    // 更新联系人统计
                    contactsCount.postValue(database.contactDao().getCount());

                    executors.mainThread().execute(() -> {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                        LogUtils.logPerformance(TAG, "刷新数据库统计", startTime);
                    });
                } catch (Exception e) {
                    LogUtils.logError(TAG, "刷新数据库统计失败", e);
                }
            });

            LogUtils.logMethodExit(TAG, "refreshStats");
        } catch (Exception e) {
            LogUtils.logError(TAG, "刷新数据库统计时发生异常", e);
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        LogUtils.logMethodEnter(TAG, "onCleared");
        // 清理资源
        LogUtils.logMethodExit(TAG, "onCleared");
    }

    public static class DatabaseStats {
        public int recordingsCount;
        public long recordingsTotalSize;
        public long recordingsTotalDuration;
        public int callsCount;
        public long callsTotalDuration;
        public long callsRecordingSize;
        public int contactsCount;
        public int safeZoneCount;
        public int tempZoneCount;
        public int blacklistCount;
        public int phoneNumbersCount;
    }
} 