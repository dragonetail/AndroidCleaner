package com.blackharry.androidcleaner.calls.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import com.blackharry.androidcleaner.AppDatabase;
import com.blackharry.androidcleaner.calls.CallExceptionHandler;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.blackharry.androidcleaner.common.utils.PerformanceMonitor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class CallRepository {
    private static final String TAG = "CallRepository";
    private final Context context;
    private final CallDao callDao;
    private final ExecutorService executorService;
    private static volatile CallRepository instance;

    private CallRepository(Context context) {
        this.context = context.getApplicationContext();
        this.callDao = AppDatabase.getInstance(context).callDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static CallRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (CallRepository.class) {
                if (instance == null) {
                    instance = new CallRepository(context);
                }
            }
        }
        return instance;
    }

    public void syncCallLogs(Callback<Void> callback) {
        LogUtils.logMethodEnter(TAG, "syncCallLogs");
        PerformanceMonitor.startOperation("Call", "syncCallLogs");

        executorService.execute(() -> {
            try {
                // 检查权限
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) 
                        != PackageManager.PERMISSION_GRANTED) {
                    CallExceptionHandler.handlePermissionError(Manifest.permission.READ_CALL_LOG);
                }

                // 查询通话记录
                String[] projection = new String[] {
                    CallLog.Calls._ID,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.TYPE
                };

                List<CallEntity> calls = new ArrayList<>();
                try (Cursor cursor = context.getContentResolver().query(
                        CallLog.Calls.CONTENT_URI,
                        projection,
                        null,
                        null,
                        CallLog.Calls.DATE + " DESC")) {
                    
                    CallExceptionHandler.validateCallLogCursor(cursor);
                    
                    while (cursor.moveToNext()) {
                        try {
                            PerformanceMonitor.startOperation("Call", "processCallLog");
                            
                            CallEntity call = new CallEntity();
                            call.setNumber(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
                            call.setName(cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
                            call.setDate(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
                            call.setDuration(cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION)));
                            call.setType(cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)));
                            calls.add(call);
                            
                            PerformanceMonitor.endOperation("Call", "processCallLog");
                        } catch (Exception e) {
                            PerformanceMonitor.recordError("Call", "processCallLog", e);
                            LogUtils.logError(TAG, "处理通话记录失败", e);
                        }
                    }
                }

                // 更新数据库
                try {
                    PerformanceMonitor.startOperation("Call", "updateDatabase");
                    callDao.deleteAll();
                    callDao.insertAll(calls);
                    PerformanceMonitor.endOperation("Call", "updateDatabase");
                } catch (Exception e) {
                    PerformanceMonitor.recordError("Call", "updateDatabase", e);
                    CallExceptionHandler.handleDatabaseError(e);
                }

                LogUtils.logPerformance(TAG, String.format("同步了%d条通话记录", calls.size()));
                PerformanceMonitor.endOperation("Call", "syncCallLogs");
                callback.onSuccess(null);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Call", "syncCallLogs", e);
                LogUtils.logError(TAG, "同步通话记录失败", e);
                callback.onError(e);
            }
        });
    }

    public void getCalls(Callback<List<CallEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getCalls");
        PerformanceMonitor.startOperation("Call", "getCalls");
        
        executorService.execute(() -> {
            try {
                List<CallEntity> calls = callDao.getAll();
                PerformanceMonitor.endOperation("Call", "getCalls");
                callback.onSuccess(calls);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Call", "getCalls", e);
                LogUtils.logError(TAG, "获取通话记录失败", e);
                callback.onError(e);
            }
        });
    }

    public void getCallsAfter(long startTime, Callback<List<CallEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getCallsAfter");
        PerformanceMonitor.startOperation("Call", "getCallsAfter");
        
        executorService.execute(() -> {
            try {
                List<CallEntity> calls = callDao.getAllAfter(startTime);
                PerformanceMonitor.endOperation("Call", "getCallsAfter");
                callback.onSuccess(calls);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Call", "getCallsAfter", e);
                LogUtils.logError(TAG, "获取通话记录失败", e);
                callback.onError(e);
            }
        });
    }

    public void getCallsByType(int callType, Callback<List<CallEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getCallsByType");
        PerformanceMonitor.startOperation("Call", "getCallsByType");
        
        executorService.execute(() -> {
            try {
                List<CallEntity> calls = callDao.getAllByType(callType);
                PerformanceMonitor.endOperation("Call", "getCallsByType");
                callback.onSuccess(calls);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Call", "getCallsByType", e);
                LogUtils.logError(TAG, "获取通话记录失败", e);
                callback.onError(e);
            }
        });
    }

    public void getCallsByNumber(String phoneNumber, Callback<List<CallEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getCallsByNumber");
        PerformanceMonitor.startOperation("Call", "getCallsByNumber");
        
        executorService.execute(() -> {
            try {
                List<CallEntity> calls = callDao.getAllByNumber("%" + phoneNumber + "%");
                PerformanceMonitor.endOperation("Call", "getCallsByNumber");
                callback.onSuccess(calls);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Call", "getCallsByNumber", e);
                LogUtils.logError(TAG, "获取通话记录失败", e);
                callback.onError(e);
            }
        });
    }

    public void getCallsWithRecordings(Callback<List<CallEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getCallsWithRecordings");
        PerformanceMonitor.startOperation("Call", "getCallsWithRecordings");
        
        executorService.execute(() -> {
            try {
                List<CallEntity> calls = callDao.getAllWithRecordings();
                PerformanceMonitor.endOperation("Call", "getCallsWithRecordings");
                callback.onSuccess(calls);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Call", "getCallsWithRecordings", e);
                LogUtils.logError(TAG, "获取通话记录失败", e);
                callback.onError(e);
            }
        });
    }

    public LiveData<List<CallEntity>> getAllCalls() {
        return callDao.getAllCalls();
    }

    public void insert(CallEntity call) {
        executorService.execute(() -> {
            long startTime = System.currentTimeMillis();
            callDao.insert(call);
            LogUtils.logPerformance(TAG, "插入通话记录", startTime);
        });
    }

    public void update(CallEntity call) {
        executorService.execute(() -> {
            long startTime = System.currentTimeMillis();
            callDao.update(call);
            LogUtils.logPerformance(TAG, "更新通话记录", startTime);
        });
    }

    public void delete(CallEntity call) {
        executorService.execute(() -> {
            long startTime = System.currentTimeMillis();
            callDao.delete(call);
            LogUtils.logPerformance(TAG, "删除通话记录", startTime);
        });
    }

    public LiveData<List<CallEntity>> getCallsByType(int type) {
        return callDao.getCallsByType(type);
    }

    public LiveData<List<CallEntity>> getCallsByNumber(String number) {
        return callDao.getCallsByNumber(number);
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
} 