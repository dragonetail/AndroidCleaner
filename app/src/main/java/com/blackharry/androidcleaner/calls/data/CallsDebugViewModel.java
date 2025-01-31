package com.blackharry.androidcleaner.calls.data;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.blackharry.androidcleaner.recordings.data.AppDatabase;
import com.blackharry.androidcleaner.utils.LogUtils;
import java.util.List;

public class CallsDebugViewModel extends AndroidViewModel {
    private static final String TAG = "CallsDebugViewModel";
    private final CallDao callDao;
    private final LiveData<List<CallEntity>> allCalls;

    public CallsDebugViewModel(Application application) {
        super(application);
        try {
            LogUtils.logMethodEnter(TAG, "构造函数");
            long startTime = System.currentTimeMillis();
            
            LogUtils.d(TAG, "初始化数据库访问");
            AppDatabase db = AppDatabase.getDatabase(application);
            callDao = db.callDao();
            allCalls = callDao.getAllCalls();
            
            LogUtils.logPerformance(TAG, "ViewModel初始化", startTime);
            LogUtils.logMethodExit(TAG, "构造函数");
        } catch (Exception e) {
            LogUtils.logError(TAG, "初始化失败", e);
            throw e;
        }
    }

    public LiveData<List<CallEntity>> getAllCalls() {
        try {
            LogUtils.logMethodEnter(TAG, "getAllCalls");
            LogUtils.d(TAG, "获取所有通话记录");
            return allCalls;
        } finally {
            LogUtils.logMethodExit(TAG, "getAllCalls");
        }
    }
} 