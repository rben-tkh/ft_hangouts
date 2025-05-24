package com.ft_hangouts.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toolbar;
import com.ft_hangouts.R;

public class HeaderColorActivity extends BaseActivity {

    private RadioGroup colorRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header_color);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle(R.string.change_header_color);
        }

        colorRadioGroup = findViewById(R.id.colorRadioGroup);
        Button saveButton = findViewById(R.id.saveButton);

        SharedPreferences prefs = getSharedPreferences("HeaderColor", MODE_PRIVATE);
        int currentColor = prefs.getInt("color", R.color.colorPrimary);

        switch (currentColor) {
            case R.color.colorRed:
                ((RadioButton) findViewById(R.id.radioRed)).setChecked(true);
                break;
            case R.color.colorGreen:
                ((RadioButton) findViewById(R.id.radioGreen)).setChecked(true);
                break;
            case R.color.colorBlue:
                ((RadioButton) findViewById(R.id.radioBlue)).setChecked(true);
                break;
            case R.color.colorPurple:
                ((RadioButton) findViewById(R.id.radioPurple)).setChecked(true);
                break;
            default:
                ((RadioButton) findViewById(R.id.radioDefault)).setChecked(true);
                break;
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveHeaderColor();
            }
        });
    }

    private void saveHeaderColor() {
        int selectedColorId;

        int selectedRadioId = colorRadioGroup.getCheckedRadioButtonId();

        if (selectedRadioId == R.id.radioRed) {
            selectedColorId = R.color.colorRed;
        } else if (selectedRadioId == R.id.radioGreen) {
            selectedColorId = R.color.colorGreen;
        } else if (selectedRadioId == R.id.radioBlue) {
            selectedColorId = R.color.colorBlue;
        } else if (selectedRadioId == R.id.radioPurple) {
            selectedColorId = R.color.colorPurple;
        } else {
            selectedColorId = R.color.colorPrimary;
        }

        SharedPreferences prefs = getSharedPreferences("HeaderColor", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("color", selectedColorId);
        editor.apply();

        Toolbar toolbar = findViewById(R.id.toolbar);
        updateHeaderColor(toolbar);

        finish();
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