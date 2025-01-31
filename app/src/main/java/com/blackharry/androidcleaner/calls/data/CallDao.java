package com.blackharry.androidcleaner.calls.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;
import java.util.List;

@Dao
public interface CallDao {
    @Query("SELECT * FROM calls ORDER BY date DESC")
    LiveData<List<CallEntity>> getAllCalls();

    @Query("SELECT * FROM calls ORDER BY date DESC")
    List<CallEntity> getAll();

    @Query("SELECT * FROM calls WHERE type = :type ORDER BY date DESC")
    LiveData<List<CallEntity>> getCallsByType(int type);

    @Query("SELECT * FROM calls WHERE number LIKE :number ORDER BY date DESC")
    LiveData<List<CallEntity>> getCallsByNumber(String number);

    @Query("SELECT * FROM calls WHERE recording_path IS NOT NULL ORDER BY date DESC")
    LiveData<List<CallEntity>> getCallsWithRecordings();

    @Query("SELECT * FROM calls WHERE recording_path IS NOT NULL ORDER BY date DESC")
    List<CallEntity> getAllWithRecordings();

    @Query("SELECT * FROM calls WHERE date > :timestamp ORDER BY date DESC")
    LiveData<List<CallEntity>> getCallsAfter(long timestamp);

    @Query("SELECT * FROM calls WHERE date > :timestamp ORDER BY date DESC")
    List<CallEntity> getAllAfter(long timestamp);

    @Query("SELECT * FROM calls WHERE type = :type ORDER BY date DESC")
    List<CallEntity> getAllByType(int type);

    @Query("SELECT * FROM calls WHERE number LIKE :number ORDER BY date DESC")
    List<CallEntity> getAllByNumber(String number);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CallEntity call);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CallEntity> calls);

    @Update
    void update(CallEntity call);

    @Delete
    void delete(CallEntity call);

    @Query("DELETE FROM calls")
    void deleteAll();

    @Query("SELECT * FROM calls c INNER JOIN contacts ct ON c.contact_id = ct.id " +
           "WHERE ct.isSafeZone = 1 ORDER BY c.date DESC")
    List<CallEntity> getAllInSafeZone();

    @Query("SELECT * FROM calls c INNER JOIN contacts ct ON c.contact_id = ct.id " +
           "WHERE ct.isTemporaryZone = 1 ORDER BY c.date DESC")
    List<CallEntity> getAllInTemporaryZone();

    @Query("SELECT * FROM calls c INNER JOIN contacts ct ON c.contact_id = ct.id " +
           "WHERE ct.isBlacklisted = 1 ORDER BY c.date DESC")
    List<CallEntity> getAllInBlacklist();

    @Query("DELETE FROM calls WHERE id = :id")
    void deleteById(long id);

    @Query("UPDATE calls SET recording_path = :recordingPath WHERE id = :id")
    void updateRecordingPath(long id, String recordingPath);

    @Query("SELECT COUNT(*) FROM calls")
    int getCount();

    @Query("SELECT COUNT(*) FROM calls WHERE recording_path IS NOT NULL")
    int getCountWithRecordings();

    @Query("SELECT SUM(recording_size) FROM calls WHERE recording_path IS NOT NULL")
    long getTotalRecordingSize();

    @Query("SELECT SUM(duration) FROM calls")
    long getTotalDuration();
} 