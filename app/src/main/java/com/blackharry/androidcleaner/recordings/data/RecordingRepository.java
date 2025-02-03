package com.blackharry.androidcleaner.recordings.data;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import androidx.lifecycle.LiveData;
import com.blackharry.androidcleaner.AppDatabase;
import com.blackharry.androidcleaner.common.exception.AppException;
import com.blackharry.androidcleaner.common.exception.ErrorCode;
import com.blackharry.androidcleaner.common.utils.PerformanceMonitor;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordingRepository {
    private static final String TAG = "RecordingRepository";
    private final Context context;
    private final RecordingDao recordingDao;
    private final ExecutorService executorService;
    private static volatile RecordingRepository instance;

    private RecordingRepository(Context context) {
        this.context = context.getApplicationContext();
        this.recordingDao = AppDatabase.getInstance(context).recordingDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static RecordingRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (RecordingRepository.class) {
                if (instance == null) {
                    instance = new RecordingRepository(context);
                }
            }
        }
        return instance;
    }

    public void syncRecordings(String recordingsPath, Callback<Void> callback) {
        LogUtils.logMethodEnter(TAG, "syncRecordings");
        PerformanceMonitor.startOperation("Recording", "syncRecordings");

        executorService.execute(() -> {
            try {
                // 扫描录音文件目录
                File recordingsDir = new File(recordingsPath);
                if (!recordingsDir.exists() || !recordingsDir.isDirectory()) {
                    throw new AppException(ErrorCode.RECORDING_NOT_FOUND, "无效的录音文件目录");
                }

                List<RecordingEntity> recordings = new ArrayList<>();
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();

                // 遍历录音文件
                File[] files = recordingsDir.listFiles((dir, name) -> 
                    name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".wav")
                );

                if (files != null) {
                    for (File file : files) {
                        try {
                            PerformanceMonitor.startOperation("Recording", "processFile_" + file.getName());
                            
                            // 验证文件
                            if (!file.exists() || !file.isFile() || !file.canRead()) {
                                throw new AppException(ErrorCode.RECORDING_ACCESS_DENIED, 
                                    "无法访问录音文件: " + file.getAbsolutePath());
                            }

                            // 获取音频文件元数据
                            retriever.setDataSource(file.getAbsolutePath());
                            String durationStr = retriever.extractMetadata(
                                MediaMetadataRetriever.METADATA_KEY_DURATION
                            );
                            long duration = durationStr != null ? Long.parseLong(durationStr) : 0;

                            // 创建录音实体
                            RecordingEntity recording = new RecordingEntity();
                            recording.setFileName(file.getName());
                            recording.setFilePath(file.getAbsolutePath());
                            recording.setFileSize(file.length());
                            recording.setCreationTime(file.lastModified());
                            recording.setDuration(duration);
                            recordings.add(recording);

                            PerformanceMonitor.endOperation("Recording", "processFile_" + file.getName());
                        } catch (Exception e) {
                            PerformanceMonitor.recordError("Recording", "processFile_" + file.getName(), e);
                            LogUtils.e(TAG, "处理文件失败: " + file.getName(), e);
                            // 继续处理下一个文件
                        }
                    }
                }

                // 更新数据库
                try {
                    PerformanceMonitor.startOperation("Recording", "updateDatabase");
                    recordingDao.deleteAll();
                    recordingDao.insertAll(recordings);
                    PerformanceMonitor.endOperation("Recording", "updateDatabase");
                } catch (Exception e) {
                    PerformanceMonitor.recordError("Recording", "updateDatabase", e);
                    throw new AppException(ErrorCode.DATABASE_ERROR, "更新数据库失败", e);
                }

                LogUtils.i(TAG, String.format("同步了%d个录音文件", recordings.size()));
                PerformanceMonitor.endOperation("Recording", "syncRecordings");
                callback.onSuccess(null);
            } catch (AppException e) {
                PerformanceMonitor.recordError("Recording", "syncRecordings", e);
                LogUtils.e(TAG, "同步录音文件失败: " + e.getMessage(), e);
                callback.onError(e);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Recording", "syncRecordings", e);
                LogUtils.e(TAG, "同步录音文件失败", e);
                callback.onError(new AppException(ErrorCode.SYSTEM_ERROR, "同步录音文件失败", e));
            }
        });
    }

    public void getRecordings(Callback<List<RecordingEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getRecordings");
        PerformanceMonitor.startOperation("Recording", "getRecordings");
        
        executorService.execute(() -> {
            try {
                List<RecordingEntity> recordings = recordingDao.getAll();
                PerformanceMonitor.endOperation("Recording", "getRecordings");
                callback.onSuccess(recordings);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Recording", "getRecordings", e);
                LogUtils.logError(TAG, "获取录音文件失败", e);
                callback.onError(e);
            }
        });
    }

    public void getRecordingsAfter(long startTime, Callback<List<RecordingEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getRecordingsAfter");
        PerformanceMonitor.startOperation("Recording", "getRecordingsAfter");
        
        executorService.execute(() -> {
            try {
                List<RecordingEntity> recordings = recordingDao.getAllAfter(startTime);
                PerformanceMonitor.endOperation("Recording", "getRecordingsAfter");
                callback.onSuccess(recordings);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Recording", "getRecordingsAfter", e);
                LogUtils.logError(TAG, "获取录音文件失败", e);
                callback.onError(e);
            }
        });
    }

    public void getRecordingsBySize(long minSize, long maxSize, Callback<List<RecordingEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getRecordingsBySize");
        PerformanceMonitor.startOperation("Recording", "getRecordingsBySize");
        
        executorService.execute(() -> {
            try {
                List<RecordingEntity> recordings = recordingDao.getAllBySize(minSize, maxSize);
                PerformanceMonitor.endOperation("Recording", "getRecordingsBySize");
                callback.onSuccess(recordings);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Recording", "getRecordingsBySize", e);
                LogUtils.logError(TAG, "获取录音文件失败", e);
                callback.onError(e);
            }
        });
    }

    public void deleteRecordings(List<String> filePaths, Callback<Void> callback) {
        LogUtils.logMethodEnter(TAG, "deleteRecordings");
        PerformanceMonitor.startOperation("Recording", "deleteRecordings");

        executorService.execute(() -> {
            try {
                for (String filePath : filePaths) {
                    try {
                        PerformanceMonitor.startOperation("Recording", "deleteFile_" + filePath);
                        // 删除文件
                        File file = new File(filePath);
                        if (file.exists()) {
                            if (file.delete()) {
                                // 删除数据库记录
                                recordingDao.deleteByPath(filePath);
                                LogUtils.i(TAG, "删除文件：" + filePath);
                            } else {
                                throw new AppException(ErrorCode.RECORDING_DELETE_FAILED, "无法删除录音文件: " + filePath);
                            }
                        }
                        PerformanceMonitor.endOperation("Recording", "deleteFile_" + filePath);
                    } catch (Exception e) {
                        PerformanceMonitor.recordError("Recording", "deleteFile_" + filePath, e);
                        LogUtils.logError(TAG, "删除文件失败: " + filePath, e);
                    }
                }
                PerformanceMonitor.endOperation("Recording", "deleteRecordings");
                callback.onSuccess(null);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Recording", "deleteRecordings", e);
                LogUtils.logError(TAG, "删除录音文件失败", e);
                callback.onError(e);
            }
        });
    }

    public List<RecordingEntity> getAllRecordings() {
        try {
            return recordingDao.getAll();
        } catch (Exception e) {
            LogUtils.e(TAG, "获取所有录音记录失败", e);
            return new ArrayList<>();
        }
    }
    
    public void deleteRecording(RecordingEntity recording) {
        try {
            // 删除数据库记录
            recordingDao.delete(recording);
            
            // 删除实际文件
            File file = new File(recording.getFilePath());
            if (file.exists() && !file.delete()) {
                throw new AppException(ErrorCode.RECORDING_DELETE_FAILED, 
                    "无法删除录音文件: " + recording.getFilePath());
            }
            
            LogUtils.i(TAG, "成功删除录音: " + recording.getFileName());
        } catch (AppException e) {
            LogUtils.e(TAG, "删除录音失败: " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "删除录音时发生错误", e);
            throw new AppException(ErrorCode.RECORDING_DELETE_FAILED, "删除录音失败", e);
        }
    }
    
    public void insertRecording(RecordingEntity recording) {
        try {
            recordingDao.insert(recording);
            LogUtils.i(TAG, "成功插入录音: " + recording.getFileName());
        } catch (Exception e) {
            LogUtils.e(TAG, "插入录音失败", e);
            throw new AppException(ErrorCode.DATABASE_ERROR, "插入录音失败", e);
        }
    }
    
    public void updateRecording(RecordingEntity recording) {
        try {
            recordingDao.update(recording);
            LogUtils.i(TAG, "成功更新录音: " + recording.getFileName());
        } catch (Exception e) {
            LogUtils.e(TAG, "更新录音失败", e);
            throw new AppException(ErrorCode.DATABASE_ERROR, "更新录音失败", e);
        }
    }

    public void deleteRecordingByPath(String filePath) {
        LogUtils.logMethodEnter(TAG, "deleteRecordingByPath");
        try {
            recordingDao.deleteByPath(filePath);
            LogUtils.i(TAG, "成功删除数据库记录: " + filePath);
        } catch (Exception e) {
            LogUtils.e(TAG, "删除数据库记录失败: " + filePath, e);
            throw new AppException(ErrorCode.DATABASE_ERROR, "删除数据库记录失败", e);
        }
        LogUtils.logMethodExit(TAG, "deleteRecordingByPath");
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
} 