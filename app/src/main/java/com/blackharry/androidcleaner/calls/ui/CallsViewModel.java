package com.blackharry.androidcleaner.calls.ui;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.blackharry.androidcleaner.calls.data.CallEntity;
import com.blackharry.androidcleaner.calls.data.CallRepository;
import com.blackharry.androidcleaner.common.exception.AppException;

public class CallsViewModel extends AndroidViewModel {
    private static final String TAG = "CallsViewModel";
    private final CallRepository repository;
    private final ExecutorService executorService;
    private final MutableLiveData<List<CallEntity>> calls = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public CallsViewModel(Application application) {
        super(application);
        LogUtils.logMethodEnter(TAG, "CallsViewModel");
        repository = CallRepository.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
        loadCalls();
    }

    public LiveData<List<CallEntity>> getAllCalls() {
        LogUtils.logMethodEnter(TAG, "获取所有通话记录");
        return repository.getAllCalls();
    }

    public LiveData<List<CallEntity>> getCallsByType(int type) {
        LogUtils.logMethodEnter(TAG, "获取指定类型的通话记录");
        return repository.getCallsByType(type);
    }

    public LiveData<List<CallEntity>> getCallsByNumber(String number) {
        LogUtils.logMethodEnter(TAG, "获取指定号码的通话记录");
        return repository.getCallsByNumber(number);
    }

    public void getCallsWithRecordings() {
        LogUtils.logMethodEnter(TAG, "获取有录音的通话记录");
        isLoading.setValue(true);
        
        repository.getCallsWithRecordings(new CallRepository.Callback<List<CallEntity>>() {
            @Override
            public void onSuccess(List<CallEntity> result) {
                calls.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "获取通话记录失败";
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void syncCallLogs() {
        LogUtils.logMethodEnter(TAG, "同步通话记录");
        isLoading.setValue(true);
        error.setValue(null);

        repository.syncCallLogs(new CallRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                error.postValue(e.getMessage());
            }
        });
    }

    public void loadCalls() {
        LogUtils.logMethodEnter(TAG, "loadCalls");
        isLoading.setValue(true);
        
        repository.getCalls(new CallRepository.Callback<List<CallEntity>>() {
            @Override
            public void onSuccess(List<CallEntity> result) {
                calls.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "获取通话记录失败";
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }

    public void loadCallsAfter(long startTime) {
        LogUtils.logMethodEnter(TAG, "loadCallsAfter");
        isLoading.setValue(true);
        
        repository.getCallsAfter(startTime, new CallRepository.Callback<List<CallEntity>>() {
            @Override
            public void onSuccess(List<CallEntity> result) {
                calls.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "获取通话记录失败";
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }

    public LiveData<List<CallEntity>> getCalls() {
        return calls;
    }

    public void refreshCalls() {
        LogUtils.logMethodEnter(TAG, "refreshCalls");
        loadCalls();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        LogUtils.i(TAG, "ViewModel销毁");
        executorService.shutdown();
    }
} 