package com.blackharry.androidcleaner.contacts.data;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

public class ContactWithPhoneNumbers {
    @Embedded
    public ContactEntity contact;

    @Relation(
        parentColumn = "id",
        entityColumn = "contact_id"
    )
    public List<PhoneNumberEntity> phoneNumbers;
} 