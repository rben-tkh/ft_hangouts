package com.ft_hangouts.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;
import com.ft_hangouts.R;
import com.ft_hangouts.utils.LocaleHelper;

public abstract class BaseActivity extends Activity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(updateContextLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Context updateContextLocale(Context context) {
        try {
            String language = LocaleHelper.getLanguage(context);
            return LocaleHelper.updateResources(context, language);
        } catch (Exception e) {
            return context;
        }
    }

    protected void setupToolbar(Toolbar toolbar) {
        if (toolbar != null) {
            setActionBar(toolbar);

            updateHeaderColor(toolbar);
        }
    }

    protected void updateHeaderColor(Toolbar toolbar) {
        if (toolbar != null) {
            try {
                SharedPreferences prefs = getSharedPreferences("HeaderColor", MODE_PRIVATE);
                int colorResId = prefs.getInt("color", R.color.colorPrimary);

                toolbar.setBackgroundColor(getResources().getColor(colorResId));
            } catch (Exception e) {
                toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
        }
    }

    protected void applyHeaderColorToButton(Button button) {
        if (button != null) {
            try {
                SharedPreferences prefs = getSharedPreferences("HeaderColor", MODE_PRIVATE);
                int colorResId = prefs.getInt("color", R.color.colorPrimary);
                
                button.setBackgroundColor(getResources().getColor(colorResId));
                
                button.setTextColor(Color.WHITE);
            } catch (Exception e) {
                button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                button.setTextColor(Color.WHITE);
            }
        }
    }

    protected boolean isDarkModeEnabled() {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        return prefs.getBoolean("dark_mode", false);
    }

    protected void setDarkModeEnabled(boolean enabled) {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("dark_mode", enabled);
        editor.apply();
    }

    protected void applyTheme() {
        boolean isDarkMode = isDarkModeEnabled();
        
        // Apply theme to root layout
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            if (isDarkMode) {
                rootView.setBackgroundColor(Color.parseColor("#2B2B2B"));
            } else {
                rootView.setBackgroundColor(Color.WHITE);
            }
        }
        
        // Apply to main containers
        applyThemeToView(findViewById(R.id.main_container));
        applyThemeToView(findViewById(R.id.contact_detail_container));
        applyThemeToView(findViewById(R.id.message_container));
        applyThemeToView(findViewById(R.id.contentLayout));
        
        // Apply to ListView
        ListView listView = findViewById(R.id.contactListView);
        if (listView != null) {
            if (isDarkMode) {
                listView.setBackgroundColor(Color.parseColor("#2B2B2B"));
                listView.setDivider(getResources().getDrawable(android.R.color.darker_gray));
            } else {
                listView.setBackgroundColor(Color.WHITE);
                listView.setDivider(getResources().getDrawable(android.R.color.darker_gray));
            }
        }
        
        // Apply to TextViews and Labels
        applyThemeToTextView(findViewById(R.id.emptyTextView));
        applyThemeToTextView(findViewById(R.id.nameTextView));
        applyThemeToTextView(findViewById(R.id.phoneTextView));
        applyThemeToTextView(findViewById(R.id.emailTextView));
        applyThemeToTextView(findViewById(R.id.addressTextView));
        applyThemeToTextView(findViewById(R.id.noteTextView));
        applyThemeToTextView(findViewById(R.id.nameLabel));
        applyThemeToTextView(findViewById(R.id.phoneLabel));
        applyThemeToTextView(findViewById(R.id.emailLabel));
        applyThemeToTextView(findViewById(R.id.addressLabel));
        applyThemeToTextView(findViewById(R.id.noteLabel));
        
        // Apply to all TextViews that might not have specific IDs
        applyThemeToAllTextViews(findViewById(android.R.id.content));
        
        // Apply to all forms labels (search for TextViews with textStyle="bold")
        applyThemeToFormLabels();
        
        // Apply to EditTexts
        applyThemeToEditText(findViewById(R.id.messageEditText));
        applyThemeToEditText(findViewById(R.id.nameEditText));
        applyThemeToEditText(findViewById(R.id.phoneEditText));
        applyThemeToEditText(findViewById(R.id.emailEditText));
        applyThemeToEditText(findViewById(R.id.addressEditText));
        applyThemeToEditText(findViewById(R.id.noteEditText));
        
        // Apply to message input layout
        applyThemeToView(findViewById(R.id.messageInputLayout));
    }
    
    private void applyThemeToView(View view) {
        if (view != null) {
            boolean isDarkMode = isDarkModeEnabled();
            if (isDarkMode) {
                // Special handling for message input layout
                if (view.getId() == R.id.messageInputLayout) {
                    view.setBackgroundColor(Color.parseColor("#424242"));
                } else {
                    view.setBackgroundColor(Color.parseColor("#2B2B2B"));
                }
            } else {
                view.setBackgroundColor(Color.WHITE);
            }
        }
    }
    
    private void applyThemeToTextView(TextView textView) {
        if (textView != null) {
            boolean isDarkMode = isDarkModeEnabled();
            if (isDarkMode) {
                textView.setTextColor(Color.WHITE);
            } else {
                textView.setTextColor(Color.BLACK);
            }
        }
    }
    
    private void applyThemeToEditText(EditText editText) {
        if (editText != null) {
            boolean isDarkMode = isDarkModeEnabled();
            if (isDarkMode) {
                editText.setTextColor(Color.WHITE);
                editText.setHintTextColor(Color.LTGRAY);
                editText.setBackgroundColor(Color.parseColor("#424242"));
            } else {
                editText.setTextColor(Color.BLACK);
                editText.setHintTextColor(Color.GRAY);
                editText.setBackgroundResource(android.R.drawable.editbox_background);
            }
        }
    }
    
    private void applyThemeToAllTextViews(View view) {
        if (view == null) return;
        
        if (view instanceof TextView && !(view instanceof EditText)) {
            TextView textView = (TextView) view;
            // Skip TextViews that should keep their header color (white text on colored background)
            if (!shouldKeepHeaderColor(textView)) {
                applyThemeToTextView(textView);
            }
        }
        
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyThemeToAllTextViews(group.getChildAt(i));
            }
        }
    }
    
    private boolean shouldKeepHeaderColor(TextView textView) {
        // Check if this TextView is inside a toolbar
        if (isInsideToolbar(textView)) {
            return true;
        }
        
        // Check if this TextView is on a button with header color
        View parent = (View) textView.getParent();
        
        // Skip buttons that use header color (addButton, saveButton, messageButton, sendButton, selectPhotoButton)
        if (parent instanceof Button) {
            Button button = (Button) parent;
            int id = button.getId();
            return (id == R.id.addButton || id == R.id.saveButton || 
                   id == R.id.messageButton || id == R.id.sendButton ||
                   id == R.id.callButton || id == R.id.selectPhotoButton);
        }
        
        // Also check if the TextView itself is a button with header color
        if (textView instanceof Button) {
            int id = textView.getId();
            return (id == R.id.addButton || id == R.id.saveButton || 
                   id == R.id.messageButton || id == R.id.sendButton ||
                   id == R.id.callButton || id == R.id.selectPhotoButton);
        }
        
        return false;
    }
    
    private boolean isInsideToolbar(View view) {
        View parent = (View) view.getParent();
        while (parent != null) {
            if (parent instanceof Toolbar || parent.getId() == R.id.toolbar) {
                return true;
            }
            if (parent.getParent() instanceof View) {
                parent = (View) parent.getParent();
            } else {
                break;
            }
        }
        return false;
    }
    
    private void applyThemeToFormLabels() {
        // This method applies theme specifically to form labels 
        // that typically have textStyle="bold" in layouts
        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        if (rootView != null) {
            searchAndApplyToFormLabels(rootView);
        }
    }
    
    private void searchAndApplyToFormLabels(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            
            if (child instanceof TextView && !(child instanceof EditText)) {
                TextView textView = (TextView) child;
                // Apply theme only if it's not on a header-colored element
                if (!shouldKeepHeaderColor(textView)) {
                    applyThemeToTextView(textView);
                }
            }
            
            if (child instanceof ViewGroup) {
                searchAndApplyToFormLabels((ViewGroup) child);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Apply theme first
        applyTheme();
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            updateHeaderColor(toolbar);
        }
        
        Button addButton = findViewById(R.id.addButton);
        if (addButton != null) {
            applyHeaderColorToButton(addButton);
        }
        
        Button saveButton = findViewById(R.id.saveButton);
        if (saveButton != null) {
            applyHeaderColorToButton(saveButton);
        }
        
        Button messageButton = findViewById(R.id.messageButton);
        if (messageButton != null) {
            applyHeaderColorToButton(messageButton);
        }
        
        Button sendButton = findViewById(R.id.sendButton);
        if (sendButton != null) {
            applyHeaderColorToButton(sendButton);
        }
        
        Button selectPhotoButton = findViewById(R.id.selectPhotoButton);
        if (selectPhotoButton != null) {
            applyHeaderColorToButton(selectPhotoButton);
        }
        
        // Refresh adapters if they exist
        refreshAdapters();
    }
    
    protected void refreshAdapters() {
        // Refresh contact list adapter
        ListView contactListView = findViewById(R.id.contactListView);
        if (contactListView != null && contactListView.getAdapter() instanceof BaseAdapter) {
            ((BaseAdapter) contactListView.getAdapter()).notifyDataSetChanged();
        }
        
        // Refresh message list adapter
        ListView messageListView = findViewById(R.id.messageListView);
        if (messageListView != null && messageListView.getAdapter() instanceof BaseAdapter) {
            ((BaseAdapter) messageListView.getAdapter()).notifyDataSetChanged();
        }
    }
}