package com.ft_hangouts.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import java.util.ArrayList;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;
import com.ft_hangouts.R;
import com.ft_hangouts.adapters.MessageAdapter;
import com.ft_hangouts.database.DatabaseHelper;
import com.ft_hangouts.models.Contact;
import com.ft_hangouts.models.Message;
import com.ft_hangouts.receivers.SmsReceiver;
import java.util.List;

public class MessageActivity extends BaseActivity {

    private static boolean isVisible = false;
    private static int currentContactId = -1;

    private ListView messageListView;
    private EditText messageEditText;
    private DatabaseHelper dbHelper;
    private Contact contact;
    private int contactId;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;
    private BroadcastReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        messageListView = findViewById(R.id.messageListView);
        messageEditText = findViewById(R.id.messageEditText);
        Button sendButton = findViewById(R.id.sendButton);
        
        applyHeaderColorToButton(sendButton);

        dbHelper = new DatabaseHelper(this);

        contactId = getIntent().getIntExtra("CONTACT_ID", -1);
        currentContactId = contactId;

        if (contactId == -1) {
            Toast.makeText(this, R.string.error_loading_contact, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        contact = dbHelper.getContact(contactId);

        if (contact != null) {
            if (dbHelper.isContactBlocked(contactId)) {
                Toast.makeText(this, getString(R.string.contact_blocked), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            if (getActionBar() != null) {
                getActionBar().setTitle(contact.getName());
            }
        } else {
            Toast.makeText(this, R.string.error_loading_contact, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        messageListView.setAdapter(messageAdapter);

        loadMessages();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        messageListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteMessageDialog(position);
                return true;
            }
        });

        setupSmsReceiver();
    }

    private void loadMessages() {
        messageList.clear();
        messageList.addAll(dbHelper.getMessagesForContact(contactId));
        messageAdapter.notifyDataSetChanged();

        if (!messageList.isEmpty()) {
            messageListView.setSelection(messageList.size() - 1);
        }
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (messageText.isEmpty()) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.sms_permission_required), Toast.LENGTH_LONG).show();
                return;
            }
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            String phoneNumber = contact.getPhoneNumber();
            
            if (messageText.length() > 160) {
                ArrayList<String> parts = smsManager.divideMessage(messageText);
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            } else {
                smsManager.sendTextMessage(phoneNumber, null, messageText, null, null);
            }
            
            long id = dbHelper.addMessage(contactId, messageText, true);

            if (id > 0) {
                messageEditText.setText("");

                loadMessages();
                
                Toast.makeText(this, getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.error_sending_message, Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.send_error, e.getMessage()), Toast.LENGTH_LONG).show();
            Log.e("MessageActivity", "Erreur lors de l'envoi du SMS: " + e.getMessage());
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

    private void setupSmsReceiver() {
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (SmsReceiver.ACTION_SMS_RECEIVED.equals(intent.getAction())) {
                    int receivedContactId = intent.getIntExtra("CONTACT_ID", -1);
                    
                    if (receivedContactId == contactId) {
                        loadMessages();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(SmsReceiver.ACTION_SMS_RECEIVED);
        registerReceiver(smsReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;
        if (smsReceiver != null) {
            IntentFilter filter = new IntentFilter(SmsReceiver.ACTION_SMS_RECEIVED);
            registerReceiver(smsReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false;

        if (smsReceiver != null) {
            try {
                unregisterReceiver(smsReceiver);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isVisible = false;
        currentContactId = -1;

        if (smsReceiver != null) {
            try {
                unregisterReceiver(smsReceiver);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public static boolean isVisibleForContact(int contactId) {
        return isVisible && currentContactId == contactId;
    }

    private void showDeleteMessageDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_message_title);
        builder.setMessage(R.string.delete_message_confirmation);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteMessage(position);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void deleteMessage(int position) {
        if (position >= 0 && position < messageList.size()) {
            Message message = messageList.get(position);
            dbHelper.deleteMessage(message.getId());
            loadMessages();
            Toast.makeText(this, R.string.message_deleted, Toast.LENGTH_SHORT).show();
        }
    }
}