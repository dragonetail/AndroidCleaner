package com.blackharry.androidcleaner.contacts.data;

import androidx.room.*;

@Entity(
    tableName = "phone_numbers",
    foreignKeys = @ForeignKey(
        entity = ContactEntity.class,
        parentColumns = "id",
        childColumns = "contact_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("contact_id")}
)
public class PhoneNumberEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "contact_id")
    public long contactId;

    @ColumnInfo(name = "phone_number")
    public String phoneNumber;

    @ColumnInfo(name = "is_primary")
    public boolean isPrimary;
} 