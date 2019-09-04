package com.example.hendrik.mianamalaga;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;


import com.dropbox.core.android.Auth;
import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.IOUtils;
import com.example.hendrik.mianamalaga.R;

import java.io.File;


/**
 * A login screen that offers login via email/password.
 */

public class ActivityLogIntoCloud extends AppCompatActivity {
    private static String mAppDirectoryPathString;
    private static File mTemporaryDirecotry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupToolbar();
        getIntents();

        Button SignInButton = findViewById(R.id.sign_in_button);
        SignInButton.setOnClickListener(view -> Auth.startOAuth2Authentication(ActivityLogIntoCloud.this, getString(R.string.APP_KEY)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAccessToken();
    }

    private void getIntents() {
        if (getIntent().getExtras() != null){
            mAppDirectoryPathString = getIntent().getExtras().getString(Constants.MoraMora);
            String temporaryDirectory = getIntent().getExtras().getString(Constants.FullTemporaryDirectory);
            mTemporaryDirecotry = new File(temporaryDirectory);

            if( !IOUtils.prepareFileStructure(mAppDirectoryPathString) ){
                finish();
            }
        }
    }

    public void getAccessToken() {

        String accessToken = null;

        switch ( Constants.cloudProvider ) {

            case DROPBOX:
                //accessToken = Auth.getOAuth2Token();                                                                     //generate Access Token only works when dropBox App is installed
                accessToken = Constants.CloudAccessToken;
                break;

            case NEXTCLOUD:
                accessToken = BuildConfig.NEXT_CLOUD_USERNAME + Constants.Separator + BuildConfig.NEXT_CLOUD_PASSWORD;
                break;

            default:
                Log.e(Constants.TAG,"No cloud provider was chosen!");
                finish();
                break;
        }

        if (accessToken != null) {
            SharedPreferences prefs = getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE);            //Store accessToken in SharedPreferences
            prefs.edit().putString(Constants.CloudAccesTokenName, accessToken).apply();

            Intent intent = new Intent(ActivityLogIntoCloud.this, com.example.hendrik.mianamalaga.ActivityNextCloud.class);    //Proceed to ActivityCloud //TODO ActivityCloud was there before
            intent.putExtra(Constants.MoraMora, mAppDirectoryPathString);                           //TODO These String should be defined in Preference Activity
            intent.putExtra(Constants.FullTemporaryDirectory, mTemporaryDirecotry.toString());
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
