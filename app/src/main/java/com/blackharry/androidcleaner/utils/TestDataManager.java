package com.blackharry.androidcleaner.utils;

import android.content.Context;
import com.blackharry.androidcleaner.calls.data.CallEntity;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;
import com.blackharry.androidcleaner.contacts.data.PhoneNumberEntity;
import com.blackharry.androidcleaner.recordings.data.AppDatabase;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import java.util.ArrayList;
import java.util.List;

public class TestDataManager {
    private static final String TAG = "TestDataManager";

    public static void insertTestData(Context context) {
        try {
            LogUtils.logMethodEnter(TAG, "insertTestData");
            long startTime = System.currentTimeMillis();
            
            LogUtils.d(TAG, "开始插入测试数据");
            AppDatabase db = AppDatabase.getDatabase(context);
            AppExecutors.getInstance().diskIO().execute(() -> {
                try {
                    // 插入联系人测试数据
                    insertTestContacts(db);
                    // 插入通话记录测试数据
                    insertTestCalls(db);
                    // 插入录音文件测试数据
                    insertTestRecordings(db);
                    
                    LogUtils.logPerformance(TAG, "插入测试数据", startTime);
                } catch (Exception e) {
                    LogUtils.logError(TAG, "插入测试数据失败", e);
                }
            });
            
            LogUtils.logMethodExit(TAG, "insertTestData");
        } catch (Exception e) {
            LogUtils.logError(TAG, "初始化测试数据失败", e);
        }
    }

    private static void insertTestContacts(AppDatabase db) {
        try {
            LogUtils.logMethodEnter(TAG, "insertTestContacts");
            long startTime = System.currentTimeMillis();
            
            // 创建测试联系人
            List<ContactEntity> contacts = new ArrayList<>();
            
            ContactEntity contact1 = new ContactEntity();
            contact1.name = "张三";
            contact1.isSafeZone = true;
            contact1.lastUpdated = System.currentTimeMillis();
            contacts.add(contact1);
            LogUtils.d(TAG, String.format("创建测试联系人: %s (安全区)", contact1.name));

            ContactEntity contact2 = new ContactEntity();
            contact2.name = "李四";
            contact2.isTempZone = true;
            contact2.lastUpdated = System.currentTimeMillis() - 86400000; // 1天前
            contacts.add(contact2);
            LogUtils.d(TAG, String.format("创建测试联系人: %s (临时区)", contact2.name));

            ContactEntity contact3 = new ContactEntity();
            contact3.name = "王五";
            contact3.isBlacklist = true;
            contact3.lastUpdated = System.currentTimeMillis() - 172800000; // 2天前
            contacts.add(contact3);
            LogUtils.d(TAG, String.format("创建测试联系人: %s (黑名单)", contact3.name));

            // 插入联系人
            for (ContactEntity contact : contacts) {
                db.contactDao().insert(contact);
            }
            LogUtils.d(TAG, String.format("插入%d个测试联系人", contacts.size()));

            // 创建电话号码
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
            db.contactDao().insertPhoneNumbers(phoneNumbers);
            LogUtils.d(TAG, String.format("插入%d个测试电话号码", phoneNumbers.size()));
            
            LogUtils.logPerformance(TAG, "插入联系人数据", startTime);
            LogUtils.logMethodExit(TAG, "insertTestContacts");
        } catch (Exception e) {
            LogUtils.logError(TAG, "插入联系人测试数据失败", e);
            throw e;
        }
    }

    private static void insertTestCalls(AppDatabase db) {
        try {
            LogUtils.logMethodEnter(TAG, "insertTestCalls");
            long startTime = System.currentTimeMillis();
            
            List<CallEntity> calls = new ArrayList<>();
            long now = System.currentTimeMillis();
            
            // 张三的通话记录
            CallEntity call1 = new CallEntity();
            call1.phoneNumber = "13800138000";
            call1.contactId = 1L;
            call1.callTime = now - 3600000; // 1小时前
            call1.callDuration = 300; // 5分钟
            call1.recordingFilename = "call_1.mp3";
            call1.recordingFilepath = "/storage/emulated/0/Recordings/call_1.mp3";
            call1.recordingFilesize = 1024 * 1024; // 1MB
            call1.recordingCreatedTime = call1.callTime;
            calls.add(call1);
            LogUtils.d(TAG, String.format("创建通话记录: %s, 时长: %d秒", call1.phoneNumber, call1.callDuration));

            // 李四的通话记录
            CallEntity call2 = new CallEntity();
            call2.phoneNumber = "13700137000";
            call2.contactId = 2L;
            call2.callTime = now - 7200000; // 2小时前
            call2.callDuration = 600; // 10分钟
            call2.recordingFilename = "call_2.mp3";
            call2.recordingFilepath = "/storage/emulated/0/Recordings/call_2.mp3";
            call2.recordingFilesize = 2 * 1024 * 1024; // 2MB
            call2.recordingCreatedTime = call2.callTime;
            calls.add(call2);
            LogUtils.d(TAG, String.format("创建通话记录: %s, 时长: %d秒", call2.phoneNumber, call2.callDuration));

            // 王五的通话记录
            CallEntity call3 = new CallEntity();
            call3.phoneNumber = "10086";
            call3.contactId = 3L;
            call3.callTime = now - 86400000; // 1天前
            call3.callDuration = 120; // 2分钟
            call3.recordingFilename = "call_3.mp3";
            call3.recordingFilepath = "/storage/emulated/0/Recordings/call_3.mp3";
            call3.recordingFilesize = 512 * 1024; // 512KB
            call3.recordingCreatedTime = call3.callTime;
            calls.add(call3);
            LogUtils.d(TAG, String.format("创建通话记录: %s, 时长: %d秒", call3.phoneNumber, call3.callDuration));

            // 插入通话记录
            db.callDao().insertAll(calls);
            LogUtils.d(TAG, String.format("插入%d条通话记录", calls.size()));
            
            LogUtils.logPerformance(TAG, "插入通话记录", startTime);
            LogUtils.logMethodExit(TAG, "insertTestCalls");
        } catch (Exception e) {
            LogUtils.logError(TAG, "插入通话记录测试数据失败", e);
            throw e;
        }
    }

    private static void insertTestRecordings(AppDatabase db) {
        try {
            LogUtils.logMethodEnter(TAG, "insertTestRecordings");
            long startTime = System.currentTimeMillis();
            
            List<RecordingEntity> recordings = new ArrayList<>();
            long now = System.currentTimeMillis();

            // 普通录音文件
            RecordingEntity rec1 = new RecordingEntity();
            rec1.setFileName("voice_memo_1.mp3");
            rec1.setFilePath("/storage/emulated/0/Recordings/voice_memo_1.mp3");
            rec1.setFileSize(1024 * 1024); // 1MB
            rec1.setCreatedTime(now - 3600000); // 1小时前
            rec1.setDuration(300); // 5分钟
            recordings.add(rec1);
            LogUtils.d(TAG, String.format("创建录音文件: %s, 时长: %d秒, 大小: %d字节", 
                rec1.getFileName(), rec1.getDuration(), rec1.getFileSize()));

            RecordingEntity rec2 = new RecordingEntity();
            rec2.setFileName("meeting_notes.mp3");
            rec2.setFilePath("/storage/emulated/0/Recordings/meeting_notes.mp3");
            rec2.setFileSize(2 * 1024 * 1024); // 2MB
            rec2.setCreatedTime(now - 7200000); // 2小时前
            rec2.setDuration(1800); // 30分钟
            recordings.add(rec2);
            LogUtils.d(TAG, String.format("创建录音文件: %s, 时长: %d秒, 大小: %d字节", 
                rec2.getFileName(), rec2.getDuration(), rec2.getFileSize()));

            // 插入录音记录
            for (RecordingEntity recording : recordings) {
                db.recordingDao().insert(recording);
            }
            LogUtils.d(TAG, String.format("插入%d个录音文件记录", recordings.size()));
            
            LogUtils.logPerformance(TAG, "插入录音文件记录", startTime);
            LogUtils.logMethodExit(TAG, "insertTestRecordings");
        } catch (Exception e) {
            LogUtils.logError(TAG, "插入录音文件测试数据失败", e);
            throw e;
        }
    }
} 