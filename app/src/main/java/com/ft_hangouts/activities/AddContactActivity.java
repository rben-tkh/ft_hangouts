package com.ft_hangouts.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;
import java.util.regex.Pattern;
import com.ft_hangouts.R;
import com.ft_hangouts.database.DatabaseHelper;
import com.ft_hangouts.models.Contact;

public class AddContactActivity extends BaseActivity {

    private EditText nameEditText, phoneEditText, emailEditText, addressEditText, noteEditText;
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(R.string.add_contact);
        }

        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        addressEditText = findViewById(R.id.addressEditText);
        noteEditText = findViewById(R.id.noteEditText);
        Button saveButton = findViewById(R.id.saveButton);
        
        applyHeaderColorToButton(saveButton);

        dbHelper = new DatabaseHelper(this);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContact();
            }
        });
    }

    private void saveContact() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String note = noteEditText.getText().toString().trim();

        if (!validateName(name)) {
            return;
        }

        if (!validatePhone(phone)) {
            return;
        }

        if (!validateEmail(email)) {
            return;
        }

        Contact contact = new Contact(name, phone, email, address, note);

        long id = dbHelper.addContact(contact);

        if (id > 0) {
            Toast.makeText(this, R.string.contact_saved, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_saving_contact, Toast.LENGTH_SHORT).show();
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
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}