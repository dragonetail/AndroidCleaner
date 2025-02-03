package com.blackharry.androidcleaner.recordings.data;

import androidx.lifecycle.LiveData;
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

    @Query("SELECT SUM(duration) FROM recordings")
    long getTotalRecordingDuration();

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

    @Query("SELECT * FROM recordings " +
           "WHERE (:timeFilter = 'ALL' OR " +
           "      (:timeFilter = 'YEAR_AGO' AND creationTime < :yearStart) OR " +
           "      (:timeFilter = 'TODAY' AND creationTime >= :todayStart) OR " +
           "      (:timeFilter = 'WEEK' AND creationTime >= :weekStart) OR " +
           "      (:timeFilter = 'MONTH' AND creationTime >= :monthStart) OR " +
           "      (:timeFilter = 'QUARTER' AND creationTime >= :quarterStart)) " +
           "AND (:durationFilter = 'ALL' OR " +
           "     (:durationFilter = 'MIN_1' AND duration <= 60000) OR " +
           "     (:durationFilter = 'MIN_5' AND duration <= 300000) OR " +
           "     (:durationFilter = 'MIN_30' AND duration <= 1800000) OR " +
           "     (:durationFilter = 'HOUR_2' AND duration <= 7200000) OR " +
           "     (:durationFilter = 'LONGER' AND duration > 7200000)) " +
           "ORDER BY " +
           "CASE " +
           "    WHEN :sortOrder = 'TIME_DESC' THEN creationTime " +
           "    WHEN :sortOrder = 'TIME_ASC' THEN -creationTime " +
           "    WHEN :sortOrder = 'SIZE_DESC' THEN fileSize " +
           "    WHEN :sortOrder = 'SIZE_ASC' THEN -fileSize " +
           "END DESC")
    List<RecordingEntity> getFilteredAndSorted(String timeFilter, long todayStart, long weekStart, 
                                             long monthStart, long quarterStart, long yearStart,
                                             String durationFilter, String sortOrder);

    @Query("SELECT * FROM recordings")
    LiveData<List<RecordingEntity>> getAllRecordings();

    @Query("SELECT * FROM recordings WHERE " +
           "creationTime BETWEEN :startDate AND :endDate AND " +
           "duration BETWEEN :minDuration AND :maxDuration " +
           "ORDER BY " +
           "CASE :sortOption " +
           "    WHEN 'DATE_ASC' THEN creationTime END ASC, " +
           "CASE :sortOption " +
           "    WHEN 'DATE_DESC' THEN creationTime END DESC, " +
           "CASE :sortOption " +
           "    WHEN 'DURATION_ASC' THEN duration END ASC, " +
           "CASE :sortOption " +
           "    WHEN 'DURATION_DESC' THEN duration END DESC, " +
           "CASE :sortOption " +
           "    WHEN 'SIZE_ASC' THEN fileSize END ASC, " +
           "CASE :sortOption " +
           "    WHEN 'SIZE_DESC' THEN fileSize END DESC")
    LiveData<List<RecordingEntity>> getFilteredRecordings(long startDate, long endDate, 
                                                         long minDuration, long maxDuration,
                                                         String sortOption);

    @Query("DELETE FROM recordings WHERE id IN (:ids)")
    void deleteByIds(List<Long> ids);
}