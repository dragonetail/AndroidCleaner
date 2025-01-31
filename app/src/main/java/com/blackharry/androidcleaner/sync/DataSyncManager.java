package com.blackharry.androidcleaner.sync;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;
import com.blackharry.androidcleaner.App;
import com.blackharry.androidcleaner.AppDatabase;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.blackharry.androidcleaner.calls.data.CallEntity;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;
import com.blackharry.androidcleaner.recordings.data.RecordingEntity;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataSyncManager {
    private static final String TAG = "DataSyncManager";
    private final Context context;
    private final AppDatabase database;

    public DataSyncManager() {
        this.context = App.getInstance().getApplicationContext();
        this.database = AppDatabase.getInstance(context);
    }

    public void syncAll() {
        LogUtils.logMethodEnter(TAG, "syncAll");
        long startTime = System.currentTimeMillis();

        try {
            syncContacts();
            syncCalls();
            syncRecordings();
            
            LogUtils.logPerformance(TAG, "完成全部数据同步", startTime);
        } catch (Exception e) {
            LogUtils.logError(TAG, "数据同步过程出错", e);
        }
    }

    public List<ContactEntity> syncContacts() {
        LogUtils.logMethodEnter(TAG, "syncContacts");
        List<ContactEntity> contacts = new ArrayList<>();

        String[] projection = new String[] {
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        try (Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ContactEntity contact = new ContactEntity();
                    contact.setSystemContactId(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
                    contact.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    contact.addPhoneNumber(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    contact.setCreateTime(System.currentTimeMillis());
                    contact.setUpdateTime(System.currentTimeMillis());
                    contacts.add(contact);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "同步联系人失败", e);
        }

        LogUtils.logMethodExit(TAG, "syncContacts");
        return contacts;
    }

    public List<CallEntity> syncCalls() {
        LogUtils.logMethodEnter(TAG, "syncCalls");
        List<CallEntity> calls = new ArrayList<>();

        String[] projection = new String[] {
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        };

        try (Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                CallLog.Calls.DATE + " DESC")) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    CallEntity call = new CallEntity();
                    call.setNumber(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
                    call.setName(cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
                    call.setDate(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
                    call.setDuration((int)cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION)));
                    call.setType(cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)));
                    calls.add(call);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "同步通话记录失败", e);
        }

        LogUtils.logMethodExit(TAG, "syncCalls");
        return calls;
    }

    private void syncRecordings() {
        LogUtils.logMethodEnter(TAG, "syncRecordings");
        long startTime = System.currentTimeMillis();

        try {
            List<RecordingEntity> recordings = new ArrayList<>();
            // 这里需要根据具体的录音文件存储路径来扫描
            File recordingsDir = new File(context.getExternalFilesDir(null), "Recordings");
            if (recordingsDir.exists()) {
                for (File file : recordingsDir.listFiles()) {
                    if (file.isFile()) {
                        RecordingEntity recording = new RecordingEntity();
                        recording.setFileName(file.getName());
                        recording.setFilePath(file.getAbsolutePath());
                        recording.setFileSize(file.length());
                        recording.setCreationTime(file.lastModified());
                        recordings.add(recording);
                    }
                }
            }

            database.recordingDao().insertAll(recordings);
            LogUtils.logPerformance(TAG, String.format("同步录音文件完成，共%d条记录", recordings.size()), startTime);
        } catch (Exception e) {
            LogUtils.logError(TAG, "同步录音文件失败", e);
        }
    }
} 