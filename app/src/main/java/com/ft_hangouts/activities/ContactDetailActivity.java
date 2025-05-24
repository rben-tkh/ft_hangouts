package com.ft_hangouts.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;
import com.ft_hangouts.R;
import com.ft_hangouts.database.DatabaseHelper;
import com.ft_hangouts.models.Contact;
import android.widget.Toast;

public class ContactDetailActivity extends BaseActivity {

    private TextView nameTextView, phoneTextView, emailTextView, addressTextView, noteTextView;
    private DatabaseHelper dbHelper;
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
        Button messageButton = findViewById(R.id.messageButton);
        
        applyHeaderColorToButton(messageButton);

        dbHelper = new DatabaseHelper(this);

        contactId = getIntent().getIntExtra("CONTACT_ID", -1);

        if (contactId == -1) {
            Toast.makeText(this, R.string.error_loading_contact, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadContactDetails();

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
        Contact contact = dbHelper.getContact(contactId);

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

    @Override
    protected void onResume() {
        super.onResume();
        loadContactDetails();
    }
}