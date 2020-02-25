package com.example.hendrik.mianamalaga.fragments;

import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.hendrik.mianamalaga.R;


//TODO test the cloud access data and give a feedback . save only the access token which will be saved in the cloud later to make others access the data

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        SwitchPreferenceCompat useDefaultCloudPreference = findPreference("useDefaultCloud");
        SwitchPreferenceCompat useCustomCloudPreference = findPreference("useCustomCloud");
        EditTextPreference cloudUrlPreference = findPreference( getResources().getString( R.string.cloudUrlCustom));
        EditTextPreference cloudUserNamePreference = findPreference( getResources().getString( R.string.cloudUserNameCustom ));
        EditTextPreference cloudPasswordPreference = findPreference(getResources().getString( R.string.cloudPasswordCustom ));


        if ( useDefaultCloudPreference != null ){

            useDefaultCloudPreference.setOnPreferenceClickListener(preference -> {

                if( !useDefaultCloudPreference.isChecked() ){
                    useCustomCloudPreference.setVisible(true);
                } else {
                    useCustomCloudPreference.setVisible(false);
                    cloudUrlPreference.setVisible(false);
                    cloudPasswordPreference.setVisible(false);
                    cloudUserNamePreference.setVisible(false);
                }
                return false;
            });

            if( !useDefaultCloudPreference.isChecked() ){
                useCustomCloudPreference.setVisible(true);
            }

            if( useCustomCloudPreference != null ){
                if( useCustomCloudPreference.isChecked() ){

                    cloudUrlPreference.setVisible(true);
                    cloudPasswordPreference.setVisible(true);
                    cloudUserNamePreference.setVisible(true);

                }

                useCustomCloudPreference.setOnPreferenceClickListener(preference -> {
                    if( useCustomCloudPreference.isChecked() ) {
                        cloudUrlPreference.setVisible(true);
                        cloudPasswordPreference.setVisible(true);
                        cloudUserNamePreference.setVisible(true);
                    } else {
                        cloudUrlPreference.setVisible(false);
                        cloudPasswordPreference.setVisible(false);
                        cloudUserNamePreference.setVisible(false);
                    }
                    return false;
                });

            }

        }

    }
}
