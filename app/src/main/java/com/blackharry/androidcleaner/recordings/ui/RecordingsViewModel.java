package com.blackharry.androidcleaner.recordings.ui;

import android.app.Application;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.audiofx.Visualizer;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import com.blackharry.androidcleaner.common.exception.AppException;
import com.blackharry.androidcleaner.common.exception.ErrorCode;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import com.blackharry.androidcleaner.recordings.data.RecordingRepository;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class RecordingsViewModel extends AndroidViewModel {
    private static final String TAG = "RecordingsViewModel";
    private static final int PAGE_SIZE = 20;
    private static final int PRELOAD_DISTANCE = 10;
    private static final int CAPTURE_SIZE = 1024;
    
    private static RecordingsViewModel currentPlayingViewModel;
    
    private final SavedStateHandle savedStateHandle;
    private final RecordingRepository repository;
    private final ScheduledExecutorService scheduledExecutor;
    private final MutableLiveData<List<RecordingEntity>> recordings = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<PlaybackState> playbackState = new MutableLiveData<>();
    private final MutableLiveData<float[]> waveformData = new MutableLiveData<>();
    
    private MediaPlayer mediaPlayer;
    private Visualizer visualizer;
    private ScheduledFuture<?> progressUpdateTask;
    private String currentPlayingFilePath;
    private float currentSpeed = 1.0f;
    private int currentPage = 0;
    private boolean isLastPage = false;
    private boolean isPreloading = false;

    public enum Filter {
        ALL,        // 全部录音
        DELETED,    // 已删除通话
        ORPHANED    // 孤立录音
    }

    private final MutableLiveData<Filter> currentFilter = new MutableLiveData<>(Filter.ALL);

    public enum TimeFilter {
        ALL,        // 全部时间
        TODAY,      // 今天
        WEEK,       // 最近7天
        MONTH,      // 最近30天
        QUARTER     // 最近90天
    }

    public enum DurationFilter {
        ALL,        // 全部时长
        MIN_1,      // 1分钟以内
        MIN_5,      // 5分钟以内
        MIN_30,     // 30分钟以内
        HOUR_2,     // 2小时以内
        LONGER      // 更长
    }

    public enum SortOrder {
        TIME_DESC,  // 时间降序
        TIME_ASC,   // 时间升序
        SIZE_DESC,  // 大小降序
        SIZE_ASC    // 大小升序
    }

    private final MutableLiveData<TimeFilter> currentTimeFilter = new MutableLiveData<>(TimeFilter.ALL);
    private final MutableLiveData<DurationFilter> currentDurationFilter = new MutableLiveData<>(DurationFilter.ALL);
    private final MutableLiveData<SortOrder> currentSortOrder = new MutableLiveData<>(SortOrder.TIME_DESC);

    public RecordingsViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        LogUtils.logMethodEnter(TAG, "RecordingsViewModel");
        this.savedStateHandle = savedStateHandle;
        repository = RecordingRepository.getInstance(application);
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        loadRecordings(true);
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

    public LiveData<float[]> getWaveformData() {
        return waveformData;
    }

    public void setFilter(Filter filter) {
        LogUtils.logMethodEnter(TAG, "setFilter: " + filter);
        currentFilter.setValue(filter);
        loadRecordings(true);
    }

    public TimeFilter getCurrentTimeFilter() {
        return currentTimeFilter.getValue();
    }

    public void setTimeFilter(TimeFilter filter) {
        if (currentTimeFilter.getValue() != filter) {
            currentTimeFilter.setValue(filter);
            loadRecordings(false);
        }
    }

    public DurationFilter getCurrentDurationFilter() {
        return currentDurationFilter.getValue();
    }

    public void setDurationFilter(DurationFilter filter) {
        if (currentDurationFilter.getValue() != filter) {
            currentDurationFilter.setValue(filter);
            loadRecordings(false);
        }
    }

    public SortOrder getCurrentSortOrder() {
        return currentSortOrder.getValue();
    }

    public void setSortOrder(SortOrder order) {
        if (currentSortOrder.getValue() != order) {
            currentSortOrder.setValue(order);
            loadRecordings(false);
        }
    }

    public void loadRecordings(boolean forceRefresh) {
        isLoading.setValue(true);
        repository.getRecordings(new RecordingRepository.Callback<List<RecordingEntity>>() {
            @Override
            public void onSuccess(List<RecordingEntity> result) {
                // 应用过滤和排序
                List<RecordingEntity> filteredList = applyFilters(result);
                recordings.postValue(filteredList);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    private List<RecordingEntity> applyFilters(List<RecordingEntity> recordings) {
        List<RecordingEntity> result = new ArrayList<>(recordings);
        
        // 应用时间过滤
        TimeFilter timeFilter = getCurrentTimeFilter();
        if (timeFilter != TimeFilter.ALL) {
            long currentTime = System.currentTimeMillis();
            long filterTime;
            switch (timeFilter) {
                case TODAY:
                    filterTime = currentTime - 24 * 60 * 60 * 1000;
                    break;
                case WEEK:
                    filterTime = currentTime - 7 * 24 * 60 * 60 * 1000;
                    break;
                case MONTH:
                    filterTime = currentTime - 30L * 24 * 60 * 60 * 1000;
                    break;
                case QUARTER:
                    filterTime = currentTime - 90L * 24 * 60 * 60 * 1000;
                    break;
                default:
                    filterTime = 0;
            }
            result.removeIf(recording -> recording.getCreationTime() < filterTime);
        }

        // 应用时长过滤
        DurationFilter durationFilter = getCurrentDurationFilter();
        if (durationFilter != DurationFilter.ALL) {
            long maxDuration;
            switch (durationFilter) {
                case MIN_1:
                    maxDuration = 60 * 1000;
                    break;
                case MIN_5:
                    maxDuration = 5 * 60 * 1000;
                    break;
                case MIN_30:
                    maxDuration = 30 * 60 * 1000;
                    break;
                case HOUR_2:
                    maxDuration = 2 * 60 * 60 * 1000;
                    break;
                case LONGER:
                    maxDuration = Long.MAX_VALUE;
                    break;
                default:
                    maxDuration = 0;
            }
            if (durationFilter == DurationFilter.LONGER) {
                result.removeIf(recording -> recording.getDuration() <= 2 * 60 * 60 * 1000);
            } else {
                result.removeIf(recording -> recording.getDuration() > maxDuration);
            }
        }

        // 应用排序
        SortOrder sortOrder = getCurrentSortOrder();
        switch (sortOrder) {
            case TIME_DESC:
                result.sort((a, b) -> Long.compare(b.getCreationTime(), a.getCreationTime()));
                break;
            case TIME_ASC:
                result.sort((a, b) -> Long.compare(a.getCreationTime(), b.getCreationTime()));
                break;
            case SIZE_DESC:
                result.sort((a, b) -> Long.compare(b.getFileSize(), a.getFileSize()));
                break;
            case SIZE_ASC:
                result.sort((a, b) -> Long.compare(a.getFileSize(), b.getFileSize()));
                break;
        }

        return result;
    }

    public void checkPreload(int position) {
        if (position >= recordings.getValue().size() - PRELOAD_DISTANCE && !isPreloading) {
            isPreloading = true;
            loadRecordings(false);
            isPreloading = false;
        }
    }

    public void playRecording(String filePath) {
        LogUtils.logMethodEnter(TAG, "playRecording");
        
        try {
            if (!new java.io.File(filePath).exists()) {
                LogUtils.e(TAG, "录音文件不存在: " + filePath);
                error.postValue("录音文件不存在");
                stopPlayback();
                return;
            }

            if (filePath.equals(currentPlayingFilePath) && mediaPlayer != null && mediaPlayer.isPlaying()) {
                pausePlayback();
                return;
            }

            if (currentPlayingViewModel != null && currentPlayingViewModel != this) {
                currentPlayingViewModel.stopPlayback();
            }
            currentPlayingViewModel = this;

            if (!filePath.equals(currentPlayingFilePath) || mediaPlayer == null) {
                releaseMediaPlayer();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();
                currentPlayingFilePath = filePath;
                setupVisualizer();
            }

            PlaybackParams params = new PlaybackParams();
            params.setSpeed(currentSpeed);
            mediaPlayer.setPlaybackParams(params);

            mediaPlayer.start();

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

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                LogUtils.e(TAG, String.format("播放错误: what=%d, extra=%d", what, extra));
                error.postValue("播放出错，请重试");
                stopPlayback();
                return true;
            });

        } catch (IOException e) {
            LogUtils.e(TAG, "播放录音失败", e);
            error.postValue("播放录音失败");
            stopPlayback();
        }
    }

    public void pausePlayback() {
        LogUtils.logMethodEnter(TAG, "pausePlayback");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playbackState.setValue(new PlaybackState(
                currentPlayingFilePath,
                mediaPlayer.getCurrentPosition(),
                mediaPlayer.getDuration(),
                false
            ));
        }
    }

    public void resumePlayback() {
        LogUtils.logMethodEnter(TAG, "resumePlayback");
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            playbackState.setValue(new PlaybackState(
                currentPlayingFilePath,
                mediaPlayer.getCurrentPosition(),
                mediaPlayer.getDuration(),
                true
            ));
        }
    }

    public void stopPlayback() {
        LogUtils.logMethodEnter(TAG, "stopPlayback");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            playbackState.setValue(null);
        }
        releaseVisualizer();
        if (progressUpdateTask != null) {
            progressUpdateTask.cancel(true);
            progressUpdateTask = null;
        }
        currentPlayingFilePath = null;
        if (currentPlayingViewModel == this) {
            currentPlayingViewModel = null;
        }
    }

    public void seekTo(int progress) {
        LogUtils.logMethodEnter(TAG, "seekTo");
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(progress);
            playbackState.setValue(new PlaybackState(
                currentPlayingFilePath,
                progress,
                mediaPlayer.getDuration(),
                mediaPlayer.isPlaying()
            ));
        }
    }

    public void setPlaybackSpeed(float speed) {
        LogUtils.logMethodEnter(TAG, "setPlaybackSpeed");
        if (mediaPlayer != null && (speed == 1.0f || speed == 2.0f)) {
            currentSpeed = speed;
            PlaybackParams params = new PlaybackParams();
            params.setSpeed(speed);
            mediaPlayer.setPlaybackParams(params);
        }
    }

    private void setupVisualizer() {
        LogUtils.logMethodEnter(TAG, "setupVisualizer");
        if (mediaPlayer == null) return;

        releaseVisualizer();
        
        try {
            visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
            visualizer.setCaptureSize(CAPTURE_SIZE);
            
            visualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    @Override
                    public void onWaveFormDataCapture(Visualizer visualizer, 
                            byte[] waveform, int samplingRate) {
                        float[] normalizedData = new float[waveform.length];
                        for (int i = 0; i < waveform.length; i++) {
                            normalizedData[i] = ((float) (waveform[i] & 0xFF)) / 128.0f;
                        }
                        waveformData.postValue(normalizedData);
                    }

                    @Override
                    public void onFftDataCapture(Visualizer visualizer, 
                            byte[] fft, int samplingRate) {
                        // 不需要处理FFT数据
                    }
                }, 
                Visualizer.getMaxCaptureRate() / 2, 
                true, 
                false
            );
            
            visualizer.setEnabled(true);
        } catch (Exception e) {
            LogUtils.e(TAG, "设置音频可视化失败", e);
        }
    }

    private void releaseVisualizer() {
        LogUtils.logMethodEnter(TAG, "releaseVisualizer");
        if (visualizer != null) {
            visualizer.setEnabled(false);
            visualizer.release();
            visualizer = null;
        }
    }

    private void releaseMediaPlayer() {
        LogUtils.logMethodEnter(TAG, "releaseMediaPlayer");
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        LogUtils.logMethodEnter(TAG, "onCleared");
        stopPlayback();
        releaseMediaPlayer();
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

    public RecordingRepository getRepository() {
        return repository;
    }
}