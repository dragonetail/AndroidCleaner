package com.blackharry.androidcleaner.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.blackharry.androidcleaner.AppDatabase;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;
import com.blackharry.androidcleaner.calls.data.CallEntity;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.Arrays;

public class TestDataManager {
    private static final String TAG = "TestDataManager";
    private static final String PREF_NAME = "test_data_prefs";
    private static final String KEY_DATA_INITIALIZED = "data_initialized";
    private final Context context;
    private final AppDatabase database;
    private final Executor executor;
    private final Random random;

    public TestDataManager(Context context, AppDatabase database) {
        this.context = context.getApplicationContext();
        this.database = database;
        this.executor = Executors.newSingleThreadExecutor();
        this.random = new Random();
    }

    public void initializeTestDataIfNeeded() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_DATA_INITIALIZED, false)) {
            LogUtils.i(TAG, "开始初始化测试数据");
            executor.execute(() -> {
                try {
                    generateTestData();
                    prefs.edit().putBoolean(KEY_DATA_INITIALIZED, true).apply();
                    LogUtils.i(TAG, "测试数据初始化完成");
                } catch (Exception e) {
                    LogUtils.logError(TAG, "测试数据初始化失败", e);
                }
            });
        }
    }

    private void generateTestData() {
        try {
            LogUtils.i(TAG, "开始生成测试数据");
            // 生成联系人数据
            String[] names = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"};
            for (String name : names) {
                try {
                    LogUtils.d(TAG, "开始生成联系人数据: " + name);
                    ContactEntity contact = new ContactEntity();
                    contact.setName(name);
                    contact.setSystemContactId(String.valueOf(System.currentTimeMillis())); // 模拟系统联系人ID
                    String phoneNumber = generatePhoneNumber();
                    contact.setPhones(Arrays.asList(phoneNumber));
                    contact.setSafeZone(random.nextBoolean());
                    contact.setTemporaryZone(random.nextBoolean());
                    contact.setBlacklisted(random.nextBoolean());
                    contact.setDeleted(false);
                    contact.setCreateTime(System.currentTimeMillis());
                    contact.setUpdateTime(System.currentTimeMillis());
                    
                    LogUtils.d(TAG, "插入联系人数据: " + name + ", 电话: " + phoneNumber);
                    long contactId = database.contactDao().insertContact(contact);
                    LogUtils.d(TAG, "联系人数据插入成功，ID: " + contactId);
                    
                    if (contactId > 0) {
                        // 为每个联系人生成通话记录和录音记录
                        generateCallsAndRecordings(contactId, phoneNumber, name);
                    } else {
                        LogUtils.e(TAG, "联系人插入失败，未获取到有效ID: " + name);
                    }
                } catch (Exception e) {
                    LogUtils.logError(TAG, "生成联系人数据失败: " + name, e);
                }
            }
            LogUtils.i(TAG, "测试数据生成完成");
        } catch (Exception e) {
            LogUtils.logError(TAG, "生成测试数据失败", e);
            throw e;
        }
    }

    private void generateCallsAndRecordings(long contactId, String phoneNumber, String name) {
        try {
            LogUtils.d(TAG, "开始为联系人生成通话记录: " + name + ", contactId: " + contactId);
            // 为每个联系人生成5-10条通话记录
            int numCalls = 5 + random.nextInt(6);
            long now = System.currentTimeMillis();
            
            for (int i = 0; i < numCalls; i++) {
                try {
                    // 生成通话记录
                    long callTime = now - random.nextInt(30) * 24 * 60 * 60 * 1000L; // 最近30天内
                    int duration = 30 + random.nextInt(300); // 30-330秒
                    String recordingFileName = "call_" + callTime + ".mp3";
                    String recordingPath = "/storage/emulated/0/Recordings/" + recordingFileName;
                    long fileSize = 100000L + random.nextInt(900000); // 100KB-1MB
                    
                    LogUtils.d(TAG, String.format("生成通话记录 %d/%d: %s, contactId: %d, 时长: %d秒", 
                        i + 1, numCalls, name, contactId, duration));
                    
                    // 先插入录音记录
                    RecordingEntity recording = new RecordingEntity();
                    recording.setFileName(recordingFileName);
                    recording.setFilePath(recordingPath);
                    recording.setFileSize(fileSize);
                    recording.setCreationTime(callTime);
                    recording.setDuration(duration);
                    
                    LogUtils.d(TAG, "插入录音记录: " + recordingFileName);
                    database.recordingDao().insert(recording);
                    
                    // 再插入通话记录
                    CallEntity call = new CallEntity();
                    call.setContactId(contactId);
                    call.setNumber(phoneNumber);
                    call.setName(name);
                    call.setDate(callTime);
                    call.setDuration(duration);
                    call.setFileName(recordingFileName);
                    call.setRecordingPath(recordingPath);
                    call.setRecordingSize(fileSize);
                    call.setCreateTime(callTime);
                    call.setType(1); // 1表示已接来电
                    
                    LogUtils.d(TAG, "插入通话记录: " + recordingFileName + ", contactId: " + contactId);
                    database.callDao().insert(call);
                    
                    LogUtils.d(TAG, "通话和录音记录插入成功: " + recordingFileName);
                } catch (Exception e) {
                    LogUtils.logError(TAG, String.format("生成第 %d 条通话记录失败: %s, contactId: %d", 
                        i + 1, name, contactId), e);
                }
            }
            LogUtils.d(TAG, "完成联系人通话记录生成: " + name);
        } catch (Exception e) {
            LogUtils.logError(TAG, "生成通话记录失败: " + name + ", contactId: " + contactId, e);
            throw e;
        }
    }

    private String generatePhoneNumber() {
        StringBuilder number = new StringBuilder("1");
        String[] prefixes = {"30", "31", "32", "33", "34", "35", "36", "37", "38", "39", 
                            "50", "51", "52", "53", "55", "56", "57", "58", "59"};
        number.append(prefixes[random.nextInt(prefixes.length)]);
        
        for (int i = 0; i < 8; i++) {
            number.append(random.nextInt(10));
        }
        return number.toString();
    }
}