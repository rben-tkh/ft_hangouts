package com.ft_hangouts.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
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

    @Override
    protected void onResume() {
        super.onResume();
        
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
    }
}