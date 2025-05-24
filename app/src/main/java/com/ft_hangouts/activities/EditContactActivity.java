package com.ft_hangouts.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;
import java.util.regex.Pattern;
import com.ft_hangouts.R;
import com.ft_hangouts.database.DatabaseHelper;
import com.ft_hangouts.models.Contact;

public class EditContactActivity extends BaseActivity {

    private EditText nameEditText, phoneEditText, emailEditText, addressEditText, noteEditText;
    private DatabaseHelper dbHelper;
    private Contact contact;
    private int contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(R.string.edit_contact);
        }

        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        addressEditText = findViewById(R.id.addressEditText);
        noteEditText = findViewById(R.id.noteEditText);
        Button saveButton = findViewById(R.id.saveButton);
        
        applyHeaderColorToButton(saveButton);

        dbHelper = new DatabaseHelper(this);

        contactId = getIntent().getIntExtra("CONTACT_ID", -1);

        if (contactId == -1) {
            Toast.makeText(this, R.string.error_loading_contact, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadContactDetails();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateContact();
            }
        });
    }

    private void loadContactDetails() {
        contact = dbHelper.getContact(contactId);

        if (contact != null) {
            nameEditText.setText(contact.getName());
            phoneEditText.setText(contact.getPhoneNumber());
            emailEditText.setText(contact.getEmail());
            addressEditText.setText(contact.getAddress());
            noteEditText.setText(contact.getNote());
        } else {
            Toast.makeText(this, R.string.error_loading_contact, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateContact() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String note = noteEditText.getText().toString().trim();

        if (!validateName(name) || !validatePhone(phone) || !validateEmail(email)) return;

        contact.setName(name);
        contact.setPhoneNumber(phone);
        contact.setEmail(email);
        contact.setAddress(address);
        contact.setNote(note);

        int result = dbHelper.updateContact(contact);

        if (result > 0) {
            Toast.makeText(this, R.string.contact_updated, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_updating_contact, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateName(String name) {
        if (name.isEmpty()) {
            nameEditText.setError(getString(R.string.field_required));
            nameEditText.requestFocus();
            return false;
        }

        if (name.length() > 30) {
            nameEditText.setError(getString(R.string.name_too_long));
            nameEditText.requestFocus();
            return false;
        }

        nameEditText.setError(null);
        return true;
    }

    private boolean validatePhone(String phone) {
        if (phone.isEmpty()) {
            phoneEditText.setError(getString(R.string.field_required));
            phoneEditText.requestFocus();
            return false;
        }

        if (!Pattern.matches("^(\\+33|0)?[0-9\\s\\-\\.]+$", phone)) {
            phoneEditText.setError(getString(R.string.phone_invalid_format));
            phoneEditText.requestFocus();
            return false;
        }

        if (phone.length() > 20) {
            phoneEditText.setError(getString(R.string.phone_too_long));
            phoneEditText.requestFocus();
            return false;
        }

        phoneEditText.setError(null);
        return true;
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            emailEditText.setError(null);
            return true;
        }

        String emailPattern = "^[a-zA-Z0-9]+@[a-zA-Z]+\\.[a-zA-Z]{2,3}$";
        
        if (!Pattern.matches(emailPattern, email)) {
            emailEditText.setError(getString(R.string.email_invalid_format));
            emailEditText.requestFocus();
            return false;
        }

        emailEditText.setError(null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}