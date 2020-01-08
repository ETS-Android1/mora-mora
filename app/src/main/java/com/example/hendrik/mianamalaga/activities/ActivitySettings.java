package com.example.hendrik.mianamalaga.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hendrik.mianamalaga.Fragments.SettingsFragment;
import com.example.hendrik.mianamalaga.R;

public class ActivitySettings extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }
}

