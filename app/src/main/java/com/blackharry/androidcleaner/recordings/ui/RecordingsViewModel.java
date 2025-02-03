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
import java.util.concurrent.ExecutorService;
import com.blackharry.androidcleaner.AppDatabase;

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
        YEAR_AGO,   // 一年以前
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

    private final ExecutorService executorService;

    public enum SortOption {
        DATE_ASC,
        DATE_DESC,
        DURATION_ASC,
        DURATION_DESC,
        SIZE_ASC,
        SIZE_DESC
    }

    private final MutableLiveData<SortOption> currentSortOption = new MutableLiveData<>(SortOption.DATE_DESC);
    private final MutableLiveData<Long> minDuration = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> maxDuration = new MutableLiveData<>(Long.MAX_VALUE);
    private final MutableLiveData<Long> startDate = new MutableLiveData<>(0L);
    private final MutableLiveData<Long> endDate = new MutableLiveData<>(Long.MAX_VALUE);

    private LiveData<List<RecordingEntity>> filteredRecordings;

    public RecordingsViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        LogUtils.logMethodEnter(TAG, "RecordingsViewModel");
        this.savedStateHandle = savedStateHandle;
        repository = RecordingRepository.getInstance(application);
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        executorService = Executors.newSingleThreadExecutor();
        loadRecordings(true);

        // 组合所有过滤条件
        filteredRecordings = Transformations.switchMap(currentSortOption, sortOption ->
            Transformations.switchMap(minDuration, min ->
                Transformations.switchMap(maxDuration, max ->
                    Transformations.switchMap(startDate, start ->
                        Transformations.switchMap(endDate, end -> {
                            AppDatabase db = AppDatabase.getInstance(getApplication());
                            return db.recordingDao().getFilteredRecordings(
                                start,
                                end,
                                min,
                                max,
                                sortOption.toString()
                            );
                        })
                    )
                )
            )
        );
    }

    public LiveData<List<RecordingEntity>> getRecordings() {
        return filteredRecordings;
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
        LogUtils.logMethodEnter(TAG, "loadRecordings");
        if (isLoading.getValue()) {
            return;
        }
        isLoading.setValue(true);
        error.setValue(null);

        executorService.execute(() -> {
            try {
                // 计算时间过滤器的时间戳
                long currentTime = System.currentTimeMillis();
                long todayStart = currentTime - (currentTime % (24 * 60 * 60 * 1000));
                long weekStart = currentTime - (7 * 24 * 60 * 60 * 1000);
                long monthStart = currentTime - (30L * 24 * 60 * 60 * 1000);
                long quarterStart = currentTime - (90L * 24 * 60 * 60 * 1000);
                long yearStart = currentTime - (365L * 24 * 60 * 60 * 1000);

                // 获取当前过滤器和排序设置
                TimeFilter timeFilter = currentTimeFilter.getValue();
                DurationFilter durationFilter = currentDurationFilter.getValue();
                SortOrder sortOrder = currentSortOrder.getValue();

                // 使用新的查询方法获取过滤和排序后的录音列表
                List<RecordingEntity> filteredRecordings = repository.getRecordingDao()
                    .getFilteredAndSorted(
                        timeFilter.name(),
                        todayStart,
                        weekStart,
                        monthStart,
                        quarterStart,
                        yearStart,
                        durationFilter.name(),
                        sortOrder.name()
                    );

                // 更新UI
                recordings.postValue(filteredRecordings);
                isLoading.postValue(false);
            } catch (Exception e) {
                LogUtils.logError(TAG, "加载录音文件失败", e);
                error.postValue("加载录音文件失败：" + e.getMessage());
                isLoading.postValue(false);
            }
        });
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
        executorService.shutdown();
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

    public void loadTestData() {
        LogUtils.logMethodEnter(TAG, "loadTestData");
        if (isLoading.getValue()) {
            return;
        }
        isLoading.setValue(true);
        error.setValue(null);

        repository.loadTestData(new RecordingRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                LogUtils.i(TAG, "测试数据加载成功");
                loadRecordings(true);
            }

            @Override
            public void onError(Exception e) {
                LogUtils.logError(TAG, "测试数据加载失败", e);
                error.postValue("测试数据加载失败：" + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void setSortOption(SortOption option) {
        LogUtils.i(TAG, "设置排序选项: " + option);
        currentSortOption.setValue(option);
    }

    public void setDurationFilter(long min, long max) {
        LogUtils.i(TAG, String.format("设置时长过滤: %d - %d", min, max));
        minDuration.setValue(min);
        maxDuration.setValue(max);
    }

    public void setDateFilter(long start, long end) {
        LogUtils.i(TAG, String.format("设置日期过滤: %d - %d", start, end));
        startDate.setValue(start);
        endDate.setValue(end);
    }

    public void clearFilters() {
        LogUtils.i(TAG, "清除所有过滤条件");
        minDuration.setValue(0L);
        maxDuration.setValue(Long.MAX_VALUE);
        startDate.setValue(0L);
        endDate.setValue(Long.MAX_VALUE);
        currentSortOption.setValue(SortOption.DATE_DESC);
    }

    public void checkAndLoadInitialData() {
        LogUtils.logMethodEnter(TAG, "checkAndLoadInitialData");
        
        repository.getRecordings(new RecordingRepository.Callback<List<RecordingEntity>>() {
            @Override
            public void onSuccess(List<RecordingEntity> result) {
                if (result.isEmpty()) {
                    // 如果没有数据，加载测试数据
                    loadTestData();
                } else {
                    // 如果有数据，直接加载现有数据
                    loadRecordings(true);
                }
            }

            @Override
            public void onError(Exception e) {
                LogUtils.e(TAG, "检查初始数据失败", e);
                // 发生错误时，尝试加载测试数据
                loadTestData();
            }
        });
    }
}