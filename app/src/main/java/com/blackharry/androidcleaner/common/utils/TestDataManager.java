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
        // 生成联系人数据
        String[] names = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"};
        for (String name : names) {
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
            
            long contactId = database.contactDao().insert(contact);
            
            // 为每个联系人生成通话记录和录音记录
            generateCallsAndRecordings(contactId, phoneNumber);
        }
    }

    private void generateCallsAndRecordings(long contactId, String phoneNumber) {
        // 为每个联系人生成5-10条通话记录
        int numCalls = 5 + random.nextInt(6);
        long now = System.currentTimeMillis();
        
        for (int i = 0; i < numCalls; i++) {
            // 生成通话记录
            long callTime = now - random.nextInt(30) * 24 * 60 * 60 * 1000L; // 最近30天内
            int duration = 30 + random.nextInt(300); // 30-330秒
            String recordingFileName = "call_" + callTime + ".mp3";
            
            CallEntity call = new CallEntity();
            call.setContactId(contactId);
            call.setPhoneNumber(phoneNumber);
            call.setCallTime(callTime);
            call.setDuration(duration);
            call.setRecordingFileName(recordingFileName);
            call.setRecordingFilePath("/storage/emulated/0/Recordings/" + recordingFileName);
            call.setRecordingFileSize(100000L + random.nextInt(900000)); // 100KB-1MB
            call.setRecordingCreateTime(callTime);
            
            database.callDao().insert(call);
            
            // 生成对应的录音记录
            RecordingEntity recording = new RecordingEntity();
            recording.setFileName(recordingFileName);
            recording.setFilePath("/storage/emulated/0/Recordings/" + recordingFileName);
            recording.setFileSize(100000L + random.nextInt(900000));
            recording.setCreateTime(callTime);
            recording.setDuration(duration);
            
            database.recordingDao().insert(recording);
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