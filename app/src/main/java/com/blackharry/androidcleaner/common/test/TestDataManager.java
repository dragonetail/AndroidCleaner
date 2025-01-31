package com.blackharry.androidcleaner.common.test;

import android.content.Context;
import android.provider.CallLog;
import com.blackharry.androidcleaner.AppDatabase;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.blackharry.androidcleaner.calls.data.CallEntity;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestDataManager {
    private static final String TAG = "TestDataManager";
    private final Context context;
    private final AppDatabase database;

    public TestDataManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
    }

    public void generateTestData() {
        LogUtils.i(TAG, "生成测试数据");
        
        try {
            // 生成联系人测试数据
            List<ContactEntity> contacts = generateTestContacts();
            database.contactDao().insertAll(contacts);
            LogUtils.i(TAG, String.format("生成了%d个测试联系人", contacts.size()));

            // 生成通话记录测试数据
            List<CallEntity> calls = generateTestCalls(contacts);
            database.callDao().insertAll(calls);
            LogUtils.i(TAG, String.format("生成了%d条测试通话记录", calls.size()));

            // 生成录音文件测试数据
            List<RecordingEntity> recordings = generateTestRecordings(calls);
            database.recordingDao().insertAll(recordings);
            LogUtils.i(TAG, String.format("生成了%d个测试录音文件", recordings.size()));
        } catch (Exception e) {
            LogUtils.logError(TAG, "生成测试数据失败", e);
        }
    }

    private List<ContactEntity> generateTestContacts() {
        List<ContactEntity> contacts = new ArrayList<>();
        String[] names = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"};
        String[] phonePrefix = {"133", "135", "136", "137", "138", "139", "150", "151"};
        
        for (int i = 0; i < names.length; i++) {
            ContactEntity contact = new ContactEntity();
            contact.setSystemContactId(String.valueOf(i + 1));
            contact.setName(names[i]);
            // 生成随机手机号
            String phone = phonePrefix[i] + String.format("%08d", (int)(Math.random() * 100000000));
            contact.addPhoneNumber(phone);
            contact.setCreateTime(System.currentTimeMillis() - (long)(Math.random() * 30 * 24 * 60 * 60 * 1000));
            contact.setUpdateTime(System.currentTimeMillis());
            
            // 随机设置一些属性
            contact.setSafeZone(Math.random() < 0.3);
            contact.setTemporaryZone(Math.random() < 0.2);
            contact.setBlacklisted(Math.random() < 0.1);
            
            contacts.add(contact);
        }
        
        return contacts;
    }

    private List<CallEntity> generateTestCalls(List<ContactEntity> contacts) {
        List<CallEntity> calls = new ArrayList<>();
        int[] callTypes = {CallLog.Calls.INCOMING_TYPE, CallLog.Calls.OUTGOING_TYPE, CallLog.Calls.MISSED_TYPE};
        
        // 为每个联系人生成多条通话记录
        for (ContactEntity contact : contacts) {
            int callCount = 3 + (int)(Math.random() * 8); // 每个联系人3-10条通话记录
            
            for (int i = 0; i < callCount; i++) {
                CallEntity call = new CallEntity();
                call.setNumber(contact.getPhones().get(0));
                call.setName(contact.getName());
                
                // 随机生成通话时间，最近30天内
                long callTime = System.currentTimeMillis() - (long)(Math.random() * 30 * 24 * 60 * 60 * 1000);
                call.setDate(callTime);
                
                // 随机生成通话类型
                int type = callTypes[(int)(Math.random() * callTypes.length)];
                call.setType(type);
                
                // 如果不是未接来电，生成通话时长
                if (type != CallLog.Calls.MISSED_TYPE) {
                    int duration = (int)(Math.random() * 600); // 0-10分钟
                    call.setDuration(duration);
                }
                
                calls.add(call);
            }
        }
        
        // 按时间排序
        Collections.sort(calls, (a, b) -> Long.compare(b.getDate(), a.getDate()));
        return calls;
    }

    private List<RecordingEntity> generateTestRecordings(List<CallEntity> calls) {
        List<RecordingEntity> recordings = new ArrayList<>();
        
        // 为30%的通话生成录音记录
        for (CallEntity call : calls) {
            if (Math.random() < 0.3 && call.getType() != CallLog.Calls.MISSED_TYPE) {
                RecordingEntity recording = new RecordingEntity();
                
                // 生成录音文件名
                String fileName = String.format("record_%d_%s.mp3", 
                    call.getDate(),
                    call.getNumber().replace("+", "").replace(" ", ""));
                recording.setFileName(fileName);
                
                // 设置录音文件路径
                String filePath = context.getExternalFilesDir("Recordings").getAbsolutePath() + 
                    "/" + fileName;
                recording.setFilePath(filePath);
                
                // 设置文件大小（假设每分钟1MB）
                long fileSize = call.getDuration() * 1024L * 1024L / 60;
                recording.setFileSize(fileSize);
                
                // 设置创建时间
                recording.setCreationTime(call.getDate());
                
                // 设置时长（与通话时长相同）
                recording.setDuration(call.getDuration() * 1000L);
                
                recordings.add(recording);
            }
        }
        
        return recordings;
    }
} 