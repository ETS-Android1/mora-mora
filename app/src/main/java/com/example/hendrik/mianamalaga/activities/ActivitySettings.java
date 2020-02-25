package com.example.hendrik.mianamalaga.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.dialogs.DialogHelp;
import com.example.hendrik.mianamalaga.fragments.SettingsFragment;
import com.example.hendrik.mianamalaga.R;

public class ActivitySettings extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupActionBar();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_settings_container, new SettingsFragment())
                .commit();
    }


    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mToolbar	=	findViewById(R.id.toolbar_settings);
            if ( mToolbar != null ) {
                setSupportActionBar(mToolbar);
                getSupportActionBar().setIcon(R.drawable.ic_gecko_top_view_shape);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.help_menu:
                DialogHelp helpDialog = DialogHelp.newInstance( getString( R.string.ActivitySettingsHelpText) );
                helpDialog.show( getSupportFragmentManager(), Constants.TAG );
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}



