package com.blackharry.androidcleaner.contacts.ui;

import androidx.lifecycle.ViewModel;
import com.blackharry.androidcleaner.contacts.data.ContactDao;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;
import java.util.List;

public class ContactViewModel extends ViewModel {
    private ContactDao contactDao;

    public ContactViewModel(ContactDao contactDao) {
        this.contactDao = contactDao;
    }

    public List<ContactEntity> getAllContacts() {
        return contactDao.getAllContacts();
    }

    public void addContact(ContactEntity contact) {
        contactDao.insertContact(contact);
    }

    public void deleteContact(ContactEntity contact) {
        contactDao.deleteContact(contact);
    }

    // Additional methods for managing contacts
    // ...
} 