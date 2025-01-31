package com.blackharry.androidcleaner.common.utils;

import android.os.SystemClock;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class PerformanceMonitor {
    private static final String TAG = "PerformanceMonitor";
    private static final Map<String, Long> startTimes = new HashMap<>();
    private static final Map<String, Long> totalTimes = new HashMap<>();
    private static final Map<String, Integer> callCounts = new HashMap<>();

    public static void start(String tag) {
        startTimes.put(tag, SystemClock.elapsedRealtime());
    }

    public static void startOperation(String module, String operation) {
        start(module + ":" + operation);
    }

    public static void end(String tag) {
        Long startTime = startTimes.remove(tag);
        if (startTime == null) {
            LogUtils.w("性能监控：未找到开始时间，tag=" + tag);
            return;
        }

        long duration = SystemClock.elapsedRealtime() - startTime;
        totalTimes.merge(tag, duration, Long::sum);
        callCounts.merge(tag, 1, Integer::sum);

        LogUtils.d("Performance", String.format("性能监控：%s 耗时 %d ms", tag, duration));
    }

    public static void endOperation(String module, String operation) {
        end(module + ":" + operation);
    }

    public static void recordError(String module, String operation, Exception e) {
        String tag = module + ":" + operation;
        LogUtils.e(TAG, String.format("操作出错: %s", tag), e);
    }

    public static void reset() {
        startTimes.clear();
        totalTimes.clear();
        callCounts.clear();
    }

    public static Map<String, PerformanceStats> getStats() {
        Map<String, PerformanceStats> stats = new HashMap<>();
        for (Map.Entry<String, Long> entry : totalTimes.entrySet()) {
            String tag = entry.getKey();
            long totalTime = entry.getValue();
            int count = callCounts.getOrDefault(tag, 0);
            stats.put(tag, new PerformanceStats(totalTime, count));
        }
        return stats;
    }

    public static class PerformanceStats {
        private final long totalTime;
        private final int count;

        public PerformanceStats(long totalTime, int count) {
            this.totalTime = totalTime;
            this.count = count;
        }

        public long getTotalTime() {
            return totalTime;
        }

        public int getCount() {
            return count;
        }

        public double getAverageTime() {
            return count > 0 ? (double) totalTime / count : 0;
        }
    }
} 