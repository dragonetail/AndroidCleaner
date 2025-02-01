package com.blackharry.androidcleaner.recordings.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY creationTime DESC")
    List<RecordingEntity> getAll();

    @Query("SELECT * FROM recordings WHERE creationTime >= :startTime ORDER BY creationTime DESC")
    List<RecordingEntity> getAllAfter(long startTime);

    @Query("SELECT * FROM recordings WHERE fileSize >= :minSize AND fileSize <= :maxSize ORDER BY fileSize DESC")
    List<RecordingEntity> getAllBySize(long minSize, long maxSize);

    @Query("SELECT COUNT(*) FROM recordings")
    int getRecordingCount();

    @Query("SELECT SUM(fileSize) FROM recordings")
    long getTotalRecordingSize();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RecordingEntity recording);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RecordingEntity> recordings);

    @Update
    void update(RecordingEntity recording);

    @Delete
    void delete(RecordingEntity recording);

    @Query("DELETE FROM recordings WHERE filePath = :filePath")
    void deleteByPath(String filePath);

    @Query("DELETE FROM recordings")
    void deleteAll();
}