package com.example.hendrik.mianamalaga.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.widget.Button;


import com.dropbox.core.android.Auth;
import com.example.hendrik.mianamalaga.BuildConfig;
import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.utilities.Utils;
import com.example.hendrik.mianamalaga.R;

import java.io.File;


/**
 * A login screen that offers login via email/password.
 */

public class ActivityLogIntoCloud extends ActivityBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupToolbar();

        Button SignInButton = findViewById(R.id.sign_in_button);
        SignInButton.setOnClickListener(view -> Auth.startOAuth2Authentication(ActivityLogIntoCloud.this, getString(R.string.APP_KEY)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAccessToken();
    }


    public void getAccessToken() {

        String accessToken;
                                                            // TODO one should be able to reset that token in Preference Activity

        SharedPreferences prefs = getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE);
        accessToken = prefs.getString(Constants.OldCloudAccesTokenName, null);

        if ( accessToken == null) {

            switch (Constants.cloudProvider) {

                case DROPBOX:
                    //accessToken = Auth.getOAuth2Token();                                                                     //generate Access Token only works when dropBox App is installed
                    accessToken = BuildConfig.DROPBOX_ACCES_TOKEN;
                    break;

                case NEXTCLOUD:
                    accessToken = BuildConfig.NEXT_CLOUD_USERNAME + Constants.Separator + BuildConfig.NEXT_CLOUD_PASSWORD;
                    break;

                default:
                    Log.e(Constants.TAG, "No cloud provider was chosen!");
                    finish();
                    break;
            }

            if( accessToken != null)
                prefs.edit().putString(Constants.OldCloudAccesTokenName, accessToken).apply();
        }

        if (accessToken != null) {

            Intent intent = new Intent(ActivityLogIntoCloud.this, com.example.hendrik.mianamalaga.activities.ActivityNextCloud.class);     //TODO ActivityDropBoxCloud was there before
            intent.putExtra(Constants.FullTopicDirectoryName, mFullTopicsDirectory.toString());
            startActivity(intent);

        }


    }

    private void setupToolbar(){
        Toolbar toolbar	= findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.ic_gecko_top_view_shape);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
