package com.blackharry.androidcleaner.contacts.ui;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.blackharry.androidcleaner.common.exception.AppException;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;
import com.blackharry.androidcleaner.contacts.data.ContactRepository;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import java.util.List;

public class ContactsViewModel extends AndroidViewModel {
    private static final String TAG = "ContactsViewModel";
    private final ContactRepository repository;
    private final MutableLiveData<List<ContactEntity>> contacts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ContactsViewModel(Application application) {
        super(application);
        LogUtils.logMethodEnter(TAG, "ContactsViewModel");
        repository = ContactRepository.getInstance(application);
        syncContacts();
    }

    public LiveData<List<ContactEntity>> getContacts() {
        return contacts;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void syncContacts() {
        LogUtils.logMethodEnter(TAG, "syncContacts");
        isLoading.setValue(true);

        repository.syncContacts(new ContactRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadContacts();
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "同步联系人失败";
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }

    public void loadContacts() {
        LogUtils.logMethodEnter(TAG, "loadContacts");
        isLoading.setValue(true);

        repository.getContacts(new ContactRepository.Callback<List<ContactEntity>>() {
            @Override
            public void onSuccess(List<ContactEntity> result) {
                contacts.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "获取联系人失败";
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }

    public void loadContactsWithPhoneNumber() {
        LogUtils.logMethodEnter(TAG, "loadContactsWithPhoneNumber");
        isLoading.setValue(true);
        
        repository.getContactsWithPhoneNumber(new ContactRepository.Callback<List<ContactEntity>>() {
            @Override
            public void onSuccess(List<ContactEntity> result) {
                contacts.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void searchContacts(String query) {
        LogUtils.logMethodEnter(TAG, "searchContacts");
        isLoading.setValue(true);
        
        repository.searchContacts(query, new ContactRepository.Callback<List<ContactEntity>>() {
            @Override
            public void onSuccess(List<ContactEntity> result) {
                contacts.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void updateContactSafeZone(ContactEntity contact, boolean isSafeZone) {
        LogUtils.logMethodEnter(TAG, "updateContactSafeZone");
        
        repository.updateContactSafeZone(contact, isSafeZone, new ContactRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadContacts();
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "更新联系人安全区失败";
                error.postValue(errorMessage);
            }
        });
    }

    public void updateContactTempZone(ContactEntity contact, boolean isTempZone) {
        LogUtils.logMethodEnter(TAG, "updateContactTempZone");
        
        repository.updateContactTempZone(contact, isTempZone, new ContactRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadContacts();
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "更新联系人临时区失败";
                error.postValue(errorMessage);
            }
        });
    }

    public void deleteContact(ContactEntity contact) {
        LogUtils.logMethodEnter(TAG, "deleteContact");
        
        repository.deleteContact(contact, new ContactRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadContacts();
            }

            @Override
            public void onError(Exception e) {
                String errorMessage = e instanceof AppException ? 
                    ((AppException) e).getErrorCode().getMessage() : 
                    "删除联系人失败";
                error.postValue(errorMessage);
            }
        });
    }

    public void loadContactsInSafeZone() {
        LogUtils.logMethodEnter(TAG, "loadContactsInSafeZone");
        isLoading.setValue(true);
        
        repository.getContactsInSafeZone(new ContactRepository.Callback<List<ContactEntity>>() {
            @Override
            public void onSuccess(List<ContactEntity> result) {
                contacts.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void loadContactsInTemporaryZone() {
        LogUtils.logMethodEnter(TAG, "loadContactsInTemporaryZone");
        isLoading.setValue(true);
        
        repository.getContactsInTemporaryZone(new ContactRepository.Callback<List<ContactEntity>>() {
            @Override
            public void onSuccess(List<ContactEntity> result) {
                contacts.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void loadBlacklistedContacts() {
        LogUtils.logMethodEnter(TAG, "loadBlacklistedContacts");
        isLoading.setValue(true);
        
        repository.getBlacklistedContacts(new ContactRepository.Callback<List<ContactEntity>>() {
            @Override
            public void onSuccess(List<ContactEntity> result) {
                contacts.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void loadDeletedContacts() {
        LogUtils.logMethodEnter(TAG, "loadDeletedContacts");
        isLoading.setValue(true);
        
        repository.getDeletedContacts(new ContactRepository.Callback<List<ContactEntity>>() {
            @Override
            public void onSuccess(List<ContactEntity> result) {
                contacts.postValue(result);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void updateContactZone(long contactId, boolean isSafeZone, boolean isTemporaryZone, boolean isBlacklisted) {
        LogUtils.logMethodEnter(TAG, "updateContactZone");
        isLoading.setValue(true);
        
        repository.getById(String.valueOf(contactId), new ContactRepository.Callback<ContactEntity>() {
            @Override
            public void onSuccess(ContactEntity contact) {
                contact.setSafeZone(isSafeZone);
                contact.setTemporaryZone(isTemporaryZone);
                contact.setBlacklisted(isBlacklisted);
                contact.setUpdateTime(System.currentTimeMillis());
                
                repository.update(contact, new ContactRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        loadContacts();
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onError(Exception e) {
                        error.postValue(e.getMessage());
                        isLoading.postValue(false);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void restoreContact(long contactId) {
        LogUtils.logMethodEnter(TAG, "restoreContact");
        isLoading.setValue(true);
        
        repository.getById(String.valueOf(contactId), new ContactRepository.Callback<ContactEntity>() {
            @Override
            public void onSuccess(ContactEntity contact) {
                contact.setDeleted(false);
                contact.setUpdateTime(System.currentTimeMillis());
                
                repository.update(contact, new ContactRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        loadContacts();
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onError(Exception e) {
                        error.postValue(e.getMessage());
                        isLoading.postValue(false);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                error.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }
}