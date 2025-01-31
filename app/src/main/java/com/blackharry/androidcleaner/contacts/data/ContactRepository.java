package com.blackharry.androidcleaner.contacts.data;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import com.blackharry.androidcleaner.AppDatabase;
import com.blackharry.androidcleaner.contacts.ContactExceptionHandler;
import com.blackharry.androidcleaner.common.utils.PerformanceMonitor;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.lifecycle.LiveData;

public class ContactRepository {
    private static final String TAG = "ContactRepository";
    private final Context context;
    private final ContactDao contactDao;
    private final ExecutorService executorService;
    private static volatile ContactRepository instance;

    private ContactRepository(Context context) {
        this.context = context.getApplicationContext();
        this.contactDao = AppDatabase.getInstance(context).contactDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static ContactRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (ContactRepository.class) {
                if (instance == null) {
                    instance = new ContactRepository(context);
                }
            }
        }
        return instance;
    }

    public void syncContacts(Callback<Void> callback) {
        LogUtils.logMethodEnter(TAG, "syncContacts");
        PerformanceMonitor.startOperation("Contact", "syncContacts");

        executorService.execute(() -> {
            try {
                // 检查权限
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) 
                        != PackageManager.PERMISSION_GRANTED) {
                    ContactExceptionHandler.handlePermissionError(Manifest.permission.READ_CONTACTS);
                }

                // 查询联系人
                List<ContactEntity> contacts = new ArrayList<>();
                try (Cursor cursor = context.getContentResolver().query(
                        ContactsContract.Contacts.CONTENT_URI,
                        null, null, null,
                        ContactsContract.Contacts.DISPLAY_NAME + " ASC")) {
                    
                    ContactExceptionHandler.validateContactCursor(cursor);
                    
                    while (cursor.moveToNext()) {
                        try {
                            PerformanceMonitor.startOperation("Contact", "processContact");
                            
                            String id = cursor.getString(
                                cursor.getColumnIndex(ContactsContract.Contacts._ID));
                            String name = cursor.getString(
                                cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            int hasPhoneNumber = cursor.getInt(
                                cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                            ContactEntity contact = new ContactEntity();
                            contact.setSystemContactId(id);
                            contact.setName(name);
                            contact.setCreateTime(System.currentTimeMillis());
                            contact.setUpdateTime(System.currentTimeMillis());

                            // 获取电话号码
                            if (hasPhoneNumber > 0) {
                                List<String> phones = new ArrayList<>();
                                try (Cursor phoneCursor = context.getContentResolver().query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                        null, null)) {
                                    while (phoneCursor.moveToNext()) {
                                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        phones.add(phoneNumber);
                                    }
                                }
                                contact.setPhones(phones);
                            }

                            contacts.add(contact);
                            PerformanceMonitor.endOperation("Contact", "processContact");
                        } catch (Exception e) {
                            PerformanceMonitor.recordError("Contact", "processContact", e);
                            LogUtils.logError(TAG, "处理联系人失败", e);
                        }
                    }
                }

                // 更新数据库
                try {
                    PerformanceMonitor.startOperation("Contact", "updateDatabase");
                    contactDao.deleteAll();
                    contactDao.insertAll(contacts);
                    PerformanceMonitor.endOperation("Contact", "updateDatabase");
                } catch (Exception e) {
                    PerformanceMonitor.recordError("Contact", "updateDatabase", e);
                    ContactExceptionHandler.handleDatabaseError(e);
                }

                LogUtils.logPerformance(TAG, String.format("同步了%d个联系人", contacts.size()));
                PerformanceMonitor.endOperation("Contact", "syncContacts");
                callback.onSuccess(null);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "syncContacts", e);
                LogUtils.logError(TAG, "同步联系人失败", e);
                callback.onError(e);
            }
        });
    }

    public void getContacts(Callback<List<ContactEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getContacts");
        PerformanceMonitor.startOperation("Contact", "getContacts");
        
        executorService.execute(() -> {
            try {
                List<ContactEntity> contacts = contactDao.getAll();
                PerformanceMonitor.endOperation("Contact", "getContacts");
                callback.onSuccess(contacts);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "getContacts", e);
                LogUtils.logError(TAG, "获取联系人失败", e);
                callback.onError(e);
            }
        });
    }

    public void getContactsWithPhoneNumber(Callback<List<ContactEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getContactsWithPhoneNumber");
        PerformanceMonitor.startOperation("Contact", "getContactsWithPhoneNumber");
        
        executorService.execute(() -> {
            try {
                List<ContactEntity> contacts = contactDao.getAllWithPhoneNumber();
                PerformanceMonitor.endOperation("Contact", "getContactsWithPhoneNumber");
                callback.onSuccess(contacts);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "getContactsWithPhoneNumber", e);
                LogUtils.logError(TAG, "获取有电话号码的联系人失败", e);
                callback.onError(e);
            }
        });
    }

    public void searchContacts(String query, Callback<List<ContactEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "searchContacts");
        PerformanceMonitor.startOperation("Contact", "searchContacts");
        
        executorService.execute(() -> {
            try {
                List<ContactEntity> contacts = contactDao.searchByName("%" + query + "%");
                PerformanceMonitor.endOperation("Contact", "searchContacts");
                callback.onSuccess(contacts);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "searchContacts", e);
                LogUtils.logError(TAG, "搜索联系人失败", e);
                callback.onError(e);
            }
        });
    }

    public void getContactsInSafeZone(Callback<List<ContactEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getContactsInSafeZone");
        PerformanceMonitor.startOperation("Contact", "getContactsInSafeZone");
        
        executorService.execute(() -> {
            try {
                List<ContactEntity> contacts = contactDao.getAllInSafeZone();
                PerformanceMonitor.endOperation("Contact", "getContactsInSafeZone");
                callback.onSuccess(contacts);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "getContactsInSafeZone", e);
                LogUtils.logError(TAG, "获取安全区联系人失败", e);
                callback.onError(e);
            }
        });
    }

    public void getContactsInTemporaryZone(Callback<List<ContactEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getContactsInTemporaryZone");
        PerformanceMonitor.startOperation("Contact", "getContactsInTemporaryZone");
        
        executorService.execute(() -> {
            try {
                List<ContactEntity> contacts = contactDao.getAllInTemporaryZone();
                PerformanceMonitor.endOperation("Contact", "getContactsInTemporaryZone");
                callback.onSuccess(contacts);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "getContactsInTemporaryZone", e);
                LogUtils.logError(TAG, "获取临时区联系人失败", e);
                callback.onError(e);
            }
        });
    }

    public void getBlacklistedContacts(Callback<List<ContactEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getBlacklistedContacts");
        PerformanceMonitor.startOperation("Contact", "getBlacklistedContacts");
        
        executorService.execute(() -> {
            try {
                List<ContactEntity> contacts = contactDao.getAllBlacklisted();
                PerformanceMonitor.endOperation("Contact", "getBlacklistedContacts");
                callback.onSuccess(contacts);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "getBlacklistedContacts", e);
                LogUtils.logError(TAG, "获取黑名单联系人失败", e);
                callback.onError(e);
            }
        });
    }

    public void getDeletedContacts(Callback<List<ContactEntity>> callback) {
        LogUtils.logMethodEnter(TAG, "getDeletedContacts");
        PerformanceMonitor.startOperation("Contact", "getDeletedContacts");
        
        executorService.execute(() -> {
            try {
                List<ContactEntity> contacts = contactDao.getAllDeleted();
                PerformanceMonitor.endOperation("Contact", "getDeletedContacts");
                callback.onSuccess(contacts);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "getDeletedContacts", e);
                LogUtils.logError(TAG, "获取已删除联系人失败", e);
                callback.onError(e);
            }
        });
    }

    public void updateContactZone(String contactId, boolean isSafeZone, boolean isTemporaryZone, 
            boolean isBlacklisted, Callback<Void> callback) {
        LogUtils.logMethodEnter(TAG, "updateContactZone");
        PerformanceMonitor.startOperation("Contact", "updateContactZone");
        
        executorService.execute(() -> {
            try {
                ContactEntity contact = contactDao.getById(contactId);
                if (contact != null) {
                    contact.setSafeZone(isSafeZone);
                    contact.setTemporaryZone(isTemporaryZone);
                    contact.setBlacklisted(isBlacklisted);
                    contact.setUpdateTime(System.currentTimeMillis());
                    contactDao.update(contact);
                    PerformanceMonitor.endOperation("Contact", "updateContactZone");
                    callback.onSuccess(null);
                } else {
                    throw new ContactExceptionHandler.ContactException(
                        ContactExceptionHandler.ErrorType.INVALID_DATA,
                        "联系人不存在"
                    );
                }
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "updateContactZone", e);
                LogUtils.logError(TAG, "更新联系人分类失败", e);
                callback.onError(e);
            }
        });
    }

    public void softDeleteContact(String contactId, Callback<Void> callback) {
        LogUtils.logMethodEnter(TAG, "softDeleteContact");
        PerformanceMonitor.startOperation("Contact", "softDeleteContact");
        
        executorService.execute(() -> {
            try {
                ContactEntity contact = contactDao.getById(contactId);
                if (contact != null) {
                    contact.setDeleted(true);
                    contact.setUpdateTime(System.currentTimeMillis());
                    contactDao.update(contact);
                    PerformanceMonitor.endOperation("Contact", "softDeleteContact");
                    callback.onSuccess(null);
                } else {
                    throw new ContactExceptionHandler.ContactException(
                        ContactExceptionHandler.ErrorType.INVALID_DATA,
                        "联系人不存在"
                    );
                }
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "softDeleteContact", e);
                LogUtils.logError(TAG, "删除联系人失败", e);
                callback.onError(e);
            }
        });
    }

    public void restoreContact(String contactId, Callback<Void> callback) {
        LogUtils.logMethodEnter(TAG, "restoreContact");
        PerformanceMonitor.startOperation("Contact", "restoreContact");
        
        executorService.execute(() -> {
            try {
                ContactEntity contact = contactDao.getById(contactId);
                if (contact != null) {
                    contact.setDeleted(false);
                    contact.setUpdateTime(System.currentTimeMillis());
                    contactDao.update(contact);
                    PerformanceMonitor.endOperation("Contact", "restoreContact");
                    callback.onSuccess(null);
                } else {
                    throw new ContactExceptionHandler.ContactException(
                        ContactExceptionHandler.ErrorType.INVALID_DATA,
                        "联系人不存在"
                    );
                }
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "restoreContact", e);
                LogUtils.logError(TAG, "恢复联系人失败", e);
                callback.onError(e);
            }
        });
    }

    public void updateContactSafeZone(ContactEntity contact, boolean isSafeZone, Callback<Void> callback) {
        LogUtils.logMethodEnter(TAG, "updateContactSafeZone");
        PerformanceMonitor.startOperation("Contact", "updateContactSafeZone");
        
        executorService.execute(() -> {
            try {
                contact.setSafeZone(isSafeZone);
                contact.setUpdateTime(System.currentTimeMillis());
                contactDao.update(contact);
                PerformanceMonitor.endOperation("Contact", "updateContactSafeZone");
                callback.onSuccess(null);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "updateContactSafeZone", e);
                LogUtils.logError(TAG, "更新联系人安全区状态失败", e);
                callback.onError(e);
            }
        });
    }

    public void updateContactTempZone(ContactEntity contact, boolean isTempZone, Callback<Void> callback) {
        LogUtils.logMethodEnter(TAG, "updateContactTempZone");
        PerformanceMonitor.startOperation("Contact", "updateContactTempZone");
        
        executorService.execute(() -> {
            try {
                contact.setTemporaryZone(isTempZone);
                contact.setUpdateTime(System.currentTimeMillis());
                contactDao.update(contact);
                PerformanceMonitor.endOperation("Contact", "updateContactTempZone");
                callback.onSuccess(null);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "updateContactTempZone", e);
                LogUtils.logError(TAG, "更新联系人临时区状态失败", e);
                callback.onError(e);
            }
        });
    }

    public void deleteContact(ContactEntity contact, Callback<Void> callback) {
        LogUtils.logMethodEnter(TAG, "deleteContact");
        PerformanceMonitor.startOperation("Contact", "deleteContact");
        
        executorService.execute(() -> {
            try {
                contact.setDeleted(true);
                contact.setUpdateTime(System.currentTimeMillis());
                contactDao.update(contact);
                PerformanceMonitor.endOperation("Contact", "deleteContact");
                callback.onSuccess(null);
            } catch (Exception e) {
                PerformanceMonitor.recordError("Contact", "deleteContact", e);
                LogUtils.logError(TAG, "删除联系人失败", e);
                callback.onError(e);
            }
        });
    }

    public void getById(String contactId, Callback<ContactEntity> callback) {
        executorService.execute(() -> {
            try {
                ContactEntity contact = contactDao.getById(contactId);
                callback.onSuccess(contact);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void update(ContactEntity contact, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                contactDao.update(contact);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}