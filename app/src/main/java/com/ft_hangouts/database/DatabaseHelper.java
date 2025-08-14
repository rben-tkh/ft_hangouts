package com.ft_hangouts.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.ft_hangouts.models.Contact;
import com.ft_hangouts.models.Message;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contacts_db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_CONTACTS = "contacts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_NOTE = "note";
    private static final String COLUMN_PHOTO_PATH = "photo_path";

    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_MESSAGE_ID = "id";
    private static final String COLUMN_CONTACT_ID = "contact_id";
    private static final String COLUMN_MESSAGE_TEXT = "message";
    private static final String COLUMN_IS_SENT = "is_sent";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_CONTACTS_TABLE =
            "CREATE TABLE " + TABLE_CONTACTS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_PHONE + " TEXT,"
                    + COLUMN_EMAIL + " TEXT,"
                    + COLUMN_ADDRESS + " TEXT,"
                    + COLUMN_NOTE + " TEXT,"
                    + COLUMN_PHOTO_PATH + " TEXT"
                    + ")";

    private static final String CREATE_MESSAGES_TABLE =
            "CREATE TABLE " + TABLE_MESSAGES + "("
                    + COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_CONTACT_ID + " INTEGER,"
                    + COLUMN_MESSAGE_TEXT + " TEXT,"
                    + COLUMN_IS_SENT + " INTEGER,"
                    + COLUMN_TIMESTAMP + " INTEGER,"
                    + "FOREIGN KEY(" + COLUMN_CONTACT_ID + ") REFERENCES " + TABLE_CONTACTS + "(" + COLUMN_ID + ")"
                    + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + COLUMN_PHOTO_PATH + " TEXT");
        }
    }

    public long addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_PHONE, contact.getPhoneNumber());
        values.put(COLUMN_EMAIL, contact.getEmail());
        values.put(COLUMN_ADDRESS, contact.getAddress());
        values.put(COLUMN_NOTE, contact.getNote());
        values.put(COLUMN_PHOTO_PATH, contact.getPhotoPath());

        long id = db.insert(TABLE_CONTACTS, null, values);
        db.close();

        return id;
    }

    public Contact getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Contact contact = null;

        Cursor cursor = db.query(TABLE_CONTACTS, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            contact = new Contact(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NOTE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_PATH))
            );
            cursor.close();
        }

        db.close();
        return contact;
    }

    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " ORDER BY " + COLUMN_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_NOTE)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_PATH))
                );
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return contactList;
    }

    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, contact.getName());
        values.put(COLUMN_PHONE, contact.getPhoneNumber());
        values.put(COLUMN_EMAIL, contact.getEmail());
        values.put(COLUMN_ADDRESS, contact.getAddress());
        values.put(COLUMN_NOTE, contact.getNote());
        values.put(COLUMN_PHOTO_PATH, contact.getPhotoPath());

        int result = db.update(TABLE_CONTACTS, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(contact.getId())});

        db.close();
        return result;
    }

    public void deleteContact(int contactId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_MESSAGES, COLUMN_CONTACT_ID + "=?",
                new String[]{String.valueOf(contactId)});

        db.delete(TABLE_CONTACTS, COLUMN_ID + "=?",
                new String[]{String.valueOf(contactId)});

        db.close();
    }

    public long addMessage(int contactId, String messageText, boolean isSent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_CONTACT_ID, contactId);
        values.put(COLUMN_MESSAGE_TEXT, messageText);
        values.put(COLUMN_IS_SENT, isSent ? 1 : 0);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());

        long id = db.insert(TABLE_MESSAGES, null, values);
        db.close();

        return id;
    }

    public List<Message> getMessagesForContact(int contactId) {
        List<Message> messageList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES +
                " WHERE " + COLUMN_CONTACT_ID + "=" + contactId +
                " ORDER BY " + COLUMN_TIMESTAMP + " ASC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Message message = new Message(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_MESSAGE_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_TEXT)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_IS_SENT)) == 1,
                        cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP))
                );
                messageList.add(message);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return messageList;
    }

    public Contact getContactByPhoneNumber(String phoneNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Contact contact = null;
        
        String normalizedPhone = normalizePhoneNumber(phoneNumber);
        
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " WHERE " + 
                "REPLACE(REPLACE(REPLACE(REPLACE(" + COLUMN_PHONE + ", ' ', ''), '-', ''), '.', ''), '+33', '0') = ?";
        
        Cursor cursor = db.rawQuery(selectQuery, new String[]{normalizedPhone});
        
        if (cursor != null && cursor.moveToFirst()) {
            contact = new Contact(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_NOTE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PHOTO_PATH))
            );
            cursor.close();
        }
        
        db.close();
        return contact;
    }
    
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return "";
        
        String normalized = phoneNumber.replaceAll("[\\s\\-\\.]", "");
        
        if (normalized.startsWith("+33")) {
            normalized = "0" + normalized.substring(3);
        }
        
        return normalized;
    }
    
    public void deleteMessage(int messageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, COLUMN_MESSAGE_ID + "=?",
                new String[]{String.valueOf(messageId)});
        db.close();
    }
}