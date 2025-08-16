package com.ft_hangouts.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import com.ft_hangouts.R;
import com.ft_hangouts.adapters.ContactAdapter;
import com.ft_hangouts.database.DatabaseHelper;
import com.ft_hangouts.models.Contact;
import java.util.ArrayList;
import java.util.List;

public class BlockedContactsActivity extends BaseActivity {

    private ContactAdapter contactAdapter;
    private List<Contact> blockedContactsList;
    private DatabaseHelper dbHelper;
    private TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_contacts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(R.string.blocked_contacts);
        }

        dbHelper = new DatabaseHelper(this);

        ListView blockedContactsListView = findViewById(R.id.blockedContactsListView);
        emptyTextView = findViewById(R.id.emptyTextView);
        
        blockedContactsList = new ArrayList<>();
        contactAdapter = new ContactAdapter(this, blockedContactsList);
        blockedContactsListView.setAdapter(contactAdapter);

        loadBlockedContacts();

        blockedContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact selectedContact = blockedContactsList.get(position);
                showUnblockConfirmationDialog(selectedContact);
            }
        });
    }

    private void loadBlockedContacts() {
        blockedContactsList.clear();
        blockedContactsList.addAll(dbHelper.getBlockedContacts());
        contactAdapter.notifyDataSetChanged();
        
        if (blockedContactsList.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(R.string.no_blocked_contacts);
        } else {
            emptyTextView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBlockedContacts();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    private void showUnblockConfirmationDialog(final Contact contact) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.unblock_contact)
                .setMessage(getString(R.string.unblock_contact_confirmation, contact.getName()))
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dbHelper.unblockContact(contact.getId())) {
                            Toast.makeText(BlockedContactsActivity.this, 
                                contact.getName() + " " + getString(R.string.contact_unblocked), 
                                Toast.LENGTH_SHORT).show();
                            loadBlockedContacts();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}