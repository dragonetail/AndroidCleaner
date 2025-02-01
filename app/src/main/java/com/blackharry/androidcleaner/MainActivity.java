package com.blackharry.androidcleaner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.blackharry.androidcleaner.common.utils.LogUtils;
import com.blackharry.androidcleaner.contacts.ui.ContactsFragment;
import com.blackharry.androidcleaner.overview.OverviewFragment;
import com.blackharry.androidcleaner.recordings.ui.RecordingsFragment;
import com.blackharry.androidcleaner.calls.ui.CallsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private BottomNavigationView bottomNav;
    private Fragment currentFragment;
    private Toolbar toolbar;
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.logMethodEnter(TAG, "onCreate");

        initializeToolbar();
        initializeBottomNavigation();
        checkPermissions();
    }

    private void initializeToolbar() {
        LogUtils.logMethodEnter(TAG, "initializeToolbar");
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        updateTitle(R.string.title_bar_overview);
    }

    private void initializeBottomNavigation() {
        LogUtils.logMethodEnter(TAG, "initializeBottomNavigation");
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(this);
        showFragment(new OverviewFragment());
    }

    private void checkPermissions() {
        LogUtils.logMethodEnter(TAG, "checkPermissions");
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
                break;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        LogUtils.logMethodEnter(TAG, "onNavigationItemSelected");
        int itemId = item.getItemId();
        
        // 清除当前菜单项
        if (toolbar != null) {
            toolbar.getMenu().clear();
        }
        
        if (itemId == R.id.nav_overview) {
            showFragment(new OverviewFragment());
            updateTitle(R.string.title_bar_overview);
            return true;
        } else if (itemId == R.id.nav_recordings) {
            showFragment(new RecordingsFragment());
            updateTitle(R.string.title_bar_recordings);
            return true;
        } else if (itemId == R.id.nav_calls) {
            showFragment(new CallsFragment());
            updateTitle(R.string.title_bar_calls);
            return true;
        } else if (itemId == R.id.nav_contacts) {
            showFragment(new ContactsFragment());
            updateTitle(R.string.title_bar_contacts);
            return true;
        }
        
        return false;
    }

    private void updateTitle(int titleResId) {
        LogUtils.logMethodEnter(TAG, "updateTitle");
        if (toolbarTitle != null) {
            toolbarTitle.setText(titleResId);
        }
    }

    private void showFragment(Fragment fragment) {
        LogUtils.logMethodEnter(TAG, "showFragment");
        if (currentFragment != null && fragment.getClass().equals(currentFragment.getClass())) {
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }

        String tag = fragment.getClass().getSimpleName();
        Fragment existingFragment = getSupportFragmentManager().findFragmentByTag(tag);
        
        if (existingFragment == null) {
            transaction.add(R.id.fragment_container, fragment, tag);
        } else {
            transaction.show(existingFragment);
            fragment = existingFragment;
        }
        
        transaction.commit();
        currentFragment = fragment;
    }
}