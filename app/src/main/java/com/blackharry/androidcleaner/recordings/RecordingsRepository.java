package com.blackharry.androidcleaner.recordings;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import com.blackharry.androidcleaner.recordings.data.AppDatabase;
import com.blackharry.androidcleaner.recordings.data.RecordingDao;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import com.blackharry.androidcleaner.utils.AppExecutors;
import com.blackharry.androidcleaner.utils.LogUtils;

public class RecordingsRepository {
    private static final String TAG = "RecordingsRepository";
    private final RecordingDao recordingDao;
    private final AppExecutors executors;
    private final Context context;

    public RecordingsRepository(Context context) {
        LogUtils.logMethodEnter(TAG, "构造函数");
        this.context = context;
        AppDatabase db = AppDatabase.getDatabase(context);
        recordingDao = db.recordingDao();
        executors = AppExecutors.getInstance();
        LogUtils.logMethodExit(TAG, "构造函数");
    }

    public void loadRecordings(OnSuccessListener<List<RecordingEntity>> onSuccess,
            OnErrorListener onError) {
        try {
            LogUtils.logMethodEnter(TAG, "loadRecordings");
            long startTime = System.currentTimeMillis();

            executors.diskIO().execute(() -> {
                try {
                    LogUtils.d(TAG, "开始从数据库加载录音列表");
                    LiveData<List<RecordingEntity>> liveData = recordingDao.getAllRecordings();
                    liveData.observeForever(recordings -> {
                        if (recordings != null) {
                            LogUtils.d(TAG, String.format("从数据库加载到%d个录音", recordings.size()));
                            executors.mainThread().execute(() -> {
                                onSuccess.onSuccess(recordings);
                                LogUtils.logPerformance(TAG, "数据库加载录音列表", startTime);
                            });
                        }
                    });
                } catch (Exception e) {
                    LogUtils.logError(TAG, "从数据库加载录音列表失败", e);
                    executors.mainThread().execute(() -> 
                        onError.onError("加载录音列表失败：" + e.getMessage())
                    );
                }
            });

            LogUtils.logMethodExit(TAG, "loadRecordings");
        } catch (Exception e) {
            LogUtils.logError(TAG, "加载录音列表时发生异常", e);
            onError.onError("加载录音列表失败：" + e.getMessage());
        }
    }

    public void searchRecordings(String query, OnSuccessListener<List<RecordingEntity>> onSuccess,
            OnErrorListener onError) {
        try {
            LogUtils.logMethodEnter(TAG, "searchRecordings");
            LogUtils.d(TAG, String.format("搜索关键词: %s", query));
            long startTime = System.currentTimeMillis();

            executors.diskIO().execute(() -> {
                try {
                    LiveData<List<RecordingEntity>> liveData = recordingDao.searchRecordings("%" + query + "%");
                    liveData.observeForever(results -> {
                        if (results != null) {
                            LogUtils.d(TAG, String.format("搜索到%d个录音", results.size()));
                            executors.mainThread().execute(() -> {
                                onSuccess.onSuccess(results);
                                LogUtils.logPerformance(TAG, "搜索录音文件", startTime);
                            });
                        }
                    });
                } catch (Exception e) {
                    LogUtils.logError(TAG, "搜索录音文件失败", e);
                    executors.mainThread().execute(() -> 
                        onError.onError("搜索录音文件失败：" + e.getMessage())
                    );
                }
            });

            LogUtils.logMethodExit(TAG, "searchRecordings");
        } catch (Exception e) {
            LogUtils.logError(TAG, "搜索录音文件时发生异常", e);
            onError.onError("搜索录音文件失败：" + e.getMessage());
        }
    }

    public void refreshRecordings(String path) {
        try {
            LogUtils.logMethodEnter(TAG, "refreshRecordings");
            LogUtils.d(TAG, String.format("刷新路径: %s", path));
            long startTime = System.currentTimeMillis();

            executors.diskIO().execute(() -> {
                try {
                    File directory = new File(path);
                    if (!directory.exists()) {
                        LogUtils.w(TAG, "录音目录不存在");
                        return;
                    }

                    List<RecordingEntity> recordings = scanRecordingFiles(directory);
                    LogUtils.d(TAG, String.format("扫描到%d个录音文件", recordings.size()));

                    recordingDao.insertAll(recordings);
                    LogUtils.d(TAG, "录音文件信息已更新到数据库");
                    LogUtils.logPerformance(TAG, "刷新录音文件", startTime);
                } catch (Exception e) {
                    LogUtils.logError(TAG, "刷新录音文件失败", e);
                }
            });

            LogUtils.logMethodExit(TAG, "refreshRecordings");
        } catch (Exception e) {
            LogUtils.logError(TAG, "刷新录音文件时发生异常", e);
        }
    }

    private List<RecordingEntity> scanRecordingFiles(File directory) {
        LogUtils.logMethodEnter(TAG, "scanRecordingFiles");
        List<RecordingEntity> recordings = new ArrayList<>();
        File[] files = directory.listFiles((dir, name) -> 
            name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".amr")
        );

        if (files != null) {
            for (File file : files) {
                try {
                    RecordingEntity recording = new RecordingEntity();
                    recording.setFileName(file.getName());
                    recording.setFilePath(file.getAbsolutePath());
                    recording.setFileSize(file.length());
                    recording.setCreatedTime(file.lastModified());
                    recordings.add(recording);
                    LogUtils.d(TAG, String.format("扫描到录音文件: %s", recording.getFileName()));
                } catch (Exception e) {
                    LogUtils.logError(TAG, String.format("处理文件失败: %s", file.getName()), e);
                }
            }
        }

        LogUtils.logMethodExit(TAG, "scanRecordingFiles");
        return recordings;
    }

    public void cleanup() {
        LogUtils.logMethodEnter(TAG, "cleanup");
        // 清理资源
        LogUtils.logMethodExit(TAG, "cleanup");
    }

    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }

    public interface OnErrorListener {
        void onError(String message);
    }
}