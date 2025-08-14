package com.ft_hangouts.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;
import com.ft_hangouts.R;
import com.ft_hangouts.database.DatabaseHelper;
import com.ft_hangouts.models.Contact;
import com.ft_hangouts.receivers.PhoneStateReceiver;
import android.widget.Toast;
import java.io.File;

public class ContactDetailActivity extends BaseActivity {

    private static final int CALL_PERMISSION_REQUEST_CODE = 102;
    
    private TextView nameTextView, phoneTextView, emailTextView, addressTextView, noteTextView;
    private ImageView profileImageView;
    private Button callButton;
    private DatabaseHelper dbHelper;
    private Contact contact;
    private int contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nameTextView = findViewById(R.id.nameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        emailTextView = findViewById(R.id.emailTextView);
        addressTextView = findViewById(R.id.addressTextView);
        noteTextView = findViewById(R.id.noteTextView);
        profileImageView = findViewById(R.id.profileImageView);
        callButton = findViewById(R.id.callButton);
        Button messageButton = findViewById(R.id.messageButton);
        
        applyHeaderColorToButton(messageButton);
        applyHeaderColorToButton(callButton);

        dbHelper = new DatabaseHelper(this);

        contactId = getIntent().getIntExtra("CONTACT_ID", -1);

        if (contactId == -1) {
            Toast.makeText(this, R.string.error_loading_contact, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadContactDetails();

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactDetailActivity.this, MessageActivity.class);
                intent.putExtra("CONTACT_ID", contactId);
                startActivity(intent);
            }
        });
    }

    private void loadContactDetails() {
        contact = dbHelper.getContact(contactId);

        if (contact != null) {
            nameTextView.setText(contact.getName());
            phoneTextView.setText(contact.getPhoneNumber());
            
            if (contact.getEmail() == null || contact.getEmail().trim().isEmpty()) {
                emailTextView.setText(getString(R.string.no_email_saved));
            } else {
                emailTextView.setText(contact.getEmail());
            }
            
            if (contact.getAddress() == null || contact.getAddress().trim().isEmpty()) {
                addressTextView.setText(getString(R.string.no_address_saved));
            } else {
                addressTextView.setText(contact.getAddress());
            }
            
            if (contact.getNote() == null || contact.getNote().trim().isEmpty()) {
                noteTextView.setText(getString(R.string.no_note_saved));
            } else {
                noteTextView.setText(contact.getNote());
            }

            if (contact.getPhotoPath() != null && !contact.getPhotoPath().isEmpty()) {
                displayImage(contact.getPhotoPath());
            }

            if (getActionBar() != null) {
                getActionBar().setTitle(contact.getName());
            }
        } else {
            Toast.makeText(this, R.string.error_loading_contact, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit) {
            Intent intent = new Intent(this, EditContactActivity.class);
            intent.putExtra("CONTACT_ID", contactId);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_delete) {
            dbHelper.deleteContact(contactId);
            Toast.makeText(this, R.string.contact_deleted, Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayImage(String imagePath) {
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            profileImageView.setImageBitmap(bitmap);
        }
    }

    private void makePhoneCall() {
        if (contact == null || contact.getPhoneNumber() == null || contact.getPhoneNumber().trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.no_phone_number), Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
                return;
            }
        }

        initiateCall();
    }

    private void initiateCall() {
        try {
            String phoneNumber = contact.getPhoneNumber();
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            
            PhoneStateReceiver.setCallInProgress(true, getPackageName());
            
            startActivity(callIntent);
            Toast.makeText(this, getString(R.string.calling) + " " + contact.getName(), Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.call_failed), Toast.LENGTH_SHORT).show();
            PhoneStateReceiver.setCallInProgress(false, null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initiateCall();
            } else {
                Toast.makeText(this, getString(R.string.call_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContactDetails();
        
        if (getIntent().getBooleanExtra("RETURN_FROM_CALL", false)) {
            Toast.makeText(this, getString(R.string.returned_from_call), Toast.LENGTH_SHORT).show();
            getIntent().removeExtra("RETURN_FROM_CALL");
        }
    }
}