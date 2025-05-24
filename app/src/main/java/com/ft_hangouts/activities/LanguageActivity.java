package com.ft_hangouts.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toolbar;
import com.ft_hangouts.R;
import com.ft_hangouts.utils.LocaleHelper;

public class LanguageActivity extends BaseActivity {

    private RadioGroup languageRadioGroup;
    private TextView currentLanguageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(R.string.change_language);
        }

        languageRadioGroup = findViewById(R.id.languageRadioGroup);
        Button saveButton = findViewById(R.id.saveButton);
        currentLanguageTextView = findViewById(R.id.currentLanguageTextView);

        updateCurrentLanguageText();

        String currentLanguage = LocaleHelper.getLanguage(this);
        if (currentLanguage.equals("fr")) {
            ((RadioButton) findViewById(R.id.radioFrench)).setChecked(true);
        } else if (currentLanguage.equals("en")) {
            ((RadioButton) findViewById(R.id.radioEnglish)).setChecked(true);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLanguage();
            }
        });
    }

    private void updateCurrentLanguageText() {
        String currentLanguage = LocaleHelper.getLanguage(this);
        String languageName = currentLanguage.equals("fr") ? 
                getString(R.string.language_french) : getString(R.string.language_english);
        
        currentLanguageTextView.setText(getString(R.string.current_language) + ": " + languageName);
    }

    private void saveLanguage() {
        String languageCode;

        int selectedRadioId = languageRadioGroup.getCheckedRadioButtonId();

        if (selectedRadioId == R.id.radioFrench) {
            languageCode = "fr";
        } else if (selectedRadioId == R.id.radioEnglish) {
            languageCode = "en";
        } else {
            languageCode = "fr";
        }

        if (!languageCode.equals(LocaleHelper.getLanguage(this))) {
            LocaleHelper.persist(this, languageCode);
            
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            finish();
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