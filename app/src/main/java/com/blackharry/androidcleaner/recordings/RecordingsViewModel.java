package com.blackharry.androidcleaner.recordings;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import com.blackharry.androidcleaner.utils.LogUtils;
import java.util.List;

public class RecordingsViewModel extends AndroidViewModel {
    private static final String TAG = "RecordingsViewModel";
    private final RecordingsRepository repository;
    private final MutableLiveData<List<RecordingEntity>> recordings;
    private final MutableLiveData<String> error;
    private String currentSearchQuery = "";

    public RecordingsViewModel(Application application) {
        super(application);
        LogUtils.logMethodEnter(TAG, "构造函数");
        repository = new RecordingsRepository(application);
        recordings = new MutableLiveData<>();
        error = new MutableLiveData<>();
        LogUtils.logMethodExit(TAG, "构造函数");
    }

    public LiveData<List<RecordingEntity>> getRecordings() {
        return recordings;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadRecordings(Runnable onComplete) {
        try {
            LogUtils.logMethodEnter(TAG, "loadRecordings");
            long startTime = System.currentTimeMillis();

            repository.loadRecordings(
                result -> {
                    LogUtils.d(TAG, String.format("加载到%d个录音文件", result.size()));
                    recordings.postValue(result);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    LogUtils.logPerformance(TAG, "加载录音列表", startTime);
                },
                errorMessage -> {
                    LogUtils.e(TAG, "加载录音列表失败", new Exception(errorMessage));
                    error.postValue(errorMessage);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            );

            LogUtils.logMethodExit(TAG, "loadRecordings");
        } catch (Exception e) {
            LogUtils.logError(TAG, "加载录音列表时发生异常", e);
            error.postValue("加载录音列表失败：" + e.getMessage());
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    public void setSearchQuery(String query) {
        try {
            LogUtils.logMethodEnter(TAG, "setSearchQuery");
            LogUtils.d(TAG, String.format("设置搜索关键词: %s", query));
            
            if (!query.equals(currentSearchQuery)) {
                currentSearchQuery = query;
                repository.searchRecordings(
                    query,
                    result -> {
                        LogUtils.d(TAG, String.format("搜索到%d个录音文件", result.size()));
                        recordings.postValue(result);
                    },
                    errorMessage -> {
                        LogUtils.e(TAG, "搜索录音文件失败", new Exception(errorMessage));
                        error.postValue(errorMessage);
                    }
                );
            }
            
            LogUtils.logMethodExit(TAG, "setSearchQuery");
        } catch (Exception e) {
            LogUtils.logError(TAG, "搜索录音文件时发生异常", e);
            error.postValue("搜索录音文件失败：" + e.getMessage());
        }
    }

    public void refreshRecordings() {
        try {
            LogUtils.logMethodEnter(TAG, "refreshRecordings");
            long startTime = System.currentTimeMillis();

            // 刷新录音文件列表
            repository.refreshRecordings(getApplication().getExternalFilesDir(null).getAbsolutePath());
            
            // 重新加载录音列表
            loadRecordings(() -> {
                LogUtils.logPerformance(TAG, "刷新录音列表", startTime);
            });

            LogUtils.logMethodExit(TAG, "refreshRecordings");
        } catch (Exception e) {
            LogUtils.logError(TAG, "刷新录音列表时发生异常", e);
            error.postValue("刷新录音列表失败：" + e.getMessage());
        }
    }

    @Override
    protected void onCleared() {
        try {
            LogUtils.logMethodEnter(TAG, "onCleared");
            super.onCleared();
            repository.cleanup();
            LogUtils.logMethodExit(TAG, "onCleared");
        } catch (Exception e) {
            LogUtils.logError(TAG, "清理资源时发生异常", e);
        }
    }
}