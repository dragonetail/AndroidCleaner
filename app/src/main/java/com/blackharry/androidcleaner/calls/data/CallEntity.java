package com.blackharry.androidcleaner.calls.data;

import androidx.room.*;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;

@Entity(
    tableName = "calls",
    foreignKeys = @ForeignKey(
        entity = ContactEntity.class,
        parentColumns = "id",
        childColumns = "contact_id",
        onDelete = ForeignKey.SET_NULL
    ),
    indices = {@Index("contact_id")}
)
public class CallEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "phone_number")
    public String phoneNumber;

    @ColumnInfo(name = "contact_id")
    public Long contactId;

    @ColumnInfo(name = "call_time")
    public long callTime;

    @ColumnInfo(name = "call_duration")
    public long callDuration;

    @ColumnInfo(name = "recording_filename")
    public String recordingFilename;

    @ColumnInfo(name = "recording_filepath")
    public String recordingFilepath;

    @ColumnInfo(name = "recording_filesize")
    public long recordingFilesize;

    @ColumnInfo(name = "recording_created_time")
    public long recordingCreatedTime;
} 