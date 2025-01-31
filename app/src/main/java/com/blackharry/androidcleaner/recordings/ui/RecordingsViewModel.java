package com.blackharry.androidcleaner.recordings.ui;

import android.app.Application;
import android.media.MediaPlayer;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.blackharry.androidcleaner.common.exception.AppException;
import com.blackharry.androidcleaner.common.exception.ErrorCode;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import com.blackharry.androidcleaner.recordings.data.RecordingRepository;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RecordingsViewModel extends AndroidViewModel {
    private static final String TAG = "RecordingsViewModel";
    private final RecordingRepository repository;
    private final ScheduledExecutorService scheduledExecutor;
    private final MutableLiveData<List<RecordingEntity>> recordings = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<PlaybackState> playbackState = new MutableLiveData<>();
    
    private MediaPlayer mediaPlayer;
    private ScheduledFuture<?> progressUpdateTask;
    private String currentPlayingFile;

    public RecordingsViewModel(Application application) {
        super(application);
        LogUtils.logMethodEnter(TAG, "RecordingsViewModel");
        repository = RecordingRepository.getInstance(application);
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        loadRecordings();
    }

    public LiveData<List<RecordingEntity>> getRecordings() {
        return recordings;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<PlaybackState> getPlaybackState() {
        return playbackState;
    }

    public void loadRecordings() {
        LogUtils.logMethodEnter(TAG, "loadRecordings");
        isLoading.setValue(true);
        
        repository.getRecordings(new RecordingRepository.Callback<List<RecordingEntity>>() {
            @Override
            public void onSuccess(List<RecordingEntity> result) {
                recordings.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "获取录音列表失败";
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }

    public void loadRecordingsAfter(long startTime) {
        LogUtils.logMethodEnter(TAG, "loadRecordingsAfter");
        isLoading.setValue(true);
        
        repository.getRecordingsAfter(startTime, new RecordingRepository.Callback<List<RecordingEntity>>() {
            @Override
            public void onSuccess(List<RecordingEntity> result) {
                recordings.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "获取录音列表失败";
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }

    public void loadRecordingsBySize(long minSize, long maxSize) {
        LogUtils.logMethodEnter(TAG, "loadRecordingsBySize");
        isLoading.setValue(true);
        
        repository.getRecordingsBySize(minSize, maxSize, new RecordingRepository.Callback<List<RecordingEntity>>() {
            @Override
            public void onSuccess(List<RecordingEntity> result) {
                recordings.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "获取录音列表失败";
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }

    public void deleteRecordings(List<String> filePaths) {
        LogUtils.logMethodEnter(TAG, "deleteRecordings");
        
        // 停止当前播放
        if (currentPlayingFile != null && filePaths.contains(currentPlayingFile)) {
            stopPlayback();
        }

        repository.deleteRecordings(filePaths, new RecordingRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadRecordings();
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "删除录音失败";
                error.postValue(errorMessage);
            }
        });
    }

    public void playRecording(String filePath) {
        LogUtils.logMethodEnter(TAG, "playRecording");
        
        try {
            if (mediaPlayer != null) {
                stopPlayback();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentPlayingFile = filePath;

            // 开始进度更新任务
            if (progressUpdateTask != null) {
                progressUpdateTask.cancel(true);
            }
            progressUpdateTask = scheduledExecutor.scheduleAtFixedRate(() -> {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    playbackState.postValue(new PlaybackState(
                        filePath,
                        mediaPlayer.getCurrentPosition(),
                        mediaPlayer.getDuration(),
                        true
                    ));
                }
            }, 0, 100, TimeUnit.MILLISECONDS);

            mediaPlayer.setOnCompletionListener(mp -> {
                stopPlayback();
            });
        } catch (IOException e) {
            LogUtils.e(TAG, "播放录音失败", e);
            error.postValue("播放录音失败：" + e.getMessage());
            throw new AppException(ErrorCode.RECORDING_ACCESS_DENIED, "无法访问录音文件", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "播放录音失败", e);
            error.postValue("播放录音失败：" + e.getMessage());
            throw new AppException(ErrorCode.SYSTEM_ERROR, "播放录音失败", e);
        }
    }

    public void pausePlayback() {
        LogUtils.logMethodEnter(TAG, "pausePlayback");
        
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                updatePlaybackState();
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "暂停播放失败", e);
            error.postValue("暂停播放失败：" + e.getMessage());
            throw new AppException(ErrorCode.SYSTEM_ERROR, "暂停播放失败", e);
        }
    }

    public void resumePlayback() {
        LogUtils.logMethodEnter(TAG, "resumePlayback");
        
        try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                updatePlaybackState();
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "恢复播放失败", e);
            error.postValue("恢复播放失败：" + e.getMessage());
            throw new AppException(ErrorCode.SYSTEM_ERROR, "恢复播放失败", e);
        }
    }

    public void stopPlayback() {
        LogUtils.logMethodEnter(TAG, "stopPlayback");
        
        try {
            if (progressUpdateTask != null) {
                progressUpdateTask.cancel(true);
                progressUpdateTask = null;
            }

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            currentPlayingFile = null;
            playbackState.postValue(null);
        } catch (Exception e) {
            LogUtils.e(TAG, "停止播放失败", e);
            error.postValue("停止播放失败：" + e.getMessage());
            throw new AppException(ErrorCode.SYSTEM_ERROR, "停止播放失败", e);
        }
    }

    private void updatePlaybackState() {
        if (mediaPlayer != null && currentPlayingFile != null) {
            playbackState.postValue(new PlaybackState(
                currentPlayingFile,
                mediaPlayer.getCurrentPosition(),
                mediaPlayer.getDuration(),
                mediaPlayer.isPlaying()
            ));
        }
    }

    public void seekTo(long position) {
        LogUtils.logMethodEnter(TAG, "seekTo");
        
        try {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo((int) position);
                updatePlaybackState();
                LogUtils.i(TAG, String.format("跳转到：%d ms", position));
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "跳转失败", e);
            error.postValue("跳转失败：" + e.getMessage());
            throw new AppException(ErrorCode.SYSTEM_ERROR, "跳转失败", e);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        LogUtils.i(TAG, "ViewModel销毁");
        stopPlayback();
        scheduledExecutor.shutdown();
    }

    public static class PlaybackState {
        public final String filePath;
        public final long currentPosition;
        public final long duration;
        public final boolean isPlaying;

        public PlaybackState(String filePath, long currentPosition, long duration, boolean isPlaying) {
            this.filePath = filePath;
            this.currentPosition = currentPosition;
            this.duration = duration;
            this.isPlaying = isPlaying;
        }
    }
}