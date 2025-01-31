package com.blackharry.androidcleaner.calls.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface CallDao {
    @Query("SELECT * FROM calls ORDER BY call_time DESC")
    LiveData<List<CallEntity>> getAllCalls();

    @Query("SELECT * FROM calls WHERE contact_id = :contactId ORDER BY call_time DESC")
    LiveData<List<CallEntity>> getCallsByContact(long contactId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CallEntity call);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CallEntity> calls);

    @Delete
    void delete(CallEntity call);

    @Query("DELETE FROM calls WHERE contact_id = :contactId")
    void deleteCallsByContact(long contactId);

    @Query("SELECT * FROM calls")
    List<CallEntity> getAllCallsSync();

    @Query("SELECT COUNT(*) FROM calls")
    int getCount();

    @Query("SELECT SUM(call_duration) FROM calls")
    long getTotalDuration();

    @Query("SELECT SUM(recording_filesize) FROM calls")
    long getTotalRecordingSize();
} 