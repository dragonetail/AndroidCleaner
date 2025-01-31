package com.blackharry.androidcleaner.recordings.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface RecordingDao {
    @Query("SELECT * FROM recordings")
    LiveData<List<RecordingEntity>> getAllRecordings();

    @Query("SELECT * FROM recordings WHERE file_name LIKE :query")
    LiveData<List<RecordingEntity>> searchRecordings(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RecordingEntity> recordings);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecordingEntity recording);

    @Delete
    void delete(RecordingEntity recording);

    @Query("DELETE FROM recordings")
    void deleteAll();

    @Query("SELECT * FROM recordings ORDER BY " +
           "CASE WHEN :asc = 1 THEN file_name END ASC, " +
           "CASE WHEN :asc = 0 THEN file_name END DESC")
    LiveData<List<RecordingEntity>> getAllRecordingsOrderByName(boolean asc);

    @Query("SELECT * FROM recordings ORDER BY " +
           "CASE WHEN :asc = 1 THEN created_time END ASC, " +
           "CASE WHEN :asc = 0 THEN created_time END DESC")
    LiveData<List<RecordingEntity>> getAllRecordingsOrderByDate(boolean asc);

    @Query("SELECT * FROM recordings ORDER BY " +
           "CASE WHEN :asc = 1 THEN file_size END ASC, " +
           "CASE WHEN :asc = 0 THEN file_size END DESC")
    LiveData<List<RecordingEntity>> getAllRecordingsOrderBySize(boolean asc);

    @Query("SELECT * FROM recordings")
    List<RecordingEntity> getAllRecordingsSync();

    @Query("SELECT COUNT(*) FROM recordings")
    int getCount();

    @Query("SELECT SUM(file_size) FROM recordings")
    long getTotalSize();

    @Query("SELECT SUM(duration) FROM recordings")
    long getTotalDuration();
} 