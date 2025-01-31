package com.blackharry.androidcleaner.calls.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.ColumnInfo;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;

/**
 * 通话记录实体类
 * 使用Room注解进行数据库映射
 * 通过外键关联联系人表
 */
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
    private long id;               // 数据库主键ID

    @ColumnInfo(name = "number")
    private String number;         // 通话电话号码

    @ColumnInfo(name = "contact_id")
    private Long contactId;        // 通话联系人ID（外键，允许为空）

    @ColumnInfo(name = "type")
    private int type;             // 通话类型

    @ColumnInfo(name = "date")
    private long date;            // 通话日期

    @ColumnInfo(name = "duration")
    private int duration;          // 通话时长

    @ColumnInfo(name = "recording_path")
    private String recordingPath;  // 录音文件路径

    @ColumnInfo(name = "recording_size")
    private long recordingSize;    // 录音文件大小

    @ColumnInfo(name = "file_name")
    private String fileName;       // 录音文件名

    @ColumnInfo(name = "create_time")
    private long createTime;       // 录音文件创建时间（同时也是通话时间）

    @ColumnInfo(name = "name")
    private String name;           // 联系人名称

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRecordingPath() {
        return recordingPath;
    }

    public void setRecordingPath(String recordingPath) {
        this.recordingPath = recordingPath;
    }

    public long getRecordingSize() {
        return recordingSize;
    }

    public void setRecordingSize(long recordingSize) {
        this.recordingSize = recordingSize;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
} 