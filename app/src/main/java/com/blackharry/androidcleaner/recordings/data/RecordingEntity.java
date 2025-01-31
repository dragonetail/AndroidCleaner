package com.blackharry.androidcleaner.recordings.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "recordings")
public class RecordingEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "file_name")
    private String fileName;

    @ColumnInfo(name = "file_path")
    private String filePath;

    @ColumnInfo(name = "file_size")
    private long fileSize;

    @ColumnInfo(name = "created_time")
    private long createdTime;

    @ColumnInfo(name = "duration")
    private long duration;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
} 