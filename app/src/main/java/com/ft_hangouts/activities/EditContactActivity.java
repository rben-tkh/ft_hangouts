package com.ft_hangouts.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import com.ft_hangouts.R;
import com.ft_hangouts.database.DatabaseHelper;
import com.ft_hangouts.models.Contact;

public class EditContactActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 101;
    
    private EditText nameEditText, phoneEditText, emailEditText, addressEditText, noteEditText;
    private ImageView profileImageView;
    private Button selectPhotoButton;
    private String selectedPhotoPath = null;
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
        profileImageView = findViewById(R.id.profileImageView);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        
        applyHeaderColorToButton(saveButton);
        applyHeaderColorToButton(selectPhotoButton);

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
        
        selectPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndSelectPhoto();
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
            
            selectedPhotoPath = contact.getPhotoPath();
            if (selectedPhotoPath != null && !selectedPhotoPath.isEmpty()) {
                displayImage(selectedPhotoPath);
            }
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
        contact.setPhotoPath(selectedPhotoPath);

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

    private void checkPermissionAndSelectPhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        } else {
            openGallery();
        }
    }
    
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            
            if (!isImageFile(imageUri)) {
                Toast.makeText(this, getString(R.string.invalid_image_format), Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                String savedPath = saveImageToInternalStorage(imageUri);
                if (savedPath != null) {
                    if (selectedPhotoPath != null && !selectedPhotoPath.equals(savedPath)) {
                        File oldFile = new File(selectedPhotoPath);
                        if (oldFile.exists()) {
                            oldFile.delete();
                        }
                    }
                    selectedPhotoPath = savedPath;
                    displayImage(savedPath);
                }
            } catch (IOException e) {
                Toast.makeText(this, getString(R.string.error_saving_image), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private boolean isImageFile(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        return mimeType != null && mimeType.startsWith("image/");
    }
    
    private String saveImageToInternalStorage(Uri imageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        if (inputStream == null) return null;
        
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        
        if (bitmap == null) return null;
        
        File directory = new File(getFilesDir(), "contact_photos");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String fileName = "photo_" + System.currentTimeMillis() + ".jpg";
        File file = new File(directory, fileName);
        
        FileOutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        outputStream.close();
        
        return file.getAbsolutePath();
    }
    
    private void displayImage(String imagePath) {
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            profileImageView.setImageBitmap(bitmap);
        }
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