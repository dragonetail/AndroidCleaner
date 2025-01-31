package com.blackharry.androidcleaner.contacts.data;

import androidx.room.*;
import java.util.List;

@Entity(tableName = "contacts")
public class ContactEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "is_safe_zone")
    public boolean isSafeZone;

    @ColumnInfo(name = "is_temp_zone")
    public boolean isTempZone;

    @ColumnInfo(name = "is_blacklist")
    public boolean isBlacklist;

    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;

    @ColumnInfo(name = "last_updated")
    public long lastUpdated;
} 