package com.blackharry.androidcleaner.contacts.data;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.blackharry.androidcleaner.recordings.data.AppDatabase;
import com.blackharry.androidcleaner.utils.AppExecutors;
import com.blackharry.androidcleaner.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;

public class ContactsDebugViewModel extends AndroidViewModel {
    private static final String TAG = "ContactsDebugViewModel";
    private final ContactDao contactDao;
    private final LiveData<List<ContactWithPhoneNumbers>> contactsWithPhoneNumbers;

    public ContactsDebugViewModel(Application application) {
        super(application);
        try {
            LogUtils.logMethodEnter(TAG, "构造函数");
            long startTime = System.currentTimeMillis();
            
            LogUtils.d(TAG, "初始化数据库访问");
            AppDatabase db = AppDatabase.getDatabase(application);
            contactDao = db.contactDao();
            contactsWithPhoneNumbers = contactDao.getAllContactsWithPhoneNumbers();
            
            // 添加测试数据
            LogUtils.d(TAG, "开始插入测试数据");
            insertTestData();
            
            LogUtils.logPerformance(TAG, "ViewModel初始化", startTime);
            LogUtils.logMethodExit(TAG, "构造函数");
        } catch (Exception e) {
            LogUtils.logError(TAG, "初始化失败", e);
            throw e;
        }
    }

    private void insertTestData() {
        try {
            LogUtils.logMethodEnter(TAG, "insertTestData");
            long startTime = System.currentTimeMillis();
            
            AppExecutors.getInstance().diskIO().execute(() -> {
                try {
                    LogUtils.d(TAG, "开始创建测试联系人");
                    // 创建测试联系人
                    ContactEntity contact1 = new ContactEntity();
                    contact1.name = "张三";
                    contact1.isSafeZone = true;
                    contact1.lastUpdated = System.currentTimeMillis();
                    LogUtils.d(TAG, String.format("创建联系人: %s (安全区)", contact1.name));

                    ContactEntity contact2 = new ContactEntity();
                    contact2.name = "李四";
                    contact2.isTempZone = true;
                    contact2.lastUpdated = System.currentTimeMillis();
                    LogUtils.d(TAG, String.format("创建联系人: %s (临时区)", contact2.name));

                    ContactEntity contact3 = new ContactEntity();
                    contact3.name = "王五";
                    contact3.isBlacklist = true;
                    contact3.lastUpdated = System.currentTimeMillis();
                    LogUtils.d(TAG, String.format("创建联系人: %s (黑名单)", contact3.name));

                    // 插入联系人
                    LogUtils.d(TAG, "开始插入联系人数据");
                    contactDao.insert(contact1);
                    contactDao.insert(contact2);
                    contactDao.insert(contact3);
                    LogUtils.d(TAG, "联系人数据插入完成");

                    // 创建电话号码
                    LogUtils.d(TAG, "开始创建电话号码");
                    List<PhoneNumberEntity> phoneNumbers = new ArrayList<>();
                    
                    PhoneNumberEntity phone1 = new PhoneNumberEntity();
                    phone1.contactId = 1;
                    phone1.phoneNumber = "13800138000";
                    phone1.isPrimary = true;
                    phoneNumbers.add(phone1);
                    LogUtils.d(TAG, String.format("创建主要电话: %s (联系人ID: %d)", phone1.phoneNumber, phone1.contactId));

                    PhoneNumberEntity phone2 = new PhoneNumberEntity();
                    phone2.contactId = 1;
                    phone2.phoneNumber = "13900139000";
                    phoneNumbers.add(phone2);
                    LogUtils.d(TAG, String.format("创建次要电话: %s (联系人ID: %d)", phone2.phoneNumber, phone2.contactId));

                    PhoneNumberEntity phone3 = new PhoneNumberEntity();
                    phone3.contactId = 2;
                    phone3.phoneNumber = "13700137000";
                    phone3.isPrimary = true;
                    phoneNumbers.add(phone3);
                    LogUtils.d(TAG, String.format("创建主要电话: %s (联系人ID: %d)", phone3.phoneNumber, phone3.contactId));

                    PhoneNumberEntity phone4 = new PhoneNumberEntity();
                    phone4.contactId = 3;
                    phone4.phoneNumber = "10086";
                    phone4.isPrimary = true;
                    phoneNumbers.add(phone4);
                    LogUtils.d(TAG, String.format("创建主要电话: %s (联系人ID: %d)", phone4.phoneNumber, phone4.contactId));

                    // 插入电话号码
                    LogUtils.d(TAG, String.format("开始插入%d个电话号码", phoneNumbers.size()));
                    contactDao.insertPhoneNumbers(phoneNumbers);
                    LogUtils.d(TAG, "电话号码插入完成");
                    
                    LogUtils.logPerformance(TAG, "测试数据插入", startTime);
                } catch (Exception e) {
                    LogUtils.logError(TAG, "插入测试数据失败", e);
                }
            });
            
            LogUtils.logMethodExit(TAG, "insertTestData");
        } catch (Exception e) {
            LogUtils.logError(TAG, "准备测试数据失败", e);
            throw e;
        }
    }

    public LiveData<List<ContactWithPhoneNumbers>> getContactsWithPhoneNumbers() {
        try {
            LogUtils.logMethodEnter(TAG, "getContactsWithPhoneNumbers");
            LogUtils.d(TAG, "获取所有联系人和电话号码");
            return contactsWithPhoneNumbers;
        } finally {
            LogUtils.logMethodExit(TAG, "getContactsWithPhoneNumbers");
        }
    }

    public void refreshContacts() {
        try {
            LogUtils.logMethodEnter(TAG, "refreshContacts");
            LogUtils.d(TAG, "开始刷新联系人数据");
            // TODO: 实现联系人刷新逻辑
            LogUtils.logMethodExit(TAG, "refreshContacts");
        } catch (Exception e) {
            LogUtils.logError(TAG, "刷新联系人失败", e);
        }
    }
} 