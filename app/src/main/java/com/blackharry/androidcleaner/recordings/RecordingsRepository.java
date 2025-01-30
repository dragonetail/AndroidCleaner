package com.blackharry.androidcleaner.recordings;

import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecordingsRepository {
    public List<File> loadRecordings() {
        List<File> recordings = new ArrayList<>();
        File recordingsDir = new File(Environment.getExternalStorageDirectory(), "Recordings");
        if (recordingsDir.exists() && recordingsDir.isDirectory()) {
            File[] files = recordingsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && isAudioFile(file)) {
                        recordings.add(file);
                    }
                }
            }
        }
        return recordings;
    }

    private boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || 
               name.endsWith(".wav") || 
               name.endsWith(".m4a") || 
               name.endsWith(".aac");
    }
} 