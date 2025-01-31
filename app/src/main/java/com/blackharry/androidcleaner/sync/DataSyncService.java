package com.blackharry.androidcleaner.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.blackharry.androidcleaner.AppDatabase;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataSyncService extends Service {
    private static final String TAG = "DataSyncService";
    private ExecutorService executorService;
    private DataSyncManager dataSyncManager;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i(TAG, "数据同步服务创建");
        executorService = Executors.newSingleThreadExecutor();
        dataSyncManager = new DataSyncManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(TAG, "开始数据同步");
        executorService.execute(() -> {
            try {
                dataSyncManager.syncAll();
            } catch (Exception e) {
                LogUtils.logError(TAG, "数据同步失败", e);
            }
        });
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i(TAG, "数据同步服务销毁");
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
} 