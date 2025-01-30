package com.blackharry.androidcleaner.recordings;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import java.io.File;
import java.util.List;

public class RecordingsViewModel extends AndroidViewModel {
    private final RecordingsRepository repository;
    private final MutableLiveData<List<File>> recordings = new MutableLiveData<>();

    public RecordingsViewModel(Application application) {
        super(application);
        repository = new RecordingsRepository();
    }

    public LiveData<List<File>> getRecordings() {
        return recordings;
    }

    public void loadRecordings() {
        recordings.setValue(repository.loadRecordings());
    }
} 