package com.blackharry.androidcleaner.utils;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class AppExecutors {
    private static final String TAG = "AppExecutors";
    private static AppExecutors instance;
    private final Executor diskIO;
    private final Executor mainThread;
    private final Executor networkIO;

    private AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread) {
        LogUtils.logMethodEnter(TAG, "构造函数");
        this.diskIO = new LoggingExecutor(diskIO, "DiskIO");
        this.networkIO = new LoggingExecutor(networkIO, "NetworkIO");
        this.mainThread = new LoggingExecutor(mainThread, "MainThread");
        LogUtils.logMethodExit(TAG, "构造函数");
    }

    public static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) {
                    LogUtils.d(TAG, "创建AppExecutors实例");
                    instance = new AppExecutors(
                        Executors.newSingleThreadExecutor(new NamedThreadFactory("DiskIO")),
                        Executors.newFixedThreadPool(3, new NamedThreadFactory("NetworkIO")),
                        new MainThreadExecutor()
                    );
                }
            }
        }
        return instance;
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    public Executor networkIO() {
        return networkIO;
    }

    private static class MainThreadExecutor implements Executor {
        private static final String TAG = "MainThreadExecutor";
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            LogUtils.d(TAG, "提交主线程任务");
            mainThreadHandler.post(() -> {
                try {
                    LogUtils.d(TAG, "开始执行主线程任务");
                    command.run();
                    LogUtils.d(TAG, "主线程任务执行完成");
                } catch (Exception e) {
                    LogUtils.logError(TAG, "主线程任务执行失败", e);
                    throw e;
                }
            });
        }
    }

    private static class LoggingExecutor implements Executor {
        private final Executor delegate;
        private final String name;

        LoggingExecutor(Executor delegate, String name) {
            this.delegate = delegate;
            this.name = name;
        }

        @Override
        public void execute(@NonNull Runnable command) {
            LogUtils.d(TAG, String.format("提交%s任务", name));
            delegate.execute(() -> {
                try {
                    LogUtils.d(TAG, String.format("开始执行%s任务", name));
                    command.run();
                    LogUtils.d(TAG, String.format("%s任务执行完成", name));
                } catch (Exception e) {
                    LogUtils.logError(TAG, String.format("%s任务执行失败", name), e);
                    throw e;
                }
            });
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        NamedThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "Pool-" + poolName + "-Thread-";
        }

        public Thread newThread(@NonNull Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            LogUtils.d(TAG, String.format("创建新线程: %s", t.getName()));
            return t;
        }
    }
} 