package com.blackharry.androidcleaner.contacts.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;
import androidx.annotation.NonNull;
import java.util.List;

@Dao
public interface ContactDao {
    @Query("SELECT * FROM contacts WHERE isDeleted = 0 ORDER BY name ASC")
    List<ContactEntity> getAll();

    @Query("SELECT * FROM contacts WHERE name LIKE :name AND isDeleted = 0 ORDER BY name ASC")
    List<ContactEntity> searchByName(String name);

    @Query("SELECT * FROM contacts WHERE id = :id AND isDeleted = 0")
    ContactEntity getById(String id);

    @Query("SELECT * FROM contacts WHERE isSafeZone = 1 AND isDeleted = 0 ORDER BY name ASC")
    List<ContactEntity> getAllInSafeZone();

    @Query("SELECT * FROM contacts WHERE isTemporaryZone = 1 AND isDeleted = 0 ORDER BY name ASC")
    List<ContactEntity> getAllInTemporaryZone();

    @Query("SELECT * FROM contacts WHERE isBlacklisted = 1 AND isDeleted = 0 ORDER BY name ASC")
    List<ContactEntity> getAllBlacklisted();

    @Query("SELECT * FROM contacts WHERE isDeleted = 1 ORDER BY name ASC")
    List<ContactEntity> getAllDeleted();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ContactEntity> contacts);

    @Update
    void update(ContactEntity contact);

    @Delete
    void delete(ContactEntity contact);

    @Query("DELETE FROM contacts")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM contacts WHERE isDeleted = 0")
    int getCount();

    @Query("SELECT COUNT(*) FROM contacts WHERE isSafeZone = 1 AND isDeleted = 0")
    int getCountInSafeZone();

    @Query("SELECT COUNT(*) FROM contacts WHERE isTemporaryZone = 1 AND isDeleted = 0")
    int getCountInTemporaryZone();

    @Query("SELECT COUNT(*) FROM contacts WHERE isBlacklisted = 1 AND isDeleted = 0")
    int getCountBlacklisted();

    @Query("SELECT COUNT(*) FROM contacts WHERE isDeleted = 1")
    int getCountDeleted();

    @Query("SELECT * FROM contacts WHERE isDeleted = 0 ORDER BY name ASC")
    List<ContactEntity> getAllContacts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertContact(ContactEntity contact);

    @Delete
    void deleteContact(ContactEntity contact);

    @Query("SELECT * FROM contacts WHERE phones != '' AND isDeleted = 0")
    List<ContactEntity> getAllWithPhoneNumber();
} 