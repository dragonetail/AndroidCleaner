package com.blackharry.androidcleaner.contacts.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface ContactDao {
    @Query("SELECT * FROM contacts")
    LiveData<List<ContactEntity>> getAllContacts();

    @Query("SELECT * FROM contacts WHERE is_safe_zone = 1")
    LiveData<List<ContactEntity>> getSafeZoneContacts();

    @Query("SELECT * FROM contacts WHERE is_temp_zone = 1")
    LiveData<List<ContactEntity>> getTempZoneContacts();

    @Query("SELECT * FROM contacts WHERE is_blacklist = 1")
    LiveData<List<ContactEntity>> getBlacklistContacts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ContactEntity contact);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPhoneNumbers(List<PhoneNumberEntity> phoneNumbers);

    @Transaction
    @Query("SELECT * FROM contacts WHERE id = :contactId")
    LiveData<ContactWithPhoneNumbers> getContactWithPhoneNumbers(long contactId);

    @Delete
    void delete(ContactEntity contact);

    @Transaction
    @Query("SELECT * FROM contacts ORDER BY name")
    LiveData<List<ContactWithPhoneNumbers>> getAllContactsWithPhoneNumbers();

    @Query("SELECT * FROM contacts")
    List<ContactEntity> getAllContactsSync();

    @Query("SELECT * FROM phone_numbers")
    List<PhoneNumberEntity> getAllPhoneNumbersSync();

    @Query("SELECT COUNT(*) FROM contacts")
    int getCount();

    @Query("SELECT COUNT(*) FROM contacts WHERE is_safe_zone = 1")
    int getSafeZoneCount();

    @Query("SELECT COUNT(*) FROM contacts WHERE is_temp_zone = 1")
    int getTempZoneCount();

    @Query("SELECT COUNT(*) FROM contacts WHERE is_blacklist = 1")
    int getBlacklistCount();

    @Query("SELECT COUNT(*) FROM phone_numbers")
    int getPhoneNumbersCount();
} 