package com.blackharry.androidcleaner.recordings.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recordings")
public class RecordingEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String fileName;
    private String filePath;
    private long fileSize;
    private long creationTime;
    private long duration;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
} 