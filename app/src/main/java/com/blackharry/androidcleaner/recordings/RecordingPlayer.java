package com.blackharry.androidcleaner.recordings;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import com.blackharry.androidcleaner.utils.LogUtils;
import java.io.IOException;

public class RecordingPlayer {
    private static final String TAG = "RecordingPlayer";
    private MediaPlayer mediaPlayer;
    private final Handler handler;
    private Runnable progressCallback;
    private boolean isPlaying = false;

    public RecordingPlayer() {
        LogUtils.logMethodEnter(TAG, "构造函数");
        handler = new Handler(Looper.getMainLooper());
        LogUtils.logMethodExit(TAG, "构造函数");
    }

    public void play(Context context, String filePath) {
        try {
            LogUtils.logMethodEnter(TAG, "play");
            LogUtils.d(TAG, String.format("开始播放录音: %s", filePath));
            long startTime = System.currentTimeMillis();

            if (mediaPlayer != null) {
                LogUtils.d(TAG, "停止当前播放");
                stop();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setOnPreparedListener(mp -> {
                LogUtils.d(TAG, "录音准备完成，开始播放");
                mp.start();
                isPlaying = true;
                startProgressUpdate();
                LogUtils.logPerformance(TAG, "准备播放", startTime);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                LogUtils.d(TAG, "录音播放完成");
                isPlaying = false;
                stopProgressUpdate();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                LogUtils.e(TAG, String.format("播放错误: what=%d, extra=%d", what, extra), 
                    new Exception("MediaPlayer error"));
                isPlaying = false;
                stopProgressUpdate();
                return false;
            });

            LogUtils.d(TAG, "准备播放录音");
            mediaPlayer.prepareAsync();
            LogUtils.logMethodExit(TAG, "play");

        } catch (IOException e) {
            LogUtils.logError(TAG, "播放录音失败", e);
            isPlaying = false;
            stopProgressUpdate();
        }
    }

    public void pause() {
        try {
            LogUtils.logMethodEnter(TAG, "pause");
            if (mediaPlayer != null && isPlaying) {
                LogUtils.d(TAG, "暂停播放");
                mediaPlayer.pause();
                isPlaying = false;
                stopProgressUpdate();
            }
            LogUtils.logMethodExit(TAG, "pause");
        } catch (Exception e) {
            LogUtils.logError(TAG, "暂停播放失败", e);
        }
    }

    public void resume() {
        try {
            LogUtils.logMethodEnter(TAG, "resume");
            if (mediaPlayer != null && !isPlaying) {
                LogUtils.d(TAG, "恢复播放");
                mediaPlayer.start();
                isPlaying = true;
                startProgressUpdate();
            }
            LogUtils.logMethodExit(TAG, "resume");
        } catch (Exception e) {
            LogUtils.logError(TAG, "恢复播放失败", e);
        }
    }

    public void stop() {
        try {
            LogUtils.logMethodEnter(TAG, "stop");
            if (mediaPlayer != null) {
                LogUtils.d(TAG, "停止播放");
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                isPlaying = false;
                stopProgressUpdate();
            }
            LogUtils.logMethodExit(TAG, "stop");
        } catch (Exception e) {
            LogUtils.logError(TAG, "停止播放失败", e);
        }
    }

    public void setProgressCallback(Runnable callback) {
        this.progressCallback = callback;
    }

    private void startProgressUpdate() {
        try {
            LogUtils.logMethodEnter(TAG, "startProgressUpdate");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null && isPlaying && progressCallback != null) {
                        progressCallback.run();
                        handler.postDelayed(this, 100);
                    }
                }
            });
            LogUtils.logMethodExit(TAG, "startProgressUpdate");
        } catch (Exception e) {
            LogUtils.logError(TAG, "启动进度更新失败", e);
        }
    }

    private void stopProgressUpdate() {
        try {
            LogUtils.logMethodEnter(TAG, "stopProgressUpdate");
            handler.removeCallbacksAndMessages(null);
            LogUtils.logMethodExit(TAG, "stopProgressUpdate");
        } catch (Exception e) {
            LogUtils.logError(TAG, "停止进度更新失败", e);
        }
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
} 