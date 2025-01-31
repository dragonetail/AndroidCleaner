package com.blackharry.androidcleaner.contacts.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.blackharry.androidcleaner.R;

public class ContactActivity extends AppCompatActivity {
    private ContactViewModel contactViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        // Initialize UI components and set up interactions
        // ...
    }

    // Additional methods for UI interactions
    // ...
} 