package com.blackharry.androidcleaner.contacts.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * 联系人实体类
 * 使用Room注解进行数据库映射
 * 使用TypeConverters处理List<String>类型的字段
 */
@Entity(
    tableName = "contacts",
    indices = {@Index(value = {"systemContactId"}, unique = true)}
)
@TypeConverters(Converters.class)
public class ContactEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;                    // 数据库主键ID

    @NonNull
    private String systemContactId = ""; // 系统联系人ID
    
    @NonNull
    private String name = "";           // 联系人姓名
    
    @NonNull
    @TypeConverters(Converters.class)
    private List<String> phones = new ArrayList<>();   // 联系人电话号码（多个）
    
    private boolean isSafeZone;    // 联系人是否是安全区
    private boolean isTemporaryZone; // 联系人是否是临时区
    private boolean isBlacklisted;   // 联系人是否是黑名单
    private boolean isDeleted;       // 联系人是否是已删除
    private long createTime;         // 创建时间
    private long updateTime;         // 更新时间

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getSystemContactId() {
        return systemContactId;
    }

    public void setSystemContactId(@NonNull String systemContactId) {
        this.systemContactId = systemContactId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public List<String> getPhones() {
        return phones != null ? phones : new ArrayList<>();
    }

    public void setPhones(@NonNull List<String> phones) {
        this.phones = phones != null ? phones : new ArrayList<>();
    }

    public boolean isSafeZone() {
        return isSafeZone;
    }

    public void setSafeZone(boolean safeZone) {
        isSafeZone = safeZone;
    }

    public boolean isTemporaryZone() {
        return isTemporaryZone;
    }

    public void setTemporaryZone(boolean temporaryZone) {
        isTemporaryZone = temporaryZone;
    }

    public boolean isBlacklisted() {
        return isBlacklisted;
    }

    public void setBlacklisted(boolean blacklisted) {
        isBlacklisted = blacklisted;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void addPhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.isEmpty() && !phones.contains(phoneNumber)) {
            phones.add(phoneNumber);
        }
    }

    public List<String> getPhoneNumbers() {
        return getPhones();
    }
} 