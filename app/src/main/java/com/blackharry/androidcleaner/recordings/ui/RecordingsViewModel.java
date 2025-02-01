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
    // 全局静态变量，用于跟踪当前正在播放的ViewModel实例
    private static RecordingsViewModel currentPlayingViewModel;
    
    private final RecordingRepository repository;
    private final ScheduledExecutorService scheduledExecutor;
    private final MutableLiveData<List<RecordingEntity>> recordings = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<PlaybackState> playbackState = new MutableLiveData<>();
    
    private MediaPlayer mediaPlayer;
    private ScheduledFuture<?> progressUpdateTask;
    private String currentPlayingFilePath;

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
        if (currentPlayingFilePath != null && filePaths.contains(currentPlayingFilePath)) {
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
            // 检查文件是否存在
            if (!new java.io.File(filePath).exists()) {
                LogUtils.e(TAG, "录音文件不存在: " + filePath);
                error.postValue("录音文件不存在");
                stopPlayback(); // 确保停止当前播放
                return;
            }

            // 如果是同一个文件，且正在播放，则暂停
            if (filePath.equals(currentPlayingFilePath) && mediaPlayer != null && mediaPlayer.isPlaying()) {
                pausePlayback();
                return;
            }

            // 如果有其他ViewModel正在播放，先停止它
            if (currentPlayingViewModel != null && currentPlayingViewModel != this) {
                currentPlayingViewModel.stopPlayback();
            }
            
            // 如果当前有播放，先停止
            if (mediaPlayer != null) {
                stopPlayback();
            }

            // 开始新的播放
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            
            // 更新全局状态
            currentPlayingFilePath = filePath;
            currentPlayingViewModel = this;

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

            // 设置播放完成监听器
            mediaPlayer.setOnCompletionListener(mp -> {
                stopPlayback();
            });

            // 设置错误监听器
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                LogUtils.e(TAG, String.format("播放错误: what=%d, extra=%d", what, extra));
                error.postValue("播放出错，请重试");
                stopPlayback();
                return true;
            });

        } catch (IOException e) {
            LogUtils.e(TAG, "播放录音失败", e);
            error.postValue("无法访问录音文件，请检查存储权限");
            stopPlayback();
        } catch (Exception e) {
            LogUtils.e(TAG, "播放录音失败", e);
            error.postValue("播放录音失败：" + e.getMessage());
            stopPlayback();
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
            stopPlayback();
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
            stopPlayback();
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

            currentPlayingFilePath = null;
            if (currentPlayingViewModel == this) {
                currentPlayingViewModel = null;
            }
            playbackState.postValue(null);
        } catch (Exception e) {
            LogUtils.e(TAG, "停止播放失败", e);
            error.postValue("停止播放失败：" + e.getMessage());
            // 即使停止失败，也要确保资源被释放
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                if (progressUpdateTask != null) {
                    progressUpdateTask.cancel(true);
                    progressUpdateTask = null;
                }
                currentPlayingFilePath = null;
                if (currentPlayingViewModel == this) {
                    currentPlayingViewModel = null;
                }
                playbackState.postValue(null);
            } catch (Exception ex) {
                LogUtils.e(TAG, "清理播放资源失败", ex);
            }
        }
    }

    private void updatePlaybackState() {
        if (mediaPlayer != null && currentPlayingFilePath != null) {
            playbackState.postValue(new PlaybackState(
                currentPlayingFilePath,
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
            stopPlayback();
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