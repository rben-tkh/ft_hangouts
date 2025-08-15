package com.ft_hangouts.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;
import com.ft_hangouts.R;
import com.ft_hangouts.adapters.ContactAdapter;
import com.ft_hangouts.database.DatabaseHelper;
import com.ft_hangouts.models.Contact;
import com.ft_hangouts.receivers.SmsReceiver;
import android.widget.Button;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends BaseActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;

    private ContactAdapter contactAdapter;
    private List<Contact> contactList;
    private DatabaseHelper dbHelper;
    private BroadcastReceiver contactCreatedReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        Objects.requireNonNull(getActionBar()).setTitle(R.string.app_name);

        dbHelper = new DatabaseHelper(this);

        ListView contactListView = findViewById(R.id.contactListView);
        contactList = new ArrayList<>();
        contactAdapter = new ContactAdapter(this, contactList);
        contactListView.setAdapter(contactAdapter);

        requestPermissions();
        
        requestDefaultSmsApp();
        
        loadContacts();

        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact selectedContact = contactList.get(position);
                Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
                intent.putExtra("CONTACT_ID", selectedContact.getId());
                startActivity(intent);
            }
        });

        Button addButton = findViewById(R.id.addButton);
        applyHeaderColorToButton(addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
                startActivity(intent);
            }
        });
        
        setupContactCreatedReceiver();
    }
    
    private void setupContactCreatedReceiver() {
        contactCreatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String phoneNumber = intent.getStringExtra("PHONE_NUMBER");
                loadContacts();
                Toast.makeText(MainActivity.this, 
                    getString(R.string.contact_auto_created) + ": " + phoneNumber, 
                    Toast.LENGTH_LONG).show();
            }
        };
        
        IntentFilter filter = new IntentFilter(SmsReceiver.ACTION_CONTACT_CREATED);
        registerReceiver(contactCreatedReceiver, filter);
    }

    private void loadContacts() {
        contactList.clear();
        contactList.addAll(dbHelper.getAllContacts());
        contactAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadContacts();

        checkBackgroundReturn();
        
        if (getIntent().getBooleanExtra("RETURN_FROM_CALL", false)) {
            Toast.makeText(this, getString(R.string.returned_from_call), Toast.LENGTH_SHORT).show();
            getIntent().removeExtra("RETURN_FROM_CALL");
        }
    }

    private void checkBackgroundReturn() {
        SharedPreferences prefs = getSharedPreferences("BackgroundTime", MODE_PRIVATE);
        boolean returnedFromBackground = prefs.getBoolean("returned_from_background", false);
        
        if (returnedFromBackground) {
            long timeInBackground = prefs.getLong("time", 0);
            
            if (timeInBackground > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String timeString = sdf.format(new Date(timeInBackground));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, getString(R.string.background_time) + ' ' + timeString, Toast.LENGTH_LONG).show();
                    }
                }, 100);
                
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("returned_from_background", false);
                editor.apply();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_color) {
            Intent intent = new Intent(this, HeaderColorActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_change_language) {
            Intent intent = new Intent(this, LanguageActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_toggle_dark_mode) {
            toggleDarkMode();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    private void toggleDarkMode() {
        boolean currentMode = isDarkModeEnabled();
        setDarkModeEnabled(!currentMode);
        
        // Show toast to indicate mode change
        String message = !currentMode ? 
            getString(R.string.dark_mode_enabled) : 
            getString(R.string.dark_mode_disabled);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        // Apply theme immediately
        applyTheme();
        
        // Refresh the adapter to apply theme to list items
        if (contactAdapter != null) {
            contactAdapter.notifyDataSetChanged();
        }
    }
    
    private void requestDefaultSmsApp() {
        String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);

        if (!getPackageName().equals(defaultSmsPackage)) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());

            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.set_as_default_sms_app), Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            };
            
            List<String> permissionsToRequest = new ArrayList<>();
            
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            
            if (!permissionsToRequest.isEmpty()) {
                requestPermissions(permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            
            if (!allPermissionsGranted) {
                Toast.makeText(this, getString(R.string.permissions_required), Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (contactCreatedReceiver != null) {
            unregisterReceiver(contactCreatedReceiver);
        }
    }
}