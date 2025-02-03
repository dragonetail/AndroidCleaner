package com.blackharry.androidcleaner.common.utils;

import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class TestDataGenerator {
    private static final String TAG = "TestDataGenerator";
    private static final Random random = new Random();

    /**
     * 生成测试录音数据
     * 时间分布：
     * - 今天：20条
     * - 最近7天：30条
     * - 最近30天：30条
     * - 最近90天：20条
     * 
     * 时长分布：
     * - 1分钟以内：25条
     * - 5分钟以内：30条
     * - 30分钟以内：25条
     * - 2小时以内：15条
     * - 更长：5条
     * 
     * 文件大小分布：
     * - 小文件（<1MB）：30条
     * - 中等文件（1-10MB）：50条
     * - 大文件（>10MB）：20条
     */
    public static List<RecordingEntity> generateTestRecordings() {
        LogUtils.logMethodEnter(TAG, "generateTestRecordings");
        List<RecordingEntity> recordings = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        
        try {
            // 生成今天的数据（20条）
            generateRecordingsForTimeRange(recordings, 20, 
                currentTime - TimeUnit.HOURS.toMillis(24), currentTime);

            // 生成最近7天的数据（30条）
            generateRecordingsForTimeRange(recordings, 30,
                currentTime - TimeUnit.DAYS.toMillis(7),
                currentTime - TimeUnit.DAYS.toMillis(1));

            // 生成最近30天的数据（30条）
            generateRecordingsForTimeRange(recordings, 30,
                currentTime - TimeUnit.DAYS.toMillis(30),
                currentTime - TimeUnit.DAYS.toMillis(7));

            // 生成最近90天的数据（20条）
            generateRecordingsForTimeRange(recordings, 20,
                currentTime - TimeUnit.DAYS.toMillis(90),
                currentTime - TimeUnit.DAYS.toMillis(30));

            LogUtils.i(TAG, String.format("生成了%d条测试数据", recordings.size()));
        } catch (Exception e) {
            LogUtils.logError(TAG, "生成测试数据失败", e);
        }

        LogUtils.logMethodExit(TAG, "generateTestRecordings");
        return recordings;
    }

    private static void generateRecordingsForTimeRange(List<RecordingEntity> recordings, 
            int count, long startTime, long endTime) {
        for (int i = 0; i < count; i++) {
            RecordingEntity recording = new RecordingEntity();
            
            // 生成随机时间
            long randomTime = startTime + (long) (random.nextDouble() * (endTime - startTime));
            recording.setCreationTime(randomTime);
            
            // 生成文件名（格式：record_YYYYMMDD_HHMMSS.mp3）
            String fileName = String.format("record_%s.mp3", 
                DateTimeConverters.formatDateForFileName(randomTime));
            recording.setFileName(fileName);
            
            // 生成文件路径
            recording.setFilePath("/storage/emulated/0/Recordings/" + fileName);
            
            // 生成随机时长
            long duration;
            int durationCategory = random.nextInt(100);
            if (durationCategory < 25) {
                // 1分钟以内（25%）
                duration = random.nextInt(60) * 1000L;
            } else if (durationCategory < 55) {
                // 5分钟以内（30%）
                duration = 60_000L + random.nextInt(4 * 60) * 1000L;
            } else if (durationCategory < 80) {
                // 30分钟以内（25%）
                duration = 5 * 60_000L + random.nextInt(25 * 60) * 1000L;
            } else if (durationCategory < 95) {
                // 2小时以内（15%）
                duration = 30 * 60_000L + random.nextInt(90 * 60) * 1000L;
            } else {
                // 更长（5%）
                duration = 2 * 60 * 60_000L + random.nextInt(60 * 60) * 1000L;
            }
            recording.setDuration(duration);
            
            // 生成文件大小（基于时长，添加一些随机变化）
            // 假设平均比特率为32kbps
            long baseSize = (duration / 1000) * 32 * 1024 / 8; // 基础大小（字节）
            long randomVariation = (long) (baseSize * 0.2 * (random.nextDouble() - 0.5)); // ±10%变化
            recording.setFileSize(baseSize + randomVariation);
            
            recordings.add(recording);
        }
    }
} 