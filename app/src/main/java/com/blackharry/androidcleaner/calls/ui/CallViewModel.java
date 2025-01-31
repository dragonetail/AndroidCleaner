package com.blackharry.androidcleaner.calls.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.blackharry.androidcleaner.calls.data.CallEntity;
import com.blackharry.androidcleaner.calls.data.CallRepository;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import java.util.List;

public class CallViewModel extends AndroidViewModel {
    private static final String TAG = "CallViewModel";
    private final CallRepository repository;
    private final MutableLiveData<List<CallEntity>> calls = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public CallViewModel(@NonNull Application application) {
        super(application);
        LogUtils.logMethodEnter(TAG, "CallViewModel 初始化");
        repository = CallRepository.getInstance(application);
        loadCalls();
        LogUtils.logMethodExit(TAG, "CallViewModel 初始化");
    }

    private void loadCalls() {
        repository.getCallsWithRecordings(new CallRepository.Callback<List<CallEntity>>() {
            @Override
            public void onSuccess(List<CallEntity> result) {
                calls.postValue(result);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public LiveData<List<CallEntity>> getCalls() {
        return calls;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void insert(CallEntity call) {
        LogUtils.logMethodEnter(TAG, "插入通话记录");
        repository.insert(call);
        LogUtils.logMethodExit(TAG, "插入通话记录");
    }

    public void update(CallEntity call) {
        repository.update(call);
    }

    public void delete(CallEntity call) {
        repository.delete(call);
    }

    public void loadCallsByType(int type) {
        repository.getCallsByType(type);
    }

    public void loadCallsByNumber(String number) {
        repository.getCallsByNumber(number);
    }
} 